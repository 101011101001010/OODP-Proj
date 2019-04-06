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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableManager extends DataManager {

    public TableManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() {
        getRestaurant().registerClassToDataType(Table.class, DataType.TABLE);
        getRestaurant().registerClassToDataType(Order.class, DataType.ORDER);
        getRestaurant().registerClassToDataType(Table.Reservation.class, DataType.RESERVATION);

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
                    getCm().clearCmd();
                    Log.warning("Invalid file data for detected for " + DataType.TABLE.name() + ": " + e.getMessage());
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
                getCm().clearCmd();
                Log.warning("Invalid file data for detected for " + DataType.ORDER.name() + ": " + e.getMessage());
            }
        }

        for (String[] data : reservationData) {
            try {
                int tableId = Integer.parseInt(data[0]);
                int contact = Integer.parseInt(data[1]);
                String name = data[2];
                LocalDateTime date = formatting(data[3]);
                int pax = Integer.parseInt(data[4]);
                final Optional<Table> table = getRestaurant().getDataFromId(DataType.TABLE, tableId);
                table.ifPresent(x -> x.addReservation(contact, name, date, pax));
                table.ifPresent(x -> getRestaurant().save(x));
            } catch (NumberFormatException e) {
                getCm().clearCmd();
                Log.warning("Invalid file data for detected for " + DataType.RESERVATION.name() + ": " + e.getMessage());
            }
        }
    }

    private List<String> getAlaCarteDisplayList(String category) {
        final Optional<List<AlaCarteItem>> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        return dataList.map(data -> data.stream().filter(x -> x.getCategory().equals(category)).sorted(Comparator.comparing(MenuItem::getName)).map(AlaCarteItem::getName).collect(Collectors.toList())).get();
    }

    private List<String> getPromoPackageDisplayList() {
        final Optional<List<PromotionPackage>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        return dataList.map(data -> data.stream().sorted(Comparator.comparing(MenuItem::getName)).map(PromotionPackage::getName).collect(Collectors.toList())).get();
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

    private List<String> getOrderDisplayList(Table table) {
        final Optional<List<Order>> dataList = getRestaurant().getDataList(DataType.ORDER);
        return dataList.map(data -> data.stream().filter(x -> x.getId() == table.getId()).sorted(Comparator.comparing(RestaurantData::getId)).map(Order::toDisplayString).collect(Collectors.toList())).get();
    }

    private List<Table> getActiveTables() {
        final Optional<List<Table>> dataList = getRestaurant().getDataList(DataType.TABLE);
        return dataList.map(data -> data.stream().filter(Table::isOccupied).sorted(Comparator.comparing(RestaurantData::getId)).collect(Collectors.toList())).get();
    }

    private Optional<Table> getAvailableTable(int pax) {
        if (pax == 5) {
            pax += 1;
        }

        final int fPax = pax;
        final Optional<List<Table>> dataList = getRestaurant().getDataList(DataType.TABLE);
        final List<Table> emptyTableList =  dataList.map(data -> data.stream().filter(x -> !x.isOccupied()).sorted(Comparator.comparing(RestaurantData::getId)).collect(Collectors.toList())).get();
        return (emptyTableList.stream().filter(table -> table.getCapacity() >= fPax && table.getCapacity() <= (fPax + 2)).findFirst());
    }

    private LocalDateTime formatting(String datetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
        return LocalDateTime.parse(datetime, formatter);
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
        final List<String> options = getCm().formatChoiceList(sortOptions, null);
        List<String> displayList;

        do {
            getCm().clearCmd();
            displayList = getTableDisplayList(sortOption);
            int itemCount = displayList.size();
            int startIndex = (int) Math.ceil(1.0 * displayList.size() / 2);

            for (int index = startIndex; index < displayList.size(); index++) {
                displayList.set(index - startIndex, displayList.get(index - startIndex) + " // " + displayList.get(index));
            }

            if (displayList.size() > startIndex) {
                displayList.subList(startIndex, displayList.size()).clear();
            }

            final String headers = (itemCount > 1) ? "ID // Occupied // Reserved // ID // Occupied // Reserved" : "ID // Occupied // Reserved";
            getCm().printTable("Table Status", headers, displayList, true);
            getCm().printTable("Command // Sort Option", options, true);
        } while ((sortOption = getCm().getInt("Select a sort option", 0, sortOptions.size())) != 0);
        getCm().clearCmd();
    }

    private void viewOrder(Table table) {
        String headers = "Table // Order ID // Staff ID // Order Details";
        List<String> displayList = getOrderDisplayList(table);
        getCm().printTable(headers, displayList, true);
        getCm().getInt("Enter 0 to go back", 0, 0);
        getCm().clearCmd();
    }

    private void createNewOrder() {
        getCm().printInstructions(Collections.singletonList("Enter number of pax and an empty table will be automatically assigned."));
        final int pax = getCm().getInt("Enter number of pax", 1, 10);
        final Optional<Table> oTable = getAvailableTable(pax);

        if (oTable.isEmpty()) {
            getCm().clearCmd();
            System.out.println("There is no table available for " + pax + " " + ((pax == 1) ? "person" : "people") + ".");
            return;
        }

        Table table = oTable.get();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String orderId = LocalDateTime.now().format(format);
        Order order = table.attachOrder(orderId, getRestaurant().getSessionStaffId());

        if (getRestaurant().save(order) && getRestaurant().save(table)) {
            getCm().clearCmd();
            System.out.println("Order " + orderId + " has been created successfully.");
        }
    }

    private void manageOrders() {
        final List<Table> activeTableList = getActiveTables();
        List<String> displayList = activeTableList.stream().map(table -> "Table: " + table.getId() + ": Order " + table.getOrder().getOrderId()).collect(Collectors.toList());
        List<String> choiceList = getCm().formatChoiceList(displayList, null);
        getCm().printTable("Manage Orders", "Command // Active Tables", choiceList, true);
        int tableIndex = getCm().getInt("Select an order to manage", 0, displayList.size()) - 1;

        if (tableIndex == -1) {
            return;
        }

        Table table = activeTableList.get(tableIndex);

        while (true) {
            final List<String> actions = Arrays.asList("View order", "Add item to order", "Remove item from order", "Void order", "Print bill");
            choiceList = getCm().formatChoiceList(actions, null);
            getCm().printTable("Command // Action", choiceList, true);
            final int action = getCm().getInt("Select an action", 0, actions.size());

            if (action == 0) {
                return;
            }

            if (action == 1) {
                viewOrder(table);
            }

            if (action == 2 && (addItemToOrder(table))) {
                getCm().clearCmd();
                System.out.println("Item has been added to order successfully.");
            }

            if (action == 3 && (removeItemsFromOrder(table))) {
                getCm().clearCmd();
                System.out.println("Item has been remove from order successfully.");
            }

            if (action == 4 && voidOrder(table)) {
                getCm().clearCmd();
                System.out.println("Order has been voided successfully.");
                return;
            }

            if (action == 5) {
                f();
            }
        }
    }

    private boolean addItemToOrder(Table table) {
        getCm().clearCmd();
        MenuManager manager = new MenuManager(getRestaurant());
        final Set<String> categoryList = manager.getAlaCarteCategories();
        List<String> displayList = new ArrayList<>();
        int itemCount = 0;
        int trashCount = 0;

        for (String category : categoryList) {
            displayList.add("\\SUB // " + category);
            displayList.addAll(getAlaCarteDisplayList(category));
            displayList.add("---");
            trashCount += 2;
            itemCount = displayList.size() - trashCount;
        }

        System.out.println(itemCount);
        displayList.add("\\SUB // " + "Promotion Packages");
        displayList.addAll(getPromoPackageDisplayList());
        List<String> choiceList = getCm().formatChoiceList(displayList, null);
        getCm().printTable("Command // Menu Item", choiceList, true);

        int itemIndex = getCm().getInt("Select an item to add to order", 0, displayList.size());
        int count = getCm().getInt("Enter the amount to add", 1, 100);
        DataType dataType = (itemIndex <= itemCount)? DataType.ALA_CARTE_ITEM : DataType.PROMO_PACKAGE;

        if (dataType.equals(DataType.PROMO_PACKAGE)) {
            itemIndex -= itemCount;
        }

        Optional<MenuItem> item = getRestaurant().getDataFromIndex(dataType, itemIndex - 1);

        if (item.isPresent()) {
            table.getOrder().addItem(item.get(), count);
            return (getRestaurant().save(table.getOrder()) && getRestaurant().save(table));
        }

        return false;
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

        List<String> choiceList = getCm().formatChoiceList(displayList, null);
        getCm().printTable("Command // Menu Item", choiceList, true);
        int itemIndex = getCm().getInt("Select the item to remove to order", 0, displayList.size());

        OrderItem item = indexItemMap.get(itemIndex);
        getCm().printInstructions(Arrays.asList("Amount in order: " + item.getCount(), "Enter 0 to go back."));
        int removeCount = getCm().getInt("Enter the amount to remove", 0, item.getCount());

        if (removeCount == 0) {
            System.out.println("Remove operation aborted.");
            return false;
        }

        if (removeCount < 0) {
            removeCount *= -1;
        }

        if (removeCount == item.getCount()) {
            table.getOrder().removeItem(item);
            return (getRestaurant().save(table.getOrder()) && getRestaurant().save(table));
        }

        removeCount *= -1;
        item.updateCount(removeCount);
        return (getRestaurant().save(table.getOrder()) && getRestaurant().save(table));
    }

    private boolean voidOrder(Table table) {
        getCm().printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

        if (getCm().getString("Confirm void?").equalsIgnoreCase("Y")) {
            Order order = table.getOrder();
            table.clear();
            return (getRestaurant().remove(order) && getRestaurant().save(table));
        }

        return false;
    }

    /*
    private void printBill() {
        /*
        List<Table> activeTableList = getActiveTables();
        List<String> nameList = new ArrayList<>();

        for (Table table : activeTableList) {
            nameList.add("Table " + table.getId());
        }


        int tableIndex = getCm().printChoices("Select a table", "Index // Active Tables", nameList, new String[]{"Go back"}) - 1;
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

                    //getCm().printTitle("Invoice for Table " + table.getId(), true);
                    //getCm().printTable(Collections.singletonList(invoice.get(0)), false, false, false, false , true);
                    //getCm().printDivider(' ');

                    invoice.remove(0);
                    invoice.add(0, "QTY // ITEM DESCRIPTION // TOTAL");
                    //getCm().printTable(invoice, false, false, false, false, false);
                    //getCm().printDivider('-');

                    invoice = new ArrayList<>(Collections.singletonList(total));
                    invoice.add(0, "TOTAL AMOUNT DESCRIPTION // TOTAL");
                    //getCm().printTable(invoice, false, false, false, false , false);
                    //getCm().printDivider('=');
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
}