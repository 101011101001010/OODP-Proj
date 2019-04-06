package tables;

import client.RestaurantData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    Order attachOrder(String orderId, int staffId) {
        occupied = true;
        order = new Order(getId(), orderId, staffId);
        return order;
    }

    void attachOrder(Order order) {
        occupied = true;
        this.order = order;
    }

    Order getOrder() {
        return order;
    }

    void clear() {
        occupied = false;
        order = null;
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
        reservationList.add(new Reservation(contact, name, date, pax));
    }

    public class Reservation extends RestaurantData {
        private int contact;
        private String name;
        private LocalDateTime date;
        private int pax;

        Reservation(int contact, String name, LocalDateTime date, int pax) {
            super(tableId);
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
            return null;
        }
    }
}