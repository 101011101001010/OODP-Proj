package Classes;


import client.BaseManager;
import client.enums.AssetType;
import client.enums.Op;
import tools.FileIO;
import tools.Pair;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TableManager extends BaseManager {

    public static ArrayList<Table> tableList = new ArrayList<>();
    private static Map<Integer, Integer> map;

    public TableManager() {
        map = new HashMap<>();


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
    }

    public void choices(Scanner s) {
        System.out.println("1. Show Table");
        System.out.println("1. Set Table");
        System.out.println("1. Set reservation");
        System.out.println("");

    }

    public static void setOccupied(int tableID, int orderID) {
        int index = map.get(tableID);
        tableList.get(index).setOrderID(orderID);
        tableList.get(index).setOccupied(1);

    }

    public static void setReserved(int tableID, int pax) {
        int index = map.get(tableID);
        tableList.get(index).setOccupied(-1);
        tableList.get(index).setPax(pax);
    }

    public static void clear(int tableID) {
        int index = map.get(tableID);
        tableList.get(index).setOccupied(0);
        tableList.get(index).setOrderID(-1);
    }

    public void checkVacancy() {
        for (Table t : tableList) {
            System.out.println(t.toString());
        }
    }

    public int getOrderID(int tableID){
        return tableList.get(map.get(tableID)).getOrderID();
    }
    public boolean checkOccupied(int tableID){
        if (tableList.get(map.get(tableID)).isOccupied()!=0)
            return true;
        else
            return false;

    }

    //
    //
    //

    public void addReservation(Scanner s) {
        LocalDateTime combineDate;
        LocalDateTime now = LocalDateTime.now();
        String name, date;
        int pax = 0, index=-1, contact;
        boolean done = false;
        String test1 = "0204201909:30pm";
        String test2 = "0204201910:30pm";
        String test3 = "0204201909:45pm";
        String test4 = "0204201909:15pm";
        String test5 = "0204201907:30pm";
        tableList.get(25).isReserved(12345, "A", formatting(test1), 10);
        tableList.get(26).isReserved(67890, "B", formatting(test2), 10);
        tableList.get(27).isReserved(54321, "C", formatting(test3), 10);
        tableList.get(28).isReserved(9876, "D", formatting(test4), 10);
        tableList.get(29).isReserved(24680, "E", formatting(test5), 10);
        showReservation();
        do{
            System.out.println("Enter reserving date(ddmmyyyy) : ");
            date = s.next();
            System.out.println("Enter reserving time(hh:mm(am/pm))");
            date = date + s.next();
            combineDate = formatting(date);
            if(combineDate.minus(Period.ofDays(30)).isAfter(now))
                System.out.println("Reservation can only be made in at most 1 month in advance. Please enter again.");
        }while (combineDate.minus(Period.ofMonths(1)).isAfter(now));

        while(!done) {
            try {
                do {
                    System.out.println("Enter number of people.");

                    pax = s.nextInt();

                    if (pax > 10)
                        System.out.println("Tables are limited to a maximum number of 10 people, please enter again.");
                    else if (pax < 1)
                        System.out.println("Unable to book a table with less than a person, please enter again.");
                    else
                    done = true;
                } while (pax > 10 || pax < 1);

            } catch (Exception e) {
                s.next();
                System.out.println("Please enter a number");
            }
        }
		index = checkReservation(combineDate, pax);

		if(index != -1){
		    System.out.println(index);
			System.out.println("Enter Your Name.");
			name = s.next();
			System.out.println("Enter Your Contact.");
			contact = s.nextInt();
			tableList.get(index).getReservationList().add(new Table.Reservations(contact, name, combineDate, pax));
		}
		else
		    System.out.println("Sorry, Booking Full.");
    }
    public void deleteReservation (Scanner s){
        int index;
        showReservation();
        System.out.println("Enter TableID.");
        index = map.get(s.nextInt());
        tableList.get(index).showReservationList();
        System.out.println("Enter Reservation ID To Delete.");
        tableList.get(index).getReservationList().remove(s.nextInt()-1);
        showReservation();
        System.out.println("Successfully Removed.");
    }
    public int checkReservation(LocalDateTime combineDate, int pax) {
        DateTimeFormatter session = DateTimeFormatter.ofPattern("a");
        for (Table t : tableList) {
            if (pax <= 2 && (t.getTableID() / 10 == 2)) {
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                        return map.get(t.getTableID());
                    }
                }
            }
            if ((pax == 3 || pax == 4) && (t.getTableID() / 10 == 4)) {
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                        return map.get(t.getTableID());
                    }
                }
            }
            if (pax>=5 && pax<=8 && (t.getTableID() / 10 == 8)) {
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                        return map.get(t.getTableID());
                    }
                }
            }
            if (pax>=9 && (t.getTableID() / 10 == 10)) {
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!combineDate.format(session).equals(r.getDate().format(session))) {
                        return map.get(t.getTableID());
                    }
                }
            }
        }
            return -1;
    }

    public void searchReservation (Scanner s){
        int contact;
        boolean check =false;

        System.out.println("Enter contact number to check for reservation.");
        contact = s.nextInt();
        for (Table t : tableList){
            if (t.findReservation(contact)){
                check = true;
                break;
            }
        }
        if (!check)
            System.out.println("No reservation found.");
    }

    public void showReservation (){

        for (Table t : tableList){
            int count = 1;
            if (t.getReservationList().size()!=0){
                System.out.println("For Table " + t.getTableID());
                for (Table.Reservations r : t.getReservationList()){

                    System.out.println(count++ + ". " + r.toStringTwo());
                }
            }
        }
    }
    private LocalDateTime formatting (String datetime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
        return LocalDateTime.parse(datetime, formatter);
    }
    public boolean compareDate (LocalDateTime compare1, LocalDateTime compare2){
        return compare1.toLocalDate().equals(compare2.toLocalDate());
    }

    //
    //
    //

    public Pair<Op, String> init(){
        FileIO f = new FileIO();
        List<String> tableData = f.read(FileIO.FileNames.TABLE_FILE);
        String splitStr = " // ";

        if (tableData==null)
            return  (new Pair<>(Op.FAILED, "Failed to read files."));
        if (!getRestaurant().registerClassToAsset(Table.class, AssetType.TABLE))
            return (new Pair<>(Op.FAILED, "Failed to register class."));
    }

    @Override
    public String[] getMainCLIOptions() {
        return new String[0];
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[0];
    }
}


