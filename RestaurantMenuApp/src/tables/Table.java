package tables;

import core.RestaurantData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

public class Table extends RestaurantData {
    private int capacity;
    private boolean occupied;
    private boolean reserved;
    private Order order;
    private Map<String, Reservation> reservationMap;

    Table(int tableId, int capacity) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = false;
        this.reserved = false;
        this.order = null;
        this.reservationMap = new HashMap<>();
    }

    Table(int tableId, int capacity, boolean occupied, boolean reserved, Order order) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = occupied;
        this.reserved = reserved;
        this.order = order;
        this.reservationMap = new HashMap<>();
    }

    boolean isOccupied() {
        return occupied;
    }

    boolean isReserved() {
        return reserved;
    }

    Order getOrder() {
        return order;
    }

    boolean hasOrder() {
        return (occupied && order != null);
    }

    void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    boolean isAvailable(LocalDateTime dateTime) {
        DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
        String dateKey = dateTime.format(format);
        return (!occupied && !reservationMap.containsKey(dateKey));
    }

    boolean isLargeEnough(int pax) {
        if (pax == 5) {
            pax++;
        }

        return (capacity >= pax && capacity <= (pax + 2));
    }

    void removeExpiredReservations() {
        reservationMap.values().stream().filter(Reservation::isExpired).forEach(this::deleteReservation);
    }

    Order attachOrder(int staffId) {
        occupied = true;
        reserved = false;
        String orderId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        order = new Order(getId(), orderId, staffId);
        return order;
    }

    void attachOrder(Order order) {
        occupied = true;
        reserved = false;
        this.order = order;
    }

    void clear() {
        occupied = false;
        reserved = false;
        order = null;
    }

    Map<String, Reservation> getReservationMap() {
        return reservationMap;
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

    List<Reservation> findReservationsByContact(int contact) {
        return reservationMap.values().stream().filter(reservation -> reservation.matchContact(contact)).sorted(Comparator.comparing(Reservation::getDate)).collect(Collectors.toList());
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

        int getTableId() {
            return getId();
        }

        int getPax() {
            return pax;
        }

        String toFileString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return contact + "," + name + "," + date.format(format) + "," + pax;
        }

        String toDisplayString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getId() + " // " + name + " // " + contact + " // " + date.format(format) + " // " + pax;
        }

        boolean isExpired() {
            return date.plusSeconds(30).isBefore(LocalDateTime.now());
        }

        boolean isCurrentSession() {
            DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
            return getSessionString().equals(LocalDateTime.now().format(format));
        }

        boolean matchContact(int contact) {
            return (this.contact == contact);
        }

        boolean isArrivalWindow() {
            return (isCurrentSession() && !isExpired());
        }

        String getSessionString() {
            DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
            return date.format(format);
        }
    }
}