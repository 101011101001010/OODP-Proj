package Classes;

import client.RestaurantAsset;
import order.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Table extends RestaurantAsset {
    // private int tableID;
    private int capacity;
    private int occupied;
    private Order order;
    private List<Reservation> reservationList = new ArrayList<>();

    public Table(int tableId, int capacity) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = 0;
        this.order = null;
        this.reservationList = new ArrayList<>();
    }

    public Table(int tableId, int capacity, int occupied, Order order) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = occupied;
        this.order = order;
        this.reservationList = reservationList;
    }

    public int getCapacity() {
        return capacity;
    }

    public int isOccupied() {
        return occupied;
    }

    public boolean checkOccupied() {
        if (occupied != 0) {
            return true;
        }

        return false;
    }

    public Order attachOrder(int orderId, int staffId) {
        occupied = 1;
        order = new Order(getId(), orderId, staffId);
        return order;
    }

    public Order getOrder() {
        return order;
    }

    public boolean hasOrder() {
        return (order != null);
    }

    public void clear() {
        occupied = 0;
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

    public String toString() {
        String check = "not occupied.";

        if (isOccupied() == -1) {
            check = "reserved.";
        }

        if (isOccupied() == 1) {
            check = "occupied.";
        }

        return "Table " + getId() + " is " + check;
    }

    @Override
    public String toPrintString() {
        return getId() + " // " + capacity + " // " + occupied;
    }

    @Override
    public String toTableString() {
        return null;
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
            //DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
            //LocalDateTime date = LocalDateTime.parse(getDate(),format);
            return "Reservation date :" + date.toLocalDate() + " " + date.toLocalTime() +
                    ", Name :" + getName() + ", Contact :" + getContact() + ", Pax :" + getPax();
        }

        public String toPrintString() {
            return getId() + " // " + contact + " // " + name + " // " + date + " // " + pax;
        }
    }
}