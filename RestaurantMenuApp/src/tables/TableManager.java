package tables;


import core.RestaurantManager;
import core.Restaurant;
import core.RestaurantData;
import enums.DataType;
import menu.AlaCarteItem;
import menu.MenuItem;
import menu.PromotionPackage;
import tools.ConsolePrinter;
import tools.FileIO;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableManager extends RestaurantManager {
    private LocalTime amOpeningHour;
    private LocalTime amClosingHour;
    private LocalTime pmOpeningHour;
    private LocalTime pmClosingHour;

    public TableManager(Restaurant restaurant) {
        super(restaurant);
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("HH:mm").toFormatter(Locale.ENGLISH);
        amOpeningHour = LocalTime.parse("11:00");
        amClosingHour = LocalTime.parse("15:00");
        pmOpeningHour = LocalTime.parse("18:00");
        pmClosingHour = LocalTime.parse("22:00");

        if (getRestaurant().isDataTypeExists(DataType.TABLE)) {
            checkReservations();
        }
    }

    @Override
    public void init() {
        getRestaurant().registerClassToDataType(Table.class, DataType.TABLE);
        getRestaurant().registerClassToDataType(Order.class, DataType.ORDER);

        final FileIO f = new FileIO();
        final List<String[]> tableData = f.read(DataType.TABLE).stream().map(data -> data.split(" // ")).filter(data -> data.length >= 4 && data.length <= 5).collect(Collectors.toList());
        final List<String[]> orderData = f.read(DataType.ORDER).stream().map(data -> data.split(" // ")).filter(data -> (data.length >= 3 && data.length <= 4)).collect(Collectors.toList());

        if (tableData.size() == 0) {
            IntStream.range(0, 10).map(id -> (2 * 10 + id)).forEach(id -> getRestaurant().save(new Table(id, 2)));
            IntStream.range(0, 10).map(id -> (4 * 10 + id)).forEach(id -> getRestaurant().save(new Table(id, 4)));
            IntStream.range(0, 5).map(id -> (8 * 10 + id)).forEach(id -> getRestaurant().save(new Table(id, 8)));
            IntStream.range(0, 5).map(id -> (10 * 10 + id)).forEach(id -> getRestaurant().save(new Table(id, 10)));
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
                            Optional<MenuItem> item = getRestaurant().getDataFromId(DataType.ALA_CARTE_ITEM, itemId);
                            item.ifPresent(x -> order.addItem(x, count));
                        } else {
                            Optional<MenuItem> item = getRestaurant().getDataFromId(DataType.PROMO_PACKAGE, itemId);
                            item.ifPresent(x -> order.addItem(x, count));
                        }
                    }
                }

                getRestaurant().load(order);

                final Optional<Table> table = getRestaurant().getDataFromId(DataType.TABLE, tableId);
                table.ifPresent(x -> x.attachOrder(order));
                table.ifPresent(x -> getRestaurant().save(x));
            } catch (NumberFormatException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data for detected for " + DataType.ORDER.name() + ": " + e.getMessage());
            }
        }

        checkReservations();
    }

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

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[]{
                this::viewTable,
                this::createNewOrder,
                this::manageOrders,
                this::showReservations,
                this::newReservation,
                this::getReservation,
        };
    }

    private void viewTable() {
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

    private void viewOrder(Table table) {
        List<String> displayList = Collections.singletonList(table.getOrder().toDisplayString());
        ConsolePrinter.printTable("Table // Order ID // Staff ID // Order Details", displayList, true);
        getInputHelper().getInt("Enter 0 to go back", 0, 0);
        ConsolePrinter.clearCmd();
    }

    private void createNewOrder() {
        ConsolePrinter.printInstructions(Arrays.asList("Enter number of pax and an empty table will automatically be assigned.", "Enter 0 to exit."));
        int pax = getInputHelper().getInt("Enter number of pax", 0, 10);

        if (pax == 0) {
            return;
        }

        if (pax == 5) {
            pax += 1;
        }

        Optional<Table> oTable = getAvailableTable(LocalDateTime.now(), pax);

        if (oTable.isEmpty()) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "There is no table available for " + pax + " " + ((pax == 1) ? "person" : "people") + ".");
            return;
        }

        Table table = oTable.get();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String orderId = LocalDateTime.now().format(format);
        Order order = table.attachOrder(getRestaurant().getSessionStaffId());

        if (getRestaurant().save(order) && getRestaurant().save(table)) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Order " + orderId + " has been created successfully.");
        }
    }

    private void manageOrders() {
        final Optional<List<Table>> dataList = getRestaurant().getDataList(DataType.TABLE);
        final List<Table> activeTableList =  dataList.map(data -> data.stream().filter(Table::hasOrder).collect(Collectors.toList())).get();
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
                    ConsolePrinter.clearCmd();
                    return;
            }
        }
    }

    private void addItemToOrder(Table table) {
        final List<AlaCarteItem> alaCarteItemList = new ArrayList<>();
        getRestaurant().getDataList(DataType.ALA_CARTE_ITEM).ifPresent(data -> data.forEach(item -> alaCarteItemList.add((AlaCarteItem) item)));
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
        final List<PromotionPackage> promoPackageList = new ArrayList<>();
        getRestaurant().getDataList(DataType.PROMO_PACKAGE).ifPresent(data -> data.forEach(item -> promoPackageList.add((PromotionPackage) item)));
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
            return;
        }

        int itemCount = referenceList.size();
        DataType dataType = (itemIndex < itemCount)? DataType.ALA_CARTE_ITEM : DataType.PROMO_PACKAGE;
        MenuItem item;

        if (dataType.equals(DataType.ALA_CARTE_ITEM)) {
            item = referenceList.get(itemIndex);
        } else {
            itemIndex -= itemCount;
            Optional<MenuItem> oItem = getRestaurant().getDataFromIndex(dataType, itemIndex);

            if (oItem.isEmpty()) {
                return;
            }

            item = oItem.get();
        }

        table.getOrder().addItem(item, count);

        if (getRestaurant().save(table.getOrder()) && getRestaurant().save(table)) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been added to order successfully.");
        }
    }

    private void removeItemsFromOrder(Table table) {
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

        if (getRestaurant().save(table.getOrder()) && getRestaurant().save(table)) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Items have been removed from order successfully.");
        }
    }

    private void voidOrder(Table table) {
        ConsolePrinter.printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

        if (getInputHelper().getString("Confirm void?").equalsIgnoreCase("Y")) {
            Order order = table.getOrder();
            table.clear();

            if (getRestaurant().remove(order) && getRestaurant().save(table)) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Order has been voided successfully.");
            }
        } else {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Void operation aborted.");
        }
    }

    private void showReservations() {
        Optional<List<Table>> tableList = getRestaurant().getDataList(DataType.TABLE);
        List<Table.Reservation> reservationList = new ArrayList<>();
        tableList.ifPresent(data -> data.forEach(table -> reservationList.addAll(table.getReservationMap().values())));
        List<String> printList = new ArrayList<>();
        reservationList.forEach(r -> printList.add(r.toDisplayString()));
        ConsolePrinter.printTable("Reservation List", "Table ID // Name // Contact // Date & Time // Pax", printList, true);
        getInputHelper().getInt("Enter 0 to go back", 0, 0);
        ConsolePrinter.clearCmd();
    }

    private void newReservation() {
        ConsolePrinter.printInstructions(Arrays.asList("1. Reservation may only be made one month in advance.", "2. Reservation may only be made for tomorrow onwards.", "Date format: ddMMyyyy"));
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy").toFormatter(Locale.ENGLISH);
        String date;
        LocalDate dateFormatted;

        do {
            try {
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
        ConsolePrinter.printInstructions(Arrays.asList("Reservations may only be made from opening hours to ONE hour before closing.", "Time format: HH:mm (24-hour)"));
        String time;
        LocalTime timeFormatted;

        do {
            try {
                time = getInputHelper().getString("Enter reserving time");
                timeFormatted = LocalTime.parse(time, formatter);

                if (timeFormatted.isBefore(amOpeningHour) || timeFormatted.isAfter(pmClosingHour.minusHours(1)) || (timeFormatted.isAfter(amClosingHour.minusHours(1)) && timeFormatted.isBefore(pmOpeningHour))) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Invalid date.");
                } else {
                    break;
                }
            } catch (DateTimeParseException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Invalid date format.");
            }
        } while (true);

        int pax = getInputHelper().getInt("Enter number of pax", 0, 10);

        if (pax == 0) {
            return;
        }

        formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyyHHmm").toFormatter(Locale.ENGLISH);
        LocalDateTime reserveDateTime = LocalDateTime.parse(date + time, formatter);
        Optional<Table> oTable = getAvailableTable(reserveDateTime, pax);

        if (oTable.isEmpty()) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "There is no table available for " + pax + " " + ((pax == 1) ? "person" : "people") + ".");
            return;
        }

        Table table = oTable.get();
        String name = getInputHelper().getString("Enter name");
        int contact = getInputHelper().getInt("Enter contact number", 65000000, 99999999);

        if (table.addReservation(contact, name, reserveDateTime, pax)) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Reservation has been made successfully.");
        } else {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to make a reservation for some reason.");
        }
    }

    private void getReservation(){
        int contact;
        Optional<List<Table>> oTableList = getRestaurant().getDataList(DataType.TABLE);

        if (oTableList.isEmpty()) {
            return;
        }

        contact = getInputHelper().getInt("Enter contact number to check for reservation", 65000000, 99999999);
        List<Table.Reservation> reservationList = new ArrayList<>();

        for (Table table : oTableList.get()){
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
                return;
            }

            Table.Reservation reservation = reservationList.get(choice - 1);
            String[] actions = new String[] {"Fulfil reservation", "Delete reservation"};
            List<String> choiceList = ConsolePrinter.formatChoiceList(Arrays.asList(actions), null);

            ConsolePrinter.printTable("Manage Reservation", "Command // Action", choiceList, true);
            int action = getInputHelper().getInt("Select action", 0, actions.length);

            if (action == 1) {
                fulfilReservation(reservation);
            } else {
                deleteReservation(reservation);
            }
        }
    }

    private void fulfilReservation(Table.Reservation reservation) {
        Optional<Table> table = getRestaurant().getDataFromId(DataType.TABLE, reservation.getTableId());

        if (table.isPresent()) {
            if (!reservation.isArrivalWindow()) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Professor Oak's words echoed... There's a time and place for everything, but not now.");
                return;
            }

            Order order = table.get().attachOrder(getRestaurant().getSessionStaffId());
            table.get().deleteReservation(reservation);

            if (getRestaurant().save(order) && getRestaurant().save(table.get())) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Reservation has been fulfilled successfully.");
                return;
            }
        }

        ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to fulfil reservation.");
    }

    private void deleteReservation(Table.Reservation reservation) {
        Optional<Table> oTable = getRestaurant().getDataFromId(DataType.TABLE, reservation.getTableId());

        if (oTable.isEmpty()) {
            return;
        }

        Table table = oTable.get();
        table.deleteReservation(reservation);

        if (getRestaurant().save(table)) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Reservation has been deleted successfully.");
            return;
        }

        ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to delete reservation.");
    }

    private void checkReservations() {
        Optional<List<Table>> tableList = getRestaurant().getDataList(DataType.TABLE);
        tableList.ifPresent(list -> list.forEach(Table::removeExpiredReservations));

        if (tableList.isPresent()) {
            for (Table t : tableList.get()) {
                t.setReserved(false);

                for (Table.Reservation r : t.getReservationMap().values()) {
                    if (r.isCurrentSession()) {
                        t.setReserved(true);
                    }
                }

                getRestaurant().save(t);
            }
        }
    }

    /*
    private void printBill() {
        /*
        List<Table> activeTableList = getActiveTables();
        List<String> nameList = new ArrayList<>();

        for (Table table : activeTableList) {
            nameList.add("Table " + table.getId());
        }


        int tableIndex = ConsolePrinter.printChoices("Select a table", "Index // Active Tables", nameList, new String[]{"Go back"}) - 1;
        if (tableIndex == -2) {
            return;
        }

        Table table = activeTableList.get(tableIndex);
        for (RestaurantData o : getRestaurant().getDataList(DataType.ORDER)) {
            if (o.getId() == table.getId()) {
                if (o instanceof Order) {
                    List<String> invoice = ((Order) o).toInvoiceString();
                    String total = invoice.get(invoice.size() - 1);
                    invoice.remove(invoice.size() - 1);

                    //ConsolePrinter.printTitle("Invoice for Table " + table.getId(), true);
                    //ConsolePrinter.printTable(Collections.singletonList(invoice.get(0)), false, false, false, false , true);
                    //ConsolePrinter.printDivider(' ');

                    invoice.remove(0);
                    invoice.add(0, "QTY // ITEM DESCRIPTION // TOTAL");
                    //ConsolePrinter.printTable(invoice, false, false, false, false, false);
                    //ConsolePrinter.printDivider('-');

                    invoice = new ArrayList<>(Collections.singletonList(total));
                    invoice.add(0, "TOTAL AMOUNT DESCRIPTION // TOTAL");
                    //ConsolePrinter.printTable(invoice, false, false, false, false , false);
                    //ConsolePrinter.printDivider('=');
                    o.toFileString();

                    try {
                        getRestaurant().remove(o);
                        table.clear();
                        getRestaurant().update(table);
                        return;
                    } catch (IOException | Restaurant.FileIDMismatchException e) {
                        System.out.print("Failed to clear table: " + e.getMessage());
                    }
                }
            }
        }
    }
    */

    private Optional<Table> getAvailableTable(LocalDateTime dateTime, int pax) {
        final Optional<List<Table>> dataList = getRestaurant().getDataList(DataType.TABLE);
        final List<Table> emptyTableList =  dataList.map(data -> data.stream().filter(table -> table.isAvailable(dateTime)).collect(Collectors.toList())).get();
        return (emptyTableList.stream().filter(table -> table.isLargeEnough(pax)).findFirst());
    }

    private List<String> getTableDisplayList(int sortOption) {
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

        final Optional<List<Table>> dataList = getRestaurant().getDataList(DataType.TABLE);
        return dataList.map(data -> data.stream().sorted(comparator).map(Table::toDisplayString).collect(Collectors.toList())).get();
    }
}