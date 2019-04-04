package tables;


import client.BaseManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import order.Order;
import tools.FileIO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TableManager extends BaseManager {
    private DataType dataType = DataType.TABLE;

    public TableManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws ManagerInitFailedException {
        try {
            getRestaurant().registerClass(Table.class, DataType.TABLE);
        } catch (Restaurant.ClassNotRegisteredException e) {
            throw (new ManagerInitFailedException(this, "Class registration failed: " + e.getMessage()));
        }

        FileIO f = new FileIO();
        List<String> tableData;
        List<String> reservationData;

        try {
            tableData = f.read(dataType);
            reservationData = f.read(DataType.RESERVATION);
        } catch (IOException e) {
            throw (new ManagerInitFailedException(this, "Unable to load tables or orders from file: " + e.getMessage()));
        }

        if (tableData.size() == 0) {
            int cap = 10;
            for (int prefix = 2; prefix <= 10; prefix += 2) {
                for (int seat = 0; seat < cap; seat++) {
                    try {
                        int id = prefix * 10 + seat;
                        getRestaurant().save(new Table(id, prefix));
                    } catch (IOException e) {
                        throw (new ManagerInitFailedException(this, e.getMessage()));
                    }
                }

                if (prefix == 4) {
                    prefix += 2;
                    cap = 5;
                }
            }
        } else {
            for (String data : tableData) {
                String[] datas = data.split(" // ");

                if (datas.length != 3) {
                    continue;
                }

                int id;
                try {
                    id = Integer.parseInt(datas[0]);
                } catch (NumberFormatException e) {
                    throw (new ManagerInitFailedException(this, e.getMessage()));
                }

                Table table = new Table(id, Integer.parseInt(datas[1]), Boolean.parseBoolean(datas[2]), null);
                Order order;
                for (RestaurantData o : getRestaurant().getData(DataType.ORDER)) {
                    if (o.getId() == id) {
                        order = (Order) o;
                        table.attachOrder(order);
                    }
                }

                for (String rStr : reservationData) {
                    String[] rDatas = rStr.split(" // ");
                    int rId;

                    try {
                        rId = Integer.parseInt(rDatas[0]);
                    } catch (NumberFormatException e) {
                        throw (new ManagerInitFailedException(this, e.getMessage()));
                    }

                    if (rId == id) {
                        try {
                            table.addReservation(Integer.parseInt(rDatas[1]), rDatas[2], formatting(rDatas[3]), Integer.parseInt(rDatas[4]));
                        } catch (NumberFormatException e) {
                            throw (new ManagerInitFailedException(this, e.getMessage()));
                        }
                    }
                }

                getRestaurant().load(table);
            }
        }
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

    public Table getAvailableTable(LocalDateTime dateTime, int pax) {
        for (RestaurantData o : getRestaurant().getData(dataType)) {
            if ((o instanceof Table) && ((Table) o).getCapacity() >= pax && ((Table) o).getCapacity() <= (pax + 2)) {
                for (Table.Reservation reservation : ((Table) o).getReservationList()) {

                }
            }
        }

        return null;
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
        int id = getCs().getInt("Enter TableID");
        Table table = (Table) getRestaurant().getDataFromId(DataType.TABLE, id);
        table.showReservationList();
        int remove = getCs().getInt("Enter Reservation ID To Delete");
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

    //
    //
    //

    private List<String> getDisplay(int sortOption) {
        List<? extends RestaurantData> masterList = new ArrayList<>(getRestaurant().getData(dataType));
        List<String> ret = new ArrayList<>();
        int totalSize = masterList.size();
        if (totalSize == 0) {
            ret.add("The restaurant apparently has no tables at all.");
            return ret;
        }

        masterList.sort((item1, item2) -> {
            switch (sortOption) {
                case 2: return Integer.compare(((Table) item1).getCapacity(), ((Table) item2).getCapacity());
                case 3: return Boolean.compare(((Table) item1).isOccupied(), ((Table) item2).isOccupied());
            }

            return Integer.compare(item1.getId(), item2.getId());
        });

        for (int index = 0; index < (totalSize / 2); index++) {
            RestaurantData data = masterList.get(index);
            if (data instanceof Table) {
                ret.add(data.toTableString());
            }
        }

        for (int index = (totalSize / 2); index < totalSize; index++) {
            RestaurantData data = masterList.get(index);
            if (data instanceof Table) {
                ret.set(index - (totalSize / 2), ret.get(index - (totalSize / 2)) + " // " + data.toTableString());
            }
        }

        ret.add(0, "Table ID // Capacity // Table Occupancy // Table ID // Capacity // Table Occupancy");
        return ret;
    }

    @Override
    public String[] getMainCLIOptions() {
        return new String[]{
                "View table status"
        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[] {
                this::viewTable
        };
    }

    public void viewTable() {
        int choice = 1;
        List<String> displayList;

        do {
            displayList = getDisplay(choice);
            getCs().printDisplayTable("Table Status", displayList, true, true);
        } while ((choice = getCs().printChoices("Select a sort option", "Command // Function", Arrays.asList("Sort by ID", "Sort by capacity", "Sort by occupancy"), new String[] {"Go back"})) != -1);
    }
}