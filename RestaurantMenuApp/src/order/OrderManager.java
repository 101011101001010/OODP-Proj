package order;

import client.DataManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import menu.MenuItem;
import tables.Table;
import tools.FileIO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderManager extends DataManager {

    public OrderManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws IOException {
        getRestaurant().registerClass(Order.class, DataType.ORDER);
        List<String[]> fileData = (new FileIO()).read(DataType.ORDER).stream().map(data -> data.split(" // ")).filter(data -> (data.length >= 3 && data.length <= 4)).collect(Collectors.toList());

        for (String[] data : fileData) {
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
            } catch (NumberFormatException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Invalid file data: " + e.getMessage());
            }
        }
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

    @Override
    public String[] getMainCLIOptions() {
        return new String[]{

        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[]{
        };
    }

    private void updateOrder() {
        /*
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

            DataType dataType = (type == 1) ? DataType.ALA_CARTE_ITEM : DataType.PROMO_PACKAGE;
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
                order.updateOrder((MenuItem) item, itemCount);
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
        */
    }

    private void voidOrder() {
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
        */
        System.out.println("Failed to remove order.");
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
