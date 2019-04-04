package order;

import client.BaseManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import menu.MenuItem;
import menu.MenuManager;
import tables.Table;
import tools.FileIO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderManager extends BaseManager {

    public OrderManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws ManagerInitFailedException {
        try {
            getRestaurant().registerClass(Order.class, DataType.ORDER);
        } catch (Restaurant.ClassNotRegisteredException e) {
            throw (new ManagerInitFailedException(this, "Class registration failed: " + e.getMessage()));
        }

        FileIO f = new FileIO();
        List<String> orderData;

        try {
            orderData = f.read(DataType.ORDER);
        } catch (IOException e) {
            throw (new ManagerInitFailedException(this, "Unable to load tables or orders from file: " + e.getMessage()));
        }

        for (String data : orderData) {
            String[] datas = data.split(" // ");

            if (datas.length < 3) {
                continue;
            }

            int id;
            try {
                id = Integer.parseInt(datas[0]);
            } catch (NumberFormatException e) {
                throw (new ManagerInitFailedException(this, e.getMessage()));
            }

            Order order = new Order(id, datas[1], Integer.parseInt(datas[2]));

            if (datas.length == 4) {
                String[] items = datas[3].split("--");

                for (String item : items) {
                    int itemId;
                    int count;
                    try {
                        itemId = Integer.parseInt(item.split("x")[0]);
                        count = Integer.parseInt(item.split("x")[1]);
                    } catch (NumberFormatException e) {
                        throw (new ManagerInitFailedException(this, "Invalid file data: " + e.getMessage()));
                    }

                    MenuItem menuItem;
                    try {
                        if (itemId > 99999) {
                            menuItem = (MenuItem) getRestaurant().getDataFromId(DataType.PROMO_PACKAGE, itemId);
                        } else {
                            menuItem = (MenuItem) getRestaurant().getDataFromId(DataType.ALACARTE, itemId);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        throw (new ManagerInitFailedException(this, "Invalid file data: " + e.getMessage()));
                    }

                    order.addItem(menuItem, count);
                }
            }

            getRestaurant().load(order);
        }
    }

    private List<String> getDisplay(int sortOption) {
        List<? extends RestaurantData> masterList = new ArrayList<>(getRestaurant().getData(DataType.ORDER));
        List<String> ret = new ArrayList<>();
        if (masterList.size() == 0) {
            ret.add("There is no order added yet.");
            return ret;
        }

        masterList.sort((item1, item2) -> {
            switch (sortOption) {
                case 2:
                    return ((Order) item1).getOrderId().compareTo(((Order) item2).getOrderId());
                case 3:
                    return Integer.compare(((Order) item1).getStaffID(), ((Order) item2).getStaffID());
            }

            return Integer.compare(item1.getId(), item2.getId());
        });

        ret.add("Table ID // Order ID // Staff ID // Order Items");
        for (RestaurantData o : masterList) {
            ret.add(o.toTableString());
        } return ret;
    }

    private List<Table> getActiveTables() {
        List<Table> nameList = new ArrayList<>();
        for (RestaurantData o : getRestaurant().getData(DataType.TABLE)) {
            if (o instanceof Table) {
                if (((Table) o).isOccupied()) {
                    nameList.add((Table) o);
                }
            }
        } return nameList;
    }

    private List<Table> getEmptyTables(int pax) {
        if (pax == 5) {
            pax += 1;
        }

        List<Table> nameList = new ArrayList<>();
        for (RestaurantData o : getRestaurant().getData(DataType.TABLE)) {
            if (o instanceof Table) {
                if (!((Table) o).isOccupied() && ((Table) o).getCapacity() >= pax && ((Table) o).getCapacity() <= (pax + 2)) {
                    nameList.add((Table) o);
                }
            }
        } return nameList;
    }

    @Override
    public String[] getMainCLIOptions() {
        return new String[]{
                "View orders",
                "Add new order",
                "Add item to order",
                "Remove order",
                "Print bill"
        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[]{
                this::viewOrder,
                this::addNewOrder,
                this::addItem,
                this::removeOrder,
                this::printBill
        };
    }

    private void viewOrder() {
        int choice = 1;
        List<String> displayList;

        do {
            displayList = getDisplay(choice);
            getCs().printDisplayTable("View Order", displayList, true, true);
        } while ((choice = getCs().printChoices("Select a sort option", "Command // Function", Arrays.asList("Sort by ID", "Sort by staff ID", "Sort by table ID"), new String[]{"Go back"})) != -1);
    }

    private void addNewOrder() {
        int pax = getCs().getInt("Enter number of pax", 1, 10);
        List<Table> emptyTableList = getEmptyTables(pax);
        List<String> emptyTableDisplay = new ArrayList<>();
        for (Table table : emptyTableList) {
            emptyTableDisplay.add("Table " + table.getId());
        }

        getCs().printChoicesSimple("Command // Empty Tables", emptyTableDisplay, new String[]{"Go back"});
        int tableIndex = getCs().getInt("Select table", 0, emptyTableDisplay.size()) - 1;
        if (tableIndex == -1) {
            return;
        }

        Table table = emptyTableList.get(tableIndex);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String orderId = LocalDateTime.now().format(format);
        Order order = table.attachOrder(orderId, getRestaurant().getSessionStaffId());

        try {
            getRestaurant().save(order);
            System.out.println("Order added");
        } catch (IOException e) {
            System.out.println("Failed to add order: " + e.getMessage());
        }
    }

    private void addItem() {
        String[] footer = new String[]{"Go back"};
        List<Table> activeTableList = getActiveTables();
        List<String> displayList = new ArrayList<>();

        for (Table table : activeTableList) {
            displayList.add("Table " + table.getId());
        }

        int tableIndex = getCs().printChoices("Select a table", "Index // Active Tables", displayList, footer) - 1;
        if (tableIndex == -2) {
            return;
        }

        while (true) {
            int type = getCs().printChoices("Select item type", "Index // Item Type", new String[]{"Ala-Carte Items", "Promotion Packages"}, footer);
            if (type == -1) {
                return;
            }

            DataType dataType = (type == 1) ? DataType.ALACARTE : DataType.PROMO_PACKAGE;
            displayList = (new MenuManager(getRestaurant()).getItemNames(dataType));

            int itemIndex = getCs().printChoices("Select an item to add", "Index // Menu Item", displayList, footer) - 1;
            if (itemIndex == -1) {
                return;
            }

            int itemCount = getCs().getInt("Enter the item count", 0, 100);
            Table table = activeTableList.get(tableIndex);
            //int tableId = table.getId();
            //table = (Table) getRestaurant().getDataFromId(DataType.TABLE, tableId);
            Order order = table.getOrder();
            RestaurantData item = getRestaurant().getDataFromIndex(dataType, itemIndex);

            if (item instanceof MenuItem) {
                order.addItem((MenuItem) item, itemCount);
                order.setStaffId(getRestaurant().getSessionStaffId());
                try {
                    getRestaurant().update(order);
                    getRestaurant().update(table);
                    System.out.println("Order has been added.");
                } catch (IOException | Restaurant.FileIDMismatchException e) {
                    System.out.print("Failed to add item to order: " + e.getMessage());
                }
            } else {
                System.out.println("Error. Order not added.");
            }

        }
    }

    private void removeOrder() {
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
                    try {
                        getRestaurant().remove(o);
                        table.clear();
                        getRestaurant().update(table);
                        System.out.println("Order has been voided.");
                        return;
                    } catch (IOException | Restaurant.FileIDMismatchException e) {
                        System.out.print("Failed to remove order: " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("Failed to remove order.");
    }

    private void printBill() {
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

                    getCs().printTitle("Invoice for Table " + table.getId(), true);
                    getCs().printColumns(Collections.singletonList(invoice.get(0)), false, false, false, false , true);
                    getCs().printDivider(' ');

                    invoice.remove(0);
                    invoice.add(0, "QTY // ITEM DESCRIPTION // TOTAL");
                    getCs().printColumns(invoice, false, false, false, false, false);
                    getCs().printDivider('-');

                    invoice = new ArrayList<>(Collections.singletonList(total));
                    invoice.add(0, "TOTAL AMOUNT DESCRIPTION // TOTAL");
                    getCs().printColumns(invoice, false, false, false, false , false);
                    getCs().printDivider('=');
                    o.toPrintString();

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
}
