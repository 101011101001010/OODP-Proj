package Classes;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class TableManager {

    public static ArrayList<Table> tableList = new ArrayList<>();
    private static Map<Integer, Integer> map;
    private LocalDate now = LocalDate.now();
    private LocalTime amOpeningHour = formattingtime("11:00am");
    private LocalTime amClosingHour = formattingtime("03:00pm");
    private LocalTime pmOpeningHour = formattingtime("06:00pm");
    private LocalTime pmClosingHour = formattingtime("10:00pm");
    private DateTimeFormatter tf = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("hh:mma").toFormatter(Locale.ENGLISH);

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
        String test1 = "0804201911:18pm";
        String test2 = "1004201910:30pm";
        String test3 = "1004201909:45pm";
        String test4 = "0804201909:15pm";
        String test5 = "1004201907:30pm";
        tableList.get(25).isReserved(12345, "A", formattingdatetime(test1), 10);
        tableList.get(26).isReserved(67890, "B", formattingdatetime(test2), 10);
        tableList.get(27).isReserved(54321, "C", formattingdatetime(test3), 10);
        tableList.get(28).isReserved(9876, "D", formattingdatetime(test4), 10);
        tableList.get(29).isReserved(24680, "E", formattingdatetime(test5), 10);

        for (Table t : tableList){
            if (t.getReservationList().size()!=0){
                for (int i = 0; i<t.getReservationList().size();i++){
                    if (t.getReservationList().get(i).getDate().toLocalDate().isBefore(now))
                        t.getReservationList().remove(i);
                    if (t.getReservationList().get(i).getDate().toLocalDate().isEqual(now)){
                        t.setOccupied(-1);
                    }
                }
            }
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
            t.checkNoShow();
            System.out.println(t.toString());
        }
        showReservation();
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
        String name, date, time, session;
        int pax=0, index, contact;
        boolean done = false;

        showReservation();
        do{
            System.out.println("Enter reserving date(ddmmyyyy) : ");
            date = s.next();
            if((formattingdate(date)).minus(Period.ofMonths(1)).isAfter(now))
                System.out.println("Reservation date can only be made at most 1 month in advance. " +
                        "Please enter again.");
            if((formattingdate(date)).isBefore(now) || (formattingdate(date)).isEqual(now))
                System.out.println("Reservation date can only be made from tomorrow onwards. " +
                        "Please enter again.");
        }while ((formattingdate(date)).minus(Period.ofMonths(1)).isAfter(now)
                || (formattingdate(date)).isBefore(now)
                || (formattingdate(date)).isEqual(now));

        do{
            System.out.println("Enter reserving time(hh:mm(am/pm))");
            time = s.next();
            if(formattingtime(time).isBefore(amOpeningHour))
                System.out.println("We open at " + amOpeningHour.format(tf) + ". Please enter again.");
            else if(formattingtime(time).isAfter(amClosingHour.minusHours(1)) &&
                    formattingtime(time).isBefore(pmOpeningHour))
                System.out.println("Our last order is at " + amClosingHour.format(tf) +
                        " and we will open at " + pmOpeningHour.format(tf) +
                        ". Please enter again. Please enter again.");
            else if (formattingtime(time).isAfter(pmClosingHour.minusHours(1)))
                System.out.println("Our last order is at " + pmClosingHour.minusHours(1).format(tf) +
                        " and we closes at " + pmClosingHour.format(tf) + ". Please enter again.");
        }while (formattingtime(time).isBefore(amOpeningHour)
                ||  formattingtime(time).isAfter(pmClosingHour.minusHours(1))
                ||  (formattingtime(time).isAfter(amClosingHour.minusHours(1)) &&
                formattingtime(time).isBefore(pmOpeningHour)));

        combineDate = formattingdatetime(date+time);
        if (formattingtime(time).isBefore(amClosingHour))
            session = "am";
        else
            session = "pm";
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
        index = checkReservation(combineDate, pax, session);

        if(index != -1){
            System.out.println("Enter Your Name.");
            name = s.next();
            System.out.println("Enter Your Contact.");
            contact = s.nextInt();
            tableList.get(index).getReservationList().add(new Table.Reservations(contact, name, combineDate, pax, session));
            Collections.sort(tableList.get(index).getReservationList());
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
    public int checkReservation(LocalDateTime combineDate, int pax, String session) {
        for (Table t : tableList) {
            if (pax <= 2 && (t.getTableID() / 10 == 2)) {
                if (t.getReservationList().size()==0)
                    return map.get(t.getTableID());
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!session.equals(r.getSession())) {
                        return map.get(t.getTableID());
                    }
                }
            }
            if ((pax == 3 || pax == 4) && (t.getTableID() / 10 == 4)) {
                if (t.getReservationList().size()==0)
                    return map.get(t.getTableID());
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!session.equals(r.getSession())) {
                        return map.get(t.getTableID());
                    }
                }
            }
            if (pax>=5 && pax<=8 && (t.getTableID() / 10 == 8)) {
                if (t.getReservationList().size()==0)
                    return map.get(t.getTableID());
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!session.equals(r.getSession())) {
                        return map.get(t.getTableID());
                    }
                }
            }
            if (pax>=9 && (t.getTableID() / 10 == 10)) {
                if (t.getReservationList().size()==0)
                    return map.get(t.getTableID());
                for (Table.Reservations r : t.getReservationList()) {
                    if (!compareDate(combineDate, r.getDate()))
                        return map.get(t.getTableID());
                    else if (!session.equals(r.getSession())) {
                        return map.get(t.getTableID());
                    }
                }
            }
        }
        return -1;
    }

    public void findReservation (Scanner s){
        int contact;
        boolean check =false;

        System.out.println("Enter contact number to check for reservation.");
        contact = s.nextInt();
        for (Table t : tableList){
            if (t.findReservation(contact)){
                check = true;
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

    private LocalDate formattingdate (String date) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy").toFormatter(Locale.ENGLISH);
        return LocalDate.parse(date, formatter);
    }
    private LocalTime formattingtime (String time) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("hh:mma").toFormatter(Locale.ENGLISH);
        return LocalTime.parse(time, formatter);
    }
    private LocalDateTime formattingdatetime (String datetime) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyyhh:mma").toFormatter(Locale.ENGLISH);
        return LocalDateTime.parse(datetime, formatter);
    }
    public boolean compareDate (LocalDateTime compare1, LocalDateTime compare2){
        return compare1.toLocalDate().equals(compare2.toLocalDate());
    }


    //
    //
    //

    /*public Pair<Op, String> init(){
        FileIO f = new FileIO();
        List<String> tableData = f.read(FileIO.FileNames.TABLE_FILE);
        String splitStr = " // ";

        if (tableData==null)
            return  (new Pair<>(Op.FAILED, "Failed to read files."));
        if (!getRestaurant().registerClassToAsset(Table.class, AssetType.TABLE))
            return (new Pair<>(Op.FAILED, "Failed to register class."))
    }*/
}


