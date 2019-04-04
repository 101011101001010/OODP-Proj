package Classes;


import client.BaseManager;
import client.Restaurant;
import client.RestaurantAsset;
import enums.AssetType;
import enums.FileName;
import order.Order;
import tools.FileIO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TableManager extends BaseManager {
    private AssetType assetType = AssetType.TABLE;
    private FileName fileName = FileName.TABLE;
    private static Map<Integer, Integer> map;

    public TableManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws ManagerInitFailedException {
        map = new HashMap<>();

        FileIO f = new FileIO();
        List<String> tableData;
        List<String> reservationData;

        try {
            tableData = f.read(fileName);
            reservationData = f.read(FileName.ORDER);
        } catch (IOException e) {
            throw (new ManagerInitFailedException(this, "Unable to load tables or orders from file: " + e.getMessage()));
        }

        if (!getRestaurant().mapClassToAssetType(Table.class, AssetType.TABLE)) {
            throw (new ManagerInitFailedException(this, "Failed to register class and asset to restaurant."));
        }

        if (tableData.size() == 0) {
            int cap = 10;

            for (int prefix = 2; prefix <= 10; prefix += 2) {
                for (int seat = 0; seat < cap; seat++) {
                    try {
                        int id = prefix * 10 + seat;
                        getRestaurant().addFromFile(new Table(id, prefix));
                    } catch (Restaurant.AssetNotRegisteredException | IOException e) {
                        throw (new ManagerInitFailedException(this, e.getMessage()));
                    }
                }

                if (prefix == 4) {
                    prefix += 2;
                    cap = 5;
                }
            }

            return;
        }

        // will never happen (for now)
        String splitStr = " // ";
        for (String data : tableData) {
            String[] datas = data.split(splitStr);

            if (datas.length != 4) {
                continue;
            }

            int id;
            try {
                id = Integer.parseInt(datas[0]);
            } catch (NumberFormatException e) {
                throw (new ManagerInitFailedException(this, e.getMessage()));
            }

            Order order = null;
            try {
                for (RestaurantAsset o : getRestaurant().getAsset(AssetType.ORDER)) {
                    if (o.getId() == id && (o instanceof Order)) {
                        order = (Order) o;
                    }
                }
            } catch (Restaurant.AssetNotRegisteredException ignored) {}

            Table table = new Table(id, Integer.parseInt(datas[1]), Integer.parseInt(datas[2]), order);
            for (String rStr : reservationData) {
                String[] rDatas = rStr.split(splitStr);
                int rId;

                try {
                    rId = Integer.parseInt(rDatas[0]);
                } catch (NumberFormatException e) {
                    throw (new ManagerInitFailedException(this, e.getMessage()));
                }

                if (rId == id) {
                    //table.addReservation(Integer.parseInt(rDatas[1]), );
                }
            }

            try {
                getRestaurant().addFromFile(table);
            } catch (Restaurant.AssetNotRegisteredException | IOException e) {
                throw (new ManagerInitFailedException(this, e.getMessage()));
            }
        }

        /*
        int id = 20;
        for (int i = 0; i < 30; i++) {

            tableList.add(new Table(id));
            map.put(id++, i);
            if (id == 30)
                id = 40;
            if (id == 50)
                id = 80;
            if (id == 85)
                id = 100;
        }
        Table table = new Table(1);
        getRestaurant().add(table);
        getRestaurant().getAsset(AssetType.TABLE);
        getRestaurant().remove(table);
        return null;
        */
    }

    public void setOccupied(int tableId, int orderId, int staffId) throws Restaurant.AssetNotRegisteredException {
        Table table = (Table) getRestaurant().getAssetFromId(AssetType.TABLE, tableId);
        table.attachOrder(orderId, staffId);
        /*
        int index = map.get(tableID);
        tableList.get(index).setOrderID(orderID);
        tableList.get(index).setOccupied(1);
        */
    }

    public void checkVacancy() throws Restaurant.AssetNotRegisteredException {
        for (RestaurantAsset t : getRestaurant().getAsset(AssetType.TABLE)) {
            System.out.println(t.toString());
        }
    }

    //
    //
    //

    public void addReservation() {
        LocalDateTime combineDate;
        LocalDateTime now = LocalDateTime.now();
        String name, date, time;
        int pax = 0, contact;
        Table table;
        boolean done = false;
        String test1 = "0204201909:30PM";
        String test2 = "0204201910:30PM";
        String test3 = "0204201909:45PM";
        String test4 = "0204201909:15PM";
        String test5 = "0204201907:30PM";

        try {
            ((Table) getRestaurant().getAsset(AssetType.TABLE).get(25)).addReservation(12345, "A", formatting(test1), 10);
            ((Table) getRestaurant().getAsset(AssetType.TABLE).get(26)).addReservation(67890, "B", formatting(test2), 10);
            ((Table) getRestaurant().getAsset(AssetType.TABLE).get(27)).addReservation(54321, "C", formatting(test3), 10);
            ((Table) getRestaurant().getAsset(AssetType.TABLE).get(28)).addReservation(9876, "D", formatting(test4), 10);
            ((Table) getRestaurant().getAsset(AssetType.TABLE).get(29)).addReservation(24680, "E", formatting(test5), 10);
            showReservation();
        } catch (Restaurant.AssetNotRegisteredException e) {
            System.out.println(e.getMessage());
            return;
        }

        do {
            date = getCs().getString("Enter reserving date (ddmmyyyy)");
            time = getCs().getString("Enter reserving time (hh:mm(am/pm))");
            date = date + time;
            combineDate = formatting(date);
            if (combineDate.minus(Period.ofDays(30)).isAfter(now))
                System.out.println("Reservation can only be made in at most 1 month in advance. Please enter again.");
        } while (combineDate.minus(Period.ofMonths(1)).isAfter(now));

        pax = getCs().getInt("Enter number of people", 10, 1);

        try {
            table = checkReservation(combineDate, pax);
        } catch (Restaurant.AssetNotRegisteredException e) {
            System.out.println(e.getMessage());
            return;
        }

        if (table != null) {
            System.out.println("Table ID: " + table.getId());
            name = getCs().getString("Enter Your Name");
            contact = getCs().getInt("Enter Your Contact", 99999999, 10000000);
            table.addReservation(contact, name, combineDate, pax);
        } else {
            System.out.println("Sorry, Booking Full.");
        }
    }

    public void deleteReservation(Scanner s) {
        int index;
        Table table;
        showReservation();
        int id = getCs().getInt("Enter TableID");
        try {
            table = (Table) getRestaurant().getAssetFromId(AssetType.TABLE, id);
        } catch (Restaurant.AssetNotRegisteredException e) {
            System.out.println(e.getMessage());
            return;
        }

        table.showReservationList();
        int remove = getCs().getInt("Enter Reservation ID To Delete");
        table.getReservationList().remove(remove - 1);
        showReservation();
        System.out.println("Successfully Removed.");
    }

    public Table checkReservation(LocalDateTime combineDate, int pax) throws Restaurant.AssetNotRegisteredException {
        DateTimeFormatter session = DateTimeFormatter.ofPattern("a");

        for (RestaurantAsset t : getRestaurant().getAsset(AssetType.TABLE)) {
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

    public void searchReservation(Scanner s) throws Restaurant.AssetNotRegisteredException {
        int contact;
        boolean check = false;

        System.out.println("Enter contact number to check for reservation.");
        contact = s.nextInt();
        for (RestaurantAsset t : getRestaurant().getAsset(AssetType.TABLE)) {
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
        try {
            for (RestaurantAsset t : getRestaurant().getAsset(AssetType.TABLE)) {
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
        } catch (Restaurant.AssetNotRegisteredException e) {
            System.out.println(e.getMessage());
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

    @Override
    public String[] getMainCLIOptions() {
        return new String[]{
                "Show table",
                "Set reservation"
        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[] {
                this::showReservation,
                this::addReservation
        };
    }
}