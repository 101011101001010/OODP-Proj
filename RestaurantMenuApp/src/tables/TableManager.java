package tables;


import client.DataManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import menu.MenuItem;
import order.Order;
import tools.FileIO;
import tools.Log;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableManager extends DataManager {

    public TableManager(Restaurant restaurant) {
        super(restaurant);
    }

    private void createNewTable(int id) {
        getRestaurant().save(new Table(id, id / 10));
    }

    @Override
    public void init() throws IOException {
        getRestaurant().registerClass(Table.class, DataType.TABLE);

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

                        RestaurantData table = getRestaurant().getDataFromId(DataType.TABLE, tableId);

                        if (table instanceof Table) {
                            ((Table) table).attachOrder(order);
                            getRestaurant().update(table);
                        }
                    }

                    getRestaurant().load(order);
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
    }

    private List<String> getTableDisplayList(int sortOption) {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.TABLE);
        List<Table> tableList = dataList.stream().filter(table -> table instanceof Table).map(table -> (Table) table).collect(Collectors.toList());

        if (sortOption > 1) {
            tableList.sort((sortOption == 3)? Comparator.comparing(Table::isReserved) : Comparator.comparing(Table::isOccupied));
        } else {
            tableList.sort(Comparator.comparingInt(RestaurantData::getId));
        }

        return tableList.stream().map(Table::toDisplayString).collect(Collectors.toList());
    }

    private List<String> getOrderDisplayList() {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.ORDER);
        return dataList.stream().filter(order -> order instanceof Order).map(RestaurantData::toDisplayString).collect(Collectors.toList());
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
                "View active orders",
                "Create new order",
                "Manage orders",
                "Print bill"
        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[] {
                this::viewTable,
                this::viewOrder,
                this::createNewOrder,
                this::f,
                this::f
        };
    }

    private void f() {
        Log.notice("Not available. Code re-writing in progress.");
    }

    private void viewTable() {
        int sortOption = 1;
        final String title = "Table Status";
        final List<String> sortOptions = Arrays.asList("Sort by ID", "Sort by occupancy", "Sort by reservation");
        final List<String> footerOptions = Collections.singletonList("Go back");
        final List<String> options = getCs().formatChoiceList(sortOptions, footerOptions);
        List<String> displayList;

        do {
            displayList = getTableDisplayList(sortOption);
            int itemCount = displayList.size();
            int startIndex = (int) Math.ceil(1.0 * displayList.size() / 2);

            for (int index = startIndex; index < displayList.size(); index++) {
                displayList.set(index - startIndex, displayList.get(index - startIndex) + " // " + displayList.get(index));
            }

            for (int index = displayList.size() - 1; index >= startIndex; index--) {
                displayList.remove(index);
            }

            final String headers = (itemCount > 1)? "ID // Occupied // Reserved // ID // Occupied // Reserved" : "ID // Occupied // Reserved";
            getCs().printTable(title, headers, displayList, true);
            getCs().printTable("", "Command / Sort Option", options, true);
        } while ((sortOption = getCs().getInt("Select a sort option", (1 - footerOptions.size()), sortOptions.size())) != 0);
    }

    private void viewOrder() {
        final String title = "Active Orders";
        String headers = "Table // Order ID // Staff ID // Order Details";
        List<String> displayList = getOrderDisplayList();

        if (displayList.size() == 0) {
            headers = "";
            displayList = new ArrayList<>(Collections.singletonList("There is no active order."));
        }

        getCs().printTable(title, headers, displayList, true);
        getCs().getInt("Enter 0 to go back", 0, 0);
    }

    private void createNewOrder() {
        int pax = getCs().getInt("Enter number of pax", 1, 10);
        Table table = getAvailableTable(pax);

        if (table == null) {
            System.out.println("There is no table available for " + pax + " " + ((pax == 1)? "person" : "people") + ".");
            return;
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String orderId = LocalDateTime.now().format(format);
        Order order = table.attachOrder(orderId, getRestaurant().getSessionStaffId());

        if (getRestaurant().save(order) && getRestaurant().update(table)) {
            System.out.println("Order " + orderId + " has been created successfully.");
        } else {
            System.out.println("Failed to create order.");
        }
    }
}