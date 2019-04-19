package tables;


import core.Restaurant;
import core.RestaurantData;
import core.RestaurantManager;
import enums.DataType;
import menu.AlaCarteItem;
import menu.MenuItem;
import menu.PromotionPackage;
import staff.Staff;
import tools.ConsolePrinter;
import tools.FileIO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages table, order information in the restaurant list database through a CLI.
 * Additionally manages reservation information within each table object.
 */
public class TableManager extends RestaurantManager {
    /**
     * Constant of restaurant opening hour for the morning session.
     */
    private final LocalTime AM_OPENING = LocalTime.of(11, 0);

    /**
     * Constant of restaurant closing hour for the morning session.
     */
    private final LocalTime AM_CLOSING = LocalTime.of(15, 0);

    /**
     * Constant of restaurant opening hour for the afternoon session.
     */
    private final LocalTime PM_OPENING = LocalTime.of(18, 0);

    /**
     * Constant of restaurant closing hour for the afternoon session.
     */
    private final LocalTime PM_CLOSING = LocalTime.of(22, 0);

    /**
     * Initialises the manager with a restaurant object for data storage and manipulation.
     * Reservations are checked for each table, and expired reservations (over 30 minutes) will be removed.
     * @param restaurant Restaurant instance from main.
     * @throws Exception Errors that occurred while checking reservations.
     */
    public TableManager(Restaurant restaurant) throws Exception {
        super(restaurant);

        if (getRestaurant().isDataTypeExists(DataType.TABLE)) {
            checkReservations();
        }
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public void init() throws Exception {
        final FileIO f = new FileIO();
        final List<String[]> tableData = f.read(DataType.TABLE).stream().map(data -> data.split(" // ")).filter(data -> data.length >= 4 && data.length <= 5).collect(Collectors.toList());
        final List<String[]> orderData = f.read(DataType.ORDER).stream().map(data -> data.split(" // ")).filter(data -> (data.length >= 3 && data.length <= 4)).collect(Collectors.toList());

        if (tableData.size() == 0) {
            int cap = 10;

            for (int prefix = 2; prefix <= 10; prefix += 2) {
                for (int seat = 0; seat < cap; seat++) {
                    final int id = prefix * 10 + seat;
                    getRestaurant().save(new Table(id, prefix));
                }

                if (prefix == 4) {
                    prefix += 2;
                    cap = 5;
                }
            }
        } else {
            for (String[] data : tableData) {
                try {
                    int id = Integer.parseInt(data[0]);
                    int capacity = Integer.parseInt(data[1]);
                    boolean occupied = Boolean.parseBoolean(data[2]);
                    boolean reserved = Boolean.parseBoolean(data[3]);
                    Table table = new Table(id, capacity, occupied, reserved, null);

                    if (data.length == 5) {
                        for (String reservations : data[4].split("--")) {
                            String[] reservationData = reservations.split(",");
                            final int contact = Integer.parseInt(reservationData[0]);
                            final String name = reservationData[1];
                            DateTimeFormatter fileFormat = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
                            final LocalDateTime date = LocalDateTime.parse(reservationData[2], fileFormat);
                            final int pax = Integer.parseInt(reservationData[3]);
                            table.addReservation(contact, name, date, pax);
                        }
                    }

                    getRestaurant().load(table);
                } catch (NumberFormatException e) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data for detected for " + DataType.TABLE.name() + ": " + e.getMessage());
                }
            }
        }

        getRestaurant().bulkSave(DataType.TABLE);

        for (String[] data : orderData) {
            try {
                final int tableId = Integer.parseInt(data[0]);
                final String orderId = data[1];
                final int staffId = Integer.parseInt(data[2]);
                Order order = new Order(tableId, orderId, staffId);

                if (data.length == 4) {
                    for (String itemData : data[3].split("--")) {
                        int itemId = Integer.parseInt(itemData.split("x")[0]);
                        int count = Integer.parseInt(itemData.split("x")[1]);

                        if (itemId < 100000) {
                            MenuItem item = getRestaurant().getDataFromId(DataType.ALA_CARTE_ITEM, itemId);
                            order.addItem(item, count);
                        } else {
                            MenuItem item = getRestaurant().getDataFromId(DataType.PROMO_PACKAGE, itemId);
                            order.addItem(item, count);
                        }
                    }
                }

                getRestaurant().load(order);

                final Table table = getRestaurant().getDataFromId(DataType.TABLE, tableId);
                table.attachOrder(order);
                getRestaurant().save(table);
            } catch (NumberFormatException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data for detected for " + DataType.ORDER.name() + ": " + e.getMessage());
            }
        }

        checkReservations();
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public String[] getMainCLIOptions() {
        return new String[]{
                "View table status",
                "Create new walk-in order",
                "Manage orders",
                "View all reservations",
                "Make new reservation",
                "Manage reservations",
        };
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[]{
                () -> display(1),
                () -> display(2),
                () -> display(3),
                () -> display(4),
                () -> display(5),
                () -> display(6),
        };
    }

    /**
     * Maps to the various methods to run. Used as some methods throw exceptions, which will be caught by this method for logging purposes.
     * @param which Which method to run.
     */
    private void display(int which) {
        try {
            switch (which) {
                case 1:
                    viewTable();
                    break;

                case 2:
                    createNewOrder();
                    break;

                case 3:
                    manageOrders();
                    break;

                case 4:
                    showReservations();
                    break;

                case 5:
                    newReservation();
                    break;

                case 6:
                    manageReservations();
                    break;
            }
        } catch (Exception e) {
            ConsolePrinter.logToFile(e.getMessage(), e);
        }
    }

    /**
     * Displays all tables along with their occupancy and reservation status.
     * Reservation status are displayed for the current session only.
     * @throws Exception Errors that occurred while displaying the table information.
     */
    private void viewTable() throws Exception {
        int sortOption = 1;
        final List<String> sortOptions = Arrays.asList("Sort by ID", "Sort by occupancy", "Sort by reservation");
        final List<String> options = ConsolePrinter.formatChoiceList(sortOptions, null);
        List<String> displayList;

        do {
            ConsolePrinter.clearCmd();
            displayList = getTableDisplayList(sortOption);
            ConsolePrinter.printTable("Table Status", "ID // Occupied // Reserved", displayList, true);
            ConsolePrinter.printTable("Command // Sort Option", options, true);
        } while ((sortOption = getInputHelper().getInt("Select a sort option", 0, sortOptions.size())) != 0);

        ConsolePrinter.clearCmd();
    }

    /**
     * Displays order details for the specified table.
     * @param table Table to display the order from.
     */
    private void viewOrder(Table table) {
        List<String> displayList = Collections.singletonList(table.getOrder().toDisplayString());
        ConsolePrinter.printTable("Table // Order ID // Staff ID // Order Details", displayList, true);
        getInputHelper().getInt("Enter 0 to go back", 0, 0);
        ConsolePrinter.clearCmd();
    }

    /**
     * Creates a new order given a number of pax.
     * @throws Exception Errors that occurred while creating an order.
     */
    private void createNewOrder() throws Exception {
        ConsolePrinter.printInstructions(Arrays.asList("Enter number of pax and an empty table will automatically be assigned.", "Enter 0 to exit."));
        int pax = getInputHelper().getInt("Enter number of pax", 0, 10);
        if (pax == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        Table table = getAvailableTable(LocalDateTime.now(), pax);
        String orderId = LocalDateTime.now().format(format);
        Order order = table.attachOrder(getRestaurant().getSessionStaffId());
        getRestaurant().save(order);
        getRestaurant().save(table);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Order " + orderId + " has been created successfully.");
    }

    /**
     * Manages open orders in the restaurant list database.
     * @throws Exception Errors that occurred while managing orders.
     */
    private void manageOrders() throws Exception {
        final List<Table> dataList = getRestaurant().getDataList(DataType.TABLE);
        final List<Table> activeTableList =  dataList.stream().filter(Table::hasOrder).collect(Collectors.toList());
        List<String> displayList = activeTableList.stream().map(table -> "Table: " + table.getId() + ": Order " + table.getOrder().getOrderId()).collect(Collectors.toList());
        List<String> choiceList = ConsolePrinter.formatChoiceList(displayList, null);

        ConsolePrinter.printTable("Manage Orders", "Command // Active Tables", choiceList, true);
        int tableIndex = getInputHelper().getInt("Select an order to manage", 0, displayList.size()) - 1;
        if (tableIndex == -1) {
            ConsolePrinter.clearCmd();
            return;
        }

        Table table = activeTableList.get(tableIndex);

        while (true) {
            final List<String> actions = Arrays.asList("View order", "Add item to order", "Remove item from order", "Void order", "Print bill");
            choiceList = ConsolePrinter.formatChoiceList(actions, null);
            ConsolePrinter.printTable("Command // Action", choiceList, true);
            final int action = getInputHelper().getInt("Select an action", 0, actions.size());

            switch (action) {
                case 0:
                    ConsolePrinter.clearCmd();
                    return;

                case 1:
                    viewOrder(table);
                    break;

                case 2:
                    addItemToOrder(table);
                    break;

                case 3:
                    removeItemsFromOrder(table);
                    break;

                case 4:
                    voidOrder(table);
                    return;

                case 5:
                    printBill(table);
                    ConsolePrinter.clearCmd();
                    return;
            }
        }
    }

    /**
     * Adds menu items into an open order.
     * @param table The table which the order is attached to.
     * @throws Exception Errors that occurred while adding items into the order.
     */
    private void addItemToOrder(Table table) throws Exception {
        final List<AlaCarteItem> alaCarteItemList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        final Set<String> categoryList = alaCarteItemList.stream().map(AlaCarteItem::getCategory).collect(Collectors.toSet());
        final List<String> displayList = new ArrayList<>();
        final List<AlaCarteItem> referenceList = new ArrayList<>();
        int trashCount = 0;

        for (String category : categoryList) {
            displayList.add("\\SUB" + category);
            final List<AlaCarteItem> tempList = alaCarteItemList.stream().filter(item -> item.matchCategory(category)).collect(Collectors.toList());
            displayList.addAll(tempList.stream().map(AlaCarteItem::getName).collect(Collectors.toList()));
            referenceList.addAll(tempList);
            trashCount++;
        }

        displayList.add("\\SUB" + "Promotion Packages");
        final List<PromotionPackage> promoPackageList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        displayList.addAll(promoPackageList.stream().map(MenuItem::getName).collect(Collectors.toList()));
        trashCount++;

        List<String> choiceList = ConsolePrinter.formatChoiceList(displayList, null);
        ConsolePrinter.printTable("Command // Menu Item", choiceList, true);
        int itemIndex = getInputHelper().getInt("Select an item to add to order", 0, displayList.size() - trashCount) - 1;
        if (itemIndex == -1) {
            ConsolePrinter.clearCmd();
            return;
        }

        int count = getInputHelper().getInt("Enter the amount to add", 0, 100);
        if (count == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        int itemCount = referenceList.size();
        DataType dataType = (itemIndex < itemCount)? DataType.ALA_CARTE_ITEM : DataType.PROMO_PACKAGE;
        MenuItem item;

        if (dataType.equals(DataType.ALA_CARTE_ITEM)) {
            item = referenceList.get(itemIndex);
        } else {
            itemIndex -= itemCount;
            item = getRestaurant().getDataFromIndex(dataType, itemIndex);
        }

        table.getOrder().addItem(item, count);
        getRestaurant().save(table.getOrder());
        getRestaurant().save(table);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been added to order successfully.");
    }

    /**
     * Removes existing items in an open order.
     * @param table The table which the order is attached to.
     * @throws Exception Errors that occurred while removing items from the order.
     */
    private void removeItemsFromOrder(Table table) throws Exception {
        List<String> displayList = new ArrayList<>();
        Map<Integer, Order.OrderItem> indexItemMap = new HashMap<>();

        int index = 1;
        for (Order.OrderItem item : table.getOrder().getOrderItemList()) {
            displayList.add(table.getOrder().getItemName(item));
            indexItemMap.put(index, item);
            index++;
        }

        List<String> choiceList = ConsolePrinter.formatChoiceList(displayList, null);
        ConsolePrinter.printTable("Command // Menu Item", choiceList, true);
        int itemIndex = getInputHelper().getInt("Select the item to remove to order", 0, displayList.size());
        if (itemIndex == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        Order.OrderItem item = indexItemMap.get(itemIndex);
        ConsolePrinter.printInstructions(Arrays.asList("Amount in order: " + table.getOrder().getItemCount(item), "Enter 0 to go back."));
        int removeCount = getInputHelper().getInt("Enter the amount to remove", 0, table.getOrder().getItemCount(item));

        if (removeCount == 0) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Remove operation aborted.");
            return;
        }

        if (!table.getOrder().removeItems(item, removeCount)) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Failed to remove items from order.");
            return;
        }

        getRestaurant().save(table.getOrder());
        getRestaurant().save(table);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Items have been removed from order successfully.");
    }

    /**
     * Voids an open order without sales recording.
     * @param table The table which the order is attached to.
     * @throws Exception Errors that occurred while voiding the order.
     */
    private void voidOrder(Table table) throws Exception {
        ConsolePrinter.printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

        if (getInputHelper().getString("Confirm void?").equalsIgnoreCase("Y")) {
            Order order = table.getOrder();
            table.clear();
            getRestaurant().remove(order);
            getRestaurant().save(table);
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Order has been voided successfully.");
        } else {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Void operation aborted.");
        }
    }

    /**
     * Displays all reservations and their details.
     * @throws Exception Errors that occurred while displaying the reservations.
     */
    private void showReservations() throws Exception {
        List<Table> tableList = getRestaurant().getDataList(DataType.TABLE);
        List<Table.Reservation> reservationList = new ArrayList<>();
        tableList.forEach(table -> reservationList.addAll(table.getReservationMap().values()));

        List<String> printList = new ArrayList<>();
        reservationList.forEach(r -> printList.add(r.toDisplayString()));

        ConsolePrinter.printTable("Reservation List", "Table ID // Name // Contact // Date & Time // Pax", printList, true);
        getInputHelper().getInt("Enter 0 to go back", 0, 0);
        ConsolePrinter.clearCmd();
    }

    /**
     * Makes a new reservation for a table.
     * @throws Exception Errors that occurred while making the reservation.
     */
    private void newReservation() throws Exception {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy").toFormatter(Locale.ENGLISH);
        String date;
        LocalDate dateFormatted;

        do {
            try {
                ConsolePrinter.printInstructions(Arrays.asList("1. Reservation may only be made one month in advance.", "2. Reservation may only be made for tomorrow onwards.", "Date format: ddMMyyyy"));
                date = getInputHelper().getString("Enter reserving date");
                dateFormatted = LocalDate.parse(date, formatter);

                if ((dateFormatted.minus(Period.ofMonths(1)).isAfter(LocalDateTime.now().toLocalDate())) || (dateFormatted.isBefore(LocalDateTime.now().toLocalDate()) || dateFormatted.isEqual(LocalDateTime.now().toLocalDate()))) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Invalid date.");
                } else {
                    break;
                }
            } catch (DateTimeParseException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Invalid date format.");
            }
        } while (true);

        formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("HHmm").toFormatter(Locale.ENGLISH);
        String time;
        LocalTime timeFormatted;

        do {
            try {
                ConsolePrinter.printInstructions(Arrays.asList("Reservations may only be made from opening hours to ONE hour before closing.", "Time format: HHmm (24-hour)"));
                time = getInputHelper().getString("Enter reserving time");
                timeFormatted = LocalTime.parse(time, formatter);

                if (timeFormatted.isBefore(AM_OPENING) || timeFormatted.isAfter(PM_CLOSING.minusHours(1)) || (timeFormatted.isAfter(AM_CLOSING.minusHours(1)) && timeFormatted.isBefore(PM_OPENING))) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Invalid time.");
                } else {
                    break;
                }
            } catch (DateTimeParseException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Invalid time format.");
            }
        } while (true);

        int pax = getInputHelper().getInt("Enter number of pax", 0, 10);

        if (pax == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyyHHmm").toFormatter(Locale.ENGLISH);
        LocalDateTime reserveDateTime = LocalDateTime.parse(date + time, formatter);
        Table table = getAvailableTable(reserveDateTime, pax);
        String name = getInputHelper().getString("Enter name");
        int contact = getInputHelper().getInt("Enter contact number", 65000000, 99999999);

        if (table.addReservation(contact, name, reserveDateTime, pax)) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Reservation has been made successfully.");
        } else {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to make a reservation for some reason.");
        }
    }

    /**
     * Manages existing reservations under a contact number.
     * @throws Exception Errors that occurred while managing the reservations.
     */
    private void manageReservations() throws Exception {
        int contact;
        List<Table> tableList = getRestaurant().getDataList(DataType.TABLE);
        List<Table.Reservation> reservationList = new ArrayList<>();
        contact = getInputHelper().getInt("Enter contact number to check for reservation", 65000000, 99999999);

        for (Table table : tableList){
            reservationList.addAll(table.findReservationsByContact(contact));
        }

        if (reservationList.size() == 0) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "No reservation found.");
        } else {
            List<String> printList = new ArrayList<>();
            reservationList.forEach(r -> printList.add(r.getDateStr() + " // " + r.getTableId() + " // " + r.getPax()));
            ConsolePrinter.printTable("Command // Date // Table ID // Pax", ConsolePrinter.formatChoiceList(printList, null), true);
            int choice = getInputHelper().getInt("Select reservation to manage", 0, reservationList.size());

            if (choice == 0) {
                ConsolePrinter.clearCmd();
                return;
            }

            Table.Reservation reservation = reservationList.get(choice - 1);
            String[] actions = new String[]{"Fulfil reservation", "Delete reservation"};
            List<String> choiceList = ConsolePrinter.formatChoiceList(Arrays.asList(actions), null);

            ConsolePrinter.printTable("Manage Reservation", "Command // Action", choiceList, true);
            int action = getInputHelper().getInt("Select action", 0, actions.length);

            if (action == 0) {
                ConsolePrinter.clearCmd();
            } else if (action == 1) {
                fulfilReservation(reservation);
            } else {
                deleteReservation(reservation);
            }
        }
    }

    /**
     * Fulfil a reservation and opens an order for that reservation.
     * @param reservation Reservation to fulfil.
     * @throws Exception Errors that occurred while fulfilling the reservation.
     */
    private void fulfilReservation(Table.Reservation reservation) throws Exception {
        Table table = getRestaurant().getDataFromId(DataType.TABLE, reservation.getTableId());

        if (!reservation.isArrivalWindow()) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Professor Oak's words echoed... There's a time and place for everything, but not now.");
            return;
        }

        Order order = table.attachOrder(getRestaurant().getSessionStaffId());
        table.deleteReservation(reservation);
        getRestaurant().save(order);
        getRestaurant().save(table);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Reservation has been fulfilled successfully.");
    }

    /**
     * Removes a reservation from a table.
     * @param reservation Reservation to remove.
     * @throws Exception Errors that occurred while removing the reservation.
     */
    private void deleteReservation(Table.Reservation reservation) throws Exception {
        Table table = getRestaurant().getDataFromId(DataType.TABLE, reservation.getTableId());
        table.deleteReservation(reservation);
        getRestaurant().save(table);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Reservation has been deleted successfully.");
    }

    /**
     * Checks existing reservations.
     * Expired reservations (over 30 minutes) are removed for their respective tables.
     * Sets tables with reservations in the current session to reserved status.
     * @throws Exception Errors that occurred while checking the reservations.
     */
    private void checkReservations() throws Exception {
        List<Table> tableList = getRestaurant().getDataList(DataType.TABLE);

        for (Table table : tableList) {
            table.removeExpiredReservations();
            table.setReserved(false);

            for (Table.Reservation reservation : table.getReservationMap().values()) {
                if (reservation.isCurrentSession()) {
                    table.setReserved(true);
                    break;
                }
            }

            getRestaurant().save(table);
        }
    }

    /**
     * Prints the bill invoice for a specified table.
     * @param table Table to print invoice for.
     * @throws Exception Errors that occurred while printing the bill invoice.
     */
    private void printBill(Table table) throws Exception {
        Order order = table.getOrder();

        if (order == null) {
            return;
        }

        if (order.getOrderItemList().size() == 0) {
            return;
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        DateTimeFormatter format2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter format3 = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
        String receiptId = order.getOrderId();
        LocalDateTime dateTime = LocalDateTime.parse(receiptId, format);
        int tableNo= table.getId();
        int staffId = getRestaurant().getSessionStaffId();
        final Staff staff = getRestaurant().getDataFromId(DataType.STAFF, staffId);
        final String title = "Scam Money Restaurant";
        List<String> printList = new ArrayList<>();

        ConsolePrinter.clearCmd();
        printList.add("Server: " + staff.getName() + " // " + dateTime.format(format2));
        printList.add("Table " + tableNo);
        printList.add(" ");

        BigDecimal total = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
        StringBuilder sb = new StringBuilder(dateTime.format(format3) + " // ");

        for (Order.OrderItem o : order.getOrderItemList()) {
            final String name = order.getItemName(o);
            final int count = order.getItemCount(o);
            final BigDecimal price = order.getItemPrice(o);
            printList.add(name + " x " + count + " // " + price);
            total = total.add(price);
            sb.append(name).append(" - ").append(count).append(" - ").append(price);
            sb.append("--");
        }

        printList.add(" ");

        BigDecimal sc = total.multiply(new BigDecimal(0.5)).setScale(2, RoundingMode.FLOOR);
        BigDecimal gst = total.multiply(new BigDecimal(0.07)).setScale(2, RoundingMode.FLOOR);
        printList.add("Service charge (50%): // " + sc);
        printList.add("GST (7%): // " + gst);
        printList.add("Total (incl. GST and service charge): // " + total.add(sc).add(gst));
        ConsolePrinter.printTable(title, "", printList, false);
        getInputHelper().getInt("Enter 0 to process payment", 0, 0);
        (new FileIO()).writeLine("revenue", sb.toString());
        table.clear();
        getRestaurant().remove(order);
        getRestaurant().save(table);
    }

    /**
     * Searches the restaurant list database for an available table for a given dateTime and pax.
     * @param dateTime The date and time to obtain an available table for.
     * @param pax Number of pax to obtain an available table for.
     * @return Available table found in the database.
     * @throws Exception Errors that occurred while searching for an available table, or if no table is available for the specified pax at the specified date and time.
     */
    private Table getAvailableTable(LocalDateTime dateTime, int pax) throws Exception {
        final List<Table> dataList = getRestaurant().getDataList(DataType.TABLE);
        final List<Table> emptyTableList =  dataList.stream().filter(table -> table.isAvailable(dateTime)).collect(Collectors.toList());
        Optional<Table> oTable = (emptyTableList.stream().filter(table -> table.isLargeEnough(pax)).findFirst());

        if (oTable.isEmpty()) {
            throw (new Exception("There is no table available for " + pax + " " + ((pax == 1) ? "person" : "people") + "."));
        }

        return oTable.get();
    }

    /**
     * Formats the data objects obtained from the restaurant list database into table-friendly format.
     * @param sortOption Determines which comparator to sort the formatted list by.
     * @return List of formatted object data. One object per entry.
     * @throws Exception Errors that occurred while obtaining data objects.
     */
    private List<String> getTableDisplayList(int sortOption) throws Exception {
        Comparator<Table> comparator;

        switch (sortOption) {
            case 2:
                comparator = Comparator.comparing(Table::isOccupied);
                break;
            case 3:
                comparator = Comparator.comparing(Table::isReserved);
                break;
            default:
                comparator = Comparator.comparingInt(RestaurantData::getId);
        }

        final List<Table> dataList = getRestaurant().getDataList(DataType.TABLE);
        return dataList.stream().sorted(comparator).map(Table::toDisplayString).collect(Collectors.toList());
    }
}