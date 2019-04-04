package tables;

import client.RestaurantData;
import order.Order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Table extends RestaurantData {
    // private int tableID;
    private int capacity;
    private boolean occupied;
    private Order order;
    private List<Reservation> reservationList = new ArrayList<>();

    public Table(int tableId, int capacity) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = false;
        this.order = null;
        this.reservationList = new ArrayList<>();
    }

    public Table(int tableId, int capacity, boolean occupied, Order order) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = occupied;
        this.order = order;
        this.reservationList = reservationList;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Order attachOrder(String orderId, int staffId) {
        occupied = true;
        order = new Order(getId(), orderId, staffId);
        return order;
    }

    public void attachOrder(Order order) {
        occupied = true;
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public boolean hasOrder() {
        return (order != null);
    }

    public void clear() {
        occupied = false;
        order = null;
    }

    public List<Reservation> getReservationList() {
        return reservationList;
    }

    public void showReservationList() {
        int index = 1;
        System.out.println("For Table " + getId());
        for (Reservation r : reservationList) {
            System.out.println(index++ + ". " + r.toStringTwo());
        }
    }

    public boolean findReservation(int contact) {
        for (Reservation r : reservationList) {
            if (r.getContact() == contact) {
                System.out.println("Reservation Found At Table " + getId());
                System.out.println(r.toStringTwo());
                return true;
            }
        }

        return false;
    }

    @Override
    public String toPrintString() {
        return getId() + " // " + capacity + " // " + occupied;
    }

    @Override
    public String toTableString() {
        return getId() + " // " + capacity + " // " + occupied;
    }


    public void addReservation(int contact, String name, LocalDateTime date, int pax) {
        reservationList.add(new Reservation(contact, name, date, pax));
    }

    public class Reservation {
        private int contact;
        private String name;
        private LocalDateTime date;
        private int pax;

        public Reservation(int contact, String name, LocalDateTime date, int pax) {
            this.contact = contact;
            this.name = name;
            this.date = date;
            this.pax = pax;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public String getName() {
            return name;
        }

        public int getContact() {
            return contact;
        }

        public int getPax() {
            return pax;
        }

        public String toStringTwo() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return "Reservation date :" + date.format(format) + ", Name :" + getName() + ", Contact :" + getContact() + ", Pax :" + getPax();
        }

        public String toPrintString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return getId() + " // " + contact + " // " + name + " // " + date.format(format) + " // " + pax;
        }
    }
}