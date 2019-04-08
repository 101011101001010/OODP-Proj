package tables;

import core.RestaurantData;
import tools.FileIO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class Table extends RestaurantData {
    private int tableId;
    private int capacity;
    private boolean occupied;
    private boolean reserved;
    private Order order;
    private Map<String, Reservation> reservationMap;

    Table(int tableId, int capacity) {
        super(tableId);
        this.tableId = tableId;
        this.capacity = capacity;
        this.occupied = false;
        this.reserved = false;
        this.order = null;
        this.reservationMap = new HashMap<>();
    }

    Table(int tableId, int capacity, boolean occupied, Order order) {
        super(tableId);
        this.tableId = tableId;
        this.capacity = capacity;
        this.occupied = occupied;
        this.reserved = false;
        this.order = order;
        this.reservationMap = new HashMap<>();
    }

    int getCapacity() {
        return capacity;
    }

    boolean isOccupied() {
        return occupied;
    }

    boolean isReserved() {
        return reserved;
    }

    boolean isAvailable(LocalDateTime dateTime) {
        DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
        String dateKey = dateTime.format(format);

        return (!occupied && !reservationMap.containsKey(dateKey));
    }

    boolean isLargeEnough(int pax) {
        return (capacity >= pax && capacity <= (pax + 2));
    }

    void removeNoShow(){
        if (reservationMap.size() == 0) {
            return;
        }

        for (Reservation r : reservationMap.values()) {
            if (r.isExpired()) {
                deleteReservation(r);
            }
        }
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    Order attachOrder(String orderId, int staffId) {
        occupied = true;
        reserved = false;
        order = new Order(getId(), orderId, staffId);
        return order;
    }

    void attachOrder(Order order) {
        occupied = true;
        reserved = false;
        this.order = order;
    }

    Order getOrder() {
        return order;
    }

    int getTableId(){
        return this.tableId;
    }

    void clear() {
        occupied = false;
        order = null;
    }

    Map<String, Reservation> getReservationMap(){
        return reservationMap;
    }


    @Override
    public String toFileString() {
        String head = getId() + " // " + capacity + " // " + occupied + " // " + reserved;
        StringBuilder sb = new StringBuilder(head);

        if (reservationMap.size() > 0) {
            sb.append(" // ");
        }

        for (Reservation r : reservationMap.values()) {
            String s = r.toFileString();
            sb.append(s);
            sb.append("--");
        }

        return sb.toString();
    }

    @Override
    public String toDisplayString() {
        return getId() + " // " + occupied + " // " + reserved;
    }

    boolean checkIsReserved(LocalDateTime date) {
        DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
        String dateKey = date.format(format);

        return reservationMap.containsKey(dateKey);
    }

    boolean addReservation(int contact, String name, LocalDateTime date, int pax) {
        Reservation r = new Reservation(contact, name, date, pax);
        String dateKey = r.getSessionString();

        if (reservationMap.containsKey(dateKey)) {
            return false;
        }

        reservationMap.put(dateKey, r);
        return true;
    }

    void deleteReservation(Reservation r) {
        String dateKey = r.getSessionString();
        reservationMap.remove(dateKey);
    }

    List<Reservation> findReservation(int contact){
        List<Reservation> ret = new ArrayList<>();
        for (Reservation r : reservationMap.values()){
            if(r.getContact() == contact){
                ret.add(r);
            }
        }

        ret.sort(Comparator.comparing(Reservation::getDate));
        return ret;
    }

    class Reservation {
        private int contact;
        private String name;
        private LocalDateTime date;
        private int pax;

        private Reservation(int contact, String name, LocalDateTime date, int pax) {
            this.contact = contact;
            this.name = name;
            this.date = date;
            this.pax = pax;
        }

        LocalDateTime getDate() {
            return date;
        }

        String getDateStr() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return date.format(format);
        }

        String getName() {
            return name;
        }

        public int getTableId() {
            return getId();
        }

        int getContact() {
            return contact;
        }

        int getPax() {
            return pax;
        }

        public String toFileString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return contact + "," + name + "," + date.format(format) + "," + pax;
        }

        public String toDisplayString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getId() + " // " + name + " // " + contact + " // " + date.format(format) + " // " + pax;
        }

        public boolean isExpired() {
            return date.plusSeconds(30).isBefore(LocalDateTime.now());
        }

        public boolean isCurrentSession() {
            DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
            return getSessionString().equals(LocalDateTime.now().format(format));
        }

        public boolean isArrivalWindow() {
            return (isCurrentSession() && !isExpired());
        }

        public String getSessionString() {
            DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
            return date.format(format);
        }
    }
}