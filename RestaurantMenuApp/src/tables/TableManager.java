package tables;


import client.DataManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import menu.AlaCarteItem;
import menu.MenuItem;
import menu.MenuManager;
import menu.PromotionPackage;
import tools.FileIO;
import tools.Log;
import tools.Pair;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableManager extends DataManager {

    public TableManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws IOException {
        getRestaurant().registerClass(Table.class, DataType.TABLE);
        getRestaurant().registerClass(Order.class, DataType.ORDER);
        getRestaurant().registerClass(Table.Reservation.class, DataType.RESERVATION);

        final FileIO f = new FileIO();
        final List<String[]> tableData = f.read(DataType.TABLE).stream().map(data -> data.split(" // ")).filter(data -> data.length == 3).collect(Collectors.toList());
        final List<String[]> orderData = f.read(DataType.ORDER).stream().map(data -> data.split(" // ")).filter(data -> (data.length >= 3 && data.length <= 4)).collect(Collectors.toList());
        final List<String[]> reservationData = f.read(DataType.RESERVATION).stream().map(data -> data.split(" // ")).filter(data -> data.length == 3).collect(Collectors.toList());

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
                    Table table = new Table(id, capacity, occupied, null);

                    if (!getRestaurant().load(table)) {
                        return;
                    }
                } catch (NumberFormatException e) {
                    Logger.getAnonymousLogger().log(Level.WARNING, "Invalid file data: " + e.getMessage());
                }
            }
        }

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
                        MenuItem item;

                        if (itemId < 100000) {
                            item = (MenuItem) getRestaurant().getDataFromId(DataType.ALA_CARTE_ITEM, itemId);
                        } else {
                            item = (MenuItem) getRestaurant().getDataFromId(DataType.PROMO_PACKAGE, itemId);
                        }

                        order.addItem(item, count);
                    }
                }

                getRestaurant().load(order);
                RestaurantData table = getRestaurant().getDataFromId(DataType.TABLE, tableId);

                if (table instanceof Table) {
                    ((Table) table).attachOrder(order);
                    getRestaurant().update(table);
                }
            } catch (NumberFormatException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Invalid file data: " + e.getMessage());
            }
        }

        for (String[] data : reservationData) {
            try {
                int tableId = Integer.parseInt(data[0]);
                int contact = Integer.parseInt(data[1]);
                String name = data[2];
                LocalDateTime date = formatting(data[3]);
                int pax = Integer.parseInt(data[4]);
                RestaurantData table = getRestaurant().getDataFromId(DataType.TABLE, tableId);

                if (table instanceof Table) {
                    ((Table) table).addReservation(contact, name, date, pax);
                    getRestaurant().update(table);
                }
            } catch (NumberFormatException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Invalid file data: " + e.getMessage());
            }
        }

    }

    private Pair<List<String>, Map<Integer, MenuItem>> getMenuDisplayList() {
        final MenuManager manager = new MenuManager(getRestaurant());
        final Set<String> categoryList = manager.getAlaCarteCategories();
        final List<AlaCarteItem> alaCarteItemList = getRestaurant().getData(DataType.ALA_CARTE_ITEM).stream().filter(data -> data instanceof AlaCarteItem).map(data -> (AlaCarteItem) data).collect(Collectors.toList());
        List<String> displayList = new ArrayList<>();
        List<MenuItem> tempList;
        Map<Integer, MenuItem> itemMap = new HashMap<>();
        int index = 0;

        for (String category : categoryList) {
            tempList = new ArrayList<>();
            displayList.add("\\SUB // " + category);
            alaCarteItemList.stream().filter(data -> data.getCategory().equals(category)).forEach(tempList::add);

            for (MenuItem item : tempList) {
                itemMap.put(index, item);
                displayList.add(item.getName());
                index++;
            }

            displayList.add("---");
        }

        displayList.add("\\SUB // Promotion Package");
        tempList = getRestaurant().getData(DataType.PROMO_PACKAGE).stream().filter(data -> data instanceof PromotionPackage).map(data -> (PromotionPackage) data).collect(Collectors.toList());

        for (MenuItem item : tempList) {
            itemMap.put(index, item);
            displayList.add(item.getName());
            index++;
        }

        return (new Pair<>(displayList, itemMap));
    }

    private List<String> getTableDisplayList(int sortOption) {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.TABLE);
        List<Table> tableList = dataList.stream().filter(table -> table instanceof Table).map(table -> (Table) table).collect(Collectors.toList());

        if (sortOption > 1) {
            tableList.sort((sortOption == 3) ? Comparator.comparing(Table::isReserved) : Comparator.comparing(Table::isOccupied));
        } else {
            tableList.sort(Comparator.comparingInt(RestaurantData::getId));
        }

        return tableList.stream().map(Table::toDisplayString).collect(Collectors.toList());
    }

    private List<String> getOrderDisplayList(Table table) {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.ORDER);
        return dataList.stream().filter(order -> order instanceof Order).filter(order -> order.getId() == table.getId()).map(RestaurantData::toDisplayString).collect(Collectors.toList());
    }

    private List<Table> getActiveTables() {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.TABLE);
        return dataList.stream().filter(table -> table instanceof Table).filter(table -> ((Table) table).isOccupied()).map(table -> (Table) table).collect(Collectors.toList());
    }

    private Table getAvailableTable(int pax) {
        if (pax == 5) {
            pax += 1;
        }

        final int fPax = pax;
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.TABLE);
        final List<Table> emptyTableList = dataList.stream().filter(table -> table instanceof Table).filter(table -> !((Table) table).isOccupied()).map(table -> (Table) table).collect(Collectors.toList());
        return (emptyTableList.stream().filter(table -> table.getCapacity() >= fPax && table.getCapacity() <= (fPax + 2)).findFirst().orElse(null));
    }

    private boolean addItem(Table table, MenuItem item, int count) {
        table.getOrder().addItem(item, count);
        return (getRestaurant().update(table.getOrder()) && getRestaurant().update(table));
    }

    private boolean removeItem(Table table, OrderItem item, int removeCount) {
        if (removeCount < 0) {
            removeCount *= -1;
        }

        if (removeCount == item.getCount()) {
            table.getOrder().removeItem(item);
            return (getRestaurant().update(table.getOrder()) && getRestaurant().update(table));
        }

        removeCount *= -1;
        item.updateCount(removeCount);
        return getRestaurant().update(table.getOrder());
    }

    private boolean voidOrder(Table table) {
        final List<Order> orderList = getRestaurant().getData(DataType.ORDER).stream().filter(data -> data instanceof Order).map(data -> (Order) data).collect(Collectors.toList());
        Order order = orderList.stream().filter(data -> data.getId() == table.getId()).findFirst().orElse(null);

        if (order == null) {
            Log.error(this, "Order is null when attempting to void order.");
            return false;
        }

        table.clear();

        if (getRestaurant().remove(order) && getRestaurant().update(table)) {
            return true;
        }

        return false;
    }

    public void setOccupied(int tableId, String orderId, int staffId) {
        Table table = (Table) getRestaurant().getDataFromId(DataType.TABLE, tableId);
        table.attachOrder(orderId, staffId);
    }

    public void checkVacancy() {
        for (RestaurantData t : getRestaurant().getData(DataType.TABLE)) {
            System.out.println(t.toString());
        }
    }

    private String getSession(LocalDateTime dateTime) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("a");
        return dateTime.format(format);
    }

    private LocalDateTime formatDateTime(String dateTime) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
        return LocalDateTime.parse(dateTime, format);
    }

    public void deleteReservation(Scanner s) {
        int index;
        showReservation();
        int id = getCs().getInt("Enter TableID", 20, 104);
        Table table = (Table) getRestaurant().getDataFromId(DataType.TABLE, id);
        table.showReservationList();
        int remove = getCs().getInt("Enter Reservation ID To Delete", 1, Integer.MAX_VALUE);
        table.getReservationList().remove(remove - 1);
        showReservation();
        System.out.println("Successfully Removed.");
    }

    public Table checkReservation(LocalDateTime combineDate, int pax) {
        DateTimeFormatter session = DateTimeFormatter.ofPattern("a");

        for (RestaurantData t : getRestaurant().getData(DataType.TABLE)) {
            if (t instanceof Table) {
                if (pax <= 2 && (t.getId() / 10 == 2)) {
                    if (((Table) t).getReservationList().size() == 0) {
                        return ((Table) t);
                    }

                    for (Table.Reservation r : ((Table) t).getReservationList()) {
                        if (!compareDate(combineDate, r.getDate()))
                            return ((Table) t);
                        else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                            return ((Table) t);
                        }
                    }
                }
                if ((pax == 3 || pax == 4) && (t.getId() / 10 == 4)) {
                    if (((Table) t).getReservationList().size() == 0) {
                        return ((Table) t);
                    }

                    for (Table.Reservation r : ((Table) t).getReservationList()) {
                        if (!compareDate(combineDate, r.getDate()))
                            return ((Table) t);
                        else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                            return ((Table) t);
                        }
                    }
                }
                if (pax >= 5 && pax <= 8 && (t.getId() / 10 == 8)) {
                    if (((Table) t).getReservationList().size() == 0) {
                        return ((Table) t);
                    }

                    for (Table.Reservation r : ((Table) t).getReservationList()) {
                        if (!compareDate(combineDate, r.getDate()))
                            return ((Table) t);
                        else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                            return ((Table) t);
                        }
                    }
                }
                if (pax >= 9 && (t.getId() / 10 == 10)) {
                    if (((Table) t).getReservationList().size() == 0) {
                        return ((Table) t);
                    }

                    for (Table.Reservation r : ((Table) t).getReservationList()) {
                        if (!compareDate(combineDate, r.getDate()))
                            return ((Table) t);
                        else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                            return ((Table) t);
                        }
                    }
                }
            }
        }
        return null;
    }

    public void searchReservation(Scanner s) {
        int contact;
        boolean check = false;

        System.out.println("Enter contact number to check for reservation.");
        contact = s.nextInt();
        for (RestaurantData t : getRestaurant().getData(DataType.TABLE)) {
            if (t instanceof Table) {
                if (((Table) t).findReservation(contact)) {
                    check = true;
                    break;
                }
            }
        }
        if (!check)
            System.out.println("No reservation found.");
    }

    public void showReservation() {

        for (RestaurantData t : getRestaurant().getData(DataType.TABLE)) {
            if (t instanceof Table) {
                int count = 1;
                if (((Table) t).getReservationList().size() != 0) {
                    System.out.println("For Table " + t.getId());
                    for (Table.Reservation r : ((Table) t).getReservationList()) {
                        System.out.println(count++ + ". " + r.toStringTwo());
                    }
                }
            }
        }
    }

    private LocalDateTime formatting(String datetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
        return LocalDateTime.parse(datetime, formatter);
    }

    public boolean compareDate(LocalDateTime compare1, LocalDateTime compare2) {
        return compare1.toLocalDate().equals(compare2.toLocalDate());
    }

    @Override
    public String[] getMainCLIOptions() {
        return new String[]{
                "View table status",
                "Create new order",
                "Manage orders"
        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[]{
                this::viewTable,
                this::createNewOrder,
                this::manageOrders
        };
    }

    private void f() {
        Log.notice("Not available. Code re-writing in progress.");
    }

    private void viewTable() {
        int sortOption = 1;
        final List<String> sortOptions = Arrays.asList("Sort by ID", "Sort by occupancy", "Sort by reservation");
        final List<String> options = getCs().formatChoiceList(sortOptions, null);
        List<String> displayList;

        do {
            getCs().clearCmd();
            displayList = getTableDisplayList(sortOption);
            int itemCount = displayList.size();
            int startIndex = (int) Math.ceil(1.0 * displayList.size() / 2);

            for (int index = startIndex; index < displayList.size(); index++) {
                displayList.set(index - startIndex, displayList.get(index - startIndex) + " // " + displayList.get(index));
            }

            for (int index = displayList.size() - 1; index >= startIndex; index--) {
                displayList.remove(index);
            }

            final String headers = (itemCount > 1) ? "ID // Occupied // Reserved // ID // Occupied // Reserved" : "ID // Occupied // Reserved";
            getCs().printTable("Table Status", headers, displayList, true);
            getCs().printTable("Command // Sort Option", options, true);
        } while ((sortOption = getCs().getInt("Select a sort option", 0, sortOptions.size())) != 0);
        getCs().clearCmd();
    }

    private void viewOrder(Table table) {
        String headers = "Table // Order ID // Staff ID // Order Details";
        List<String> displayList = getOrderDisplayList(table);
        getCs().printTable(headers, displayList, true);
        getCs().getInt("Enter 0 to go back", 0, 0);
        getCs().clearCmd();
    }

    private void createNewOrder() {
        getCs().printInstructions(Collections.singletonList("Enter number of pax and an empty table will be automatically assigned."));
        final int pax = getCs().getInt("Enter number of pax", 1, 10);
        final Table table = getAvailableTable(pax);

        if (table == null) {
            getCs().clearCmd();
            System.out.println("There is no table available for " + pax + " " + ((pax == 1) ? "person" : "people") + ".");
            return;
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String orderId = LocalDateTime.now().format(format);
        Order order = table.attachOrder(orderId, getRestaurant().getSessionStaffId());

        if (getRestaurant().save(order) && getRestaurant().update(table)) {
            getCs().clearCmd();
            System.out.println("Order " + orderId + " has been created successfully.");
        } else {
            getCs().clearCmd();
            System.out.println("Failed to create order.");
        }
    }

    private void manageOrders() {
        final List<Table> activeTableList = getActiveTables();
        List<String> displayList = activeTableList.stream().map(table -> "Table: " + table.getId() + " - Order " + table.getOrder().getOrderId()).collect(Collectors.toList());
        List<String> choiceList = getCs().formatChoiceList(displayList, null);
        getCs().printTable("Manage Orders", "Command // Active Tables", choiceList, true);
        int tableIndex = getCs().getInt("Select an order to manage", 0, displayList.size());

        if (tableIndex == 0) {
            return;
        }

        tableIndex--;
        Table table = activeTableList.get(tableIndex);

        while (true) {
            final List<String> actions = Arrays.asList("View order", "Add item to order", "Remove item from order", "Void order", "Print bill");
            choiceList = getCs().formatChoiceList(actions, null);
            getCs().printTable("Command // Action", choiceList, true);
            final int action = getCs().getInt("Select an action", 0, actions.size());

            if (action == 0) {
                return;
            }

            if (action == 1) {
                viewOrder(table);
            }

            if (action == 2 && (addItemToOrder(table))) {
                getCs().clearCmd();
                System.out.println("Item has been added to order successfully.");
            }

            if (action == 3 && (removeItemsFromOrder(table))) {
                getCs().clearCmd();
                System.out.println("Item has been remove from order successfully.");
            }

            if (action == 4 && confirmBeforeVoid(table)) {
                getCs().clearCmd();
                System.out.println("Order has been voided successfully.");
                return;
            }

            if (action == 5) {

            }
        }
    }

    private boolean addItemToOrder(Table table) {
        Pair<List<String>, Map<Integer, MenuItem>> pair = getMenuDisplayList();
        List<String> displayList = pair.getLeft();
        Map<Integer, MenuItem> itemMap = pair.getRight();
        List<String> choiceList = getCs().formatChoiceList(displayList, null);

        getCs().printTable("Command // Menu Item", choiceList, true);
        int itemIndex = getCs().getInt("Select an item to add to order", 0, displayList.size());
        int count = getCs().getInt("Enter the amount to add", 1, 100);

        MenuItem item = itemMap.get(itemIndex - 1);
        return addItem(table, item, count);
    }

    private boolean removeItemsFromOrder(Table table) {
        List<String> displayList = new ArrayList<>();
        Map<Integer, OrderItem> indexItemMap = new HashMap<>();

        int index = 1;
        for (OrderItem item : table.getOrder().getOrderItemList()) {
            displayList.add(item.getItem().getName());
            indexItemMap.put(index, item);
            index++;
        }

        List<String> choiceList = getCs().formatChoiceList(displayList, null);
        getCs().printTable("Command // Menu Item", choiceList, true);
        int itemIndex = getCs().getInt("Select the item to remove to order", 0, displayList.size());

        OrderItem item = indexItemMap.get(itemIndex);
        getCs().printInstructions(Arrays.asList("Amount in order: " + item.getCount(), "Enter 0 to go back."));
        int removeCount = getCs().getInt("Enter the amount to remove", 0, item.getCount());

        if (removeCount == 0) {
            System.out.println("Remove operation aborted.");
            return false;
        }

        return removeItem(table, item, removeCount);
    }

    private boolean confirmBeforeVoid(Table table) {
        getCs().printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));
        if (getCs().getString("Confirm void?").equalsIgnoreCase("Y")) {
            if (voidOrder(table)) {
                return true;
            }
        } else {
            getCs().clearCmd();
            System.out.println("Void operation aborted.");
        }

        return false;
    }

    private void printBill() {
        /*
        List<Table> activeTableList = getActiveTables();
        List<String> nameList = new ArrayList<>();

        for (Table table : activeTableList) {
            nameList.add("Table " + table.getId());
        }


        int tableIndex = getCs().printChoices("Select a table", "Index // Active Tables", nameList, new String[]{"Go back"}) - 1;
        if (tableIndex == -2) {
            return;
        }

        Table table = activeTableList.get(tableIndex);
        for (RestaurantData o : getRestaurant().getData(DataType.ORDER)) {
            if (o.getId() == table.getId()) {
                if (o instanceof Order) {
                    List<String> invoice = ((Order) o).toInvoiceString();
                    String total = invoice.get(invoice.size() - 1);
                    invoice.remove(invoice.size() - 1);

                    //getCs().printTitle("Invoice for Table " + table.getId(), true);
                    //getCs().printTable(Collections.singletonList(invoice.get(0)), false, false, false, false , true);
                    //getCs().printDivider(' ');

                    invoice.remove(0);
                    invoice.add(0, "QTY // ITEM DESCRIPTION // TOTAL");
                    //getCs().printTable(invoice, false, false, false, false, false);
                    //getCs().printDivider('-');

                    invoice = new ArrayList<>(Collections.singletonList(total));
                    invoice.add(0, "TOTAL AMOUNT DESCRIPTION // TOTAL");
                    //getCs().printTable(invoice, false, false, false, false , false);
                    //getCs().printDivider('=');
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
        }*/
    }
}