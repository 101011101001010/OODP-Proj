package tables;

import core.RestaurantData;
import tools.ConsolePrinter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Table extends RestaurantData {
    private int tableId;
    private int capacity;
    private boolean occupied;
    private boolean reserved;
    private Order order;
    private List<Reservation> reservationList;

    Table(int tableId, int capacity) {
        super(tableId);
        this.tableId = tableId;
        this.capacity = capacity;
        this.occupied = false;
        this.reserved = false;
        this.order = null;
        this.reservationList = new ArrayList<>();
    }

    Table(int tableId, int capacity, boolean occupied, Order order) {
        super(tableId);
        this.tableId = tableId;
        this.capacity = capacity;
        this.occupied = occupied;
        this.reserved = false;
        this.order = order;
        this.reservationList = new ArrayList<>();
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

    boolean isAvailable() {
        return (!occupied && !reserved);
    }

    boolean isLargeEnough(int pax) {
        return (capacity >= pax && capacity <= (pax + 2));
    }

    public void removeNoShow(){
        if (reservationList.size() == 0) {
            return;
        }

        for (int index = 0; index < reservationList.size(); index++) {
            Reservation r = reservationList.get(index);

            if (r.getDate().plusSeconds(30).isBefore(LocalDateTime.now())) {
                reservationList.remove(index);
                index--;
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

    List<Reservation> getReservationList(){
        return reservationList;
    }


    @Override
    public String toFileString() {
        return getId() + " // " + capacity + " // " + occupied;
    }

    @Override
    public String toDisplayString() {
        return getId() + " // " + occupied + " // " + reserved;
    }


    void addReservation(int contact, String name, LocalDateTime date, int pax) {
        reservationList.add(new Reservation(tableId, contact, name, date, pax));
    }

    void deleteReservation(Reservation r) {
        reservationList.remove(r);
    }

    public class Reservation extends RestaurantData {
        private int contact;
        private String name;
        private LocalDateTime date;
        private int pax;

        Reservation(int id, int contact, String name, LocalDateTime date, int pax) {
            super(id);
            this.contact = contact;
            this.name = name;
            this.date = date;
            this.pax = pax;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public String getDateStr() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return date.format(format);
        }

        public String getName() {
            return name;
        }

        int getContact() {
            return contact;
        }

        int getPax() {
            return pax;
        }

        public String toFileString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return getId() + " // " + contact + " // " + name + " // " + date.format(format) + " // " + pax;
        }

        @Override
        public String toDisplayString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getId() + " // " + name + " // " + contact + " // " + date.format(format) + " // " + pax;
        }
    }

    List<Reservation> findReservation(int contact){
        List<Reservation> ret = new ArrayList<>();
        for (Reservation r : reservationList){
            if(r.getContact() == contact){
                ret.add(r);
            }
        }

        ret.sort(Comparator.comparing(Reservation::getDate));
        return ret;
    }
}