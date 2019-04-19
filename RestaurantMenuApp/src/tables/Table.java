package tables;

import core.RestaurantData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Table entity class
 */
public class Table extends RestaurantData {
    /**
     * Capacity of the table.
     */
    private int capacity;

    /**
     * Occupancy status of the table.
     */
    private boolean occupied;

    /**
     * Reservation status of the table for the current restaurant session.
     */
    private boolean reserved;

    /**
     * Order attached to the table. May be null.
     */
    private Order order;

    /**
     * Maps a session to a reservation. Each session should have only one reservation.
     */
    private Map<String, Reservation> reservationMap;

    /**
     * Creates a new table with mostly default parameters.
     * @param tableId ID of the table - usually related to the capacity of the table (capacity + table index). Passed into the parent class.
     * @param capacity Capacity of the table
     */
    Table(int tableId, int capacity) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = false;
        this.reserved = false;
        this.order = null;
        this.reservationMap = new HashMap<>();
    }

    /**
     * Creates a new table with the specified parameters.
     * @param tableId ID of the table - usually related to the capacity of the table (capacity + table index). Passed into the parent class.
     * @param capacity Capacity of the table
     * @param occupied Occupancy status of the table
     * @param reserved Reservation status of the table for the current restaurant session.
     * @param order Order attached to the table. May be null.
     */
    Table(int tableId, int capacity, boolean occupied, boolean reserved, Order order) {
        super(tableId);
        this.capacity = capacity;
        this.occupied = occupied;
        this.reserved = reserved;
        this.order = order;
        this.reservationMap = new HashMap<>();
    }

    /**
     * Checks if the table is occupied.
     * @return True / False
     */
    boolean isOccupied() {
        return occupied;
    }

    /**
     * Checks if the table is reserved for the current restaurant session.
     * @return True / False
     */
    boolean isReserved() {
        return reserved;
    }

    /**
     * Retrieves the order attached to the table. May be null.
     * @return Order attached to the table if not null.
     */
    Order getOrder() {
        return order;
    }

    /**
     * Checks if the table has an order (by extension is occupied).
     * @return True / False
     */
    boolean hasOrder() {
        return (occupied && order != null);
    }

    /**
     * Updates the reservation status of the table.
     * @param reserved Reservation status of the table (True / False).
     */
    void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    /**
     * Checks if the table is available at the given date/time.
     * @param dateTime Date/time to check against.
     * @return True / False
     */
    boolean isAvailable(LocalDateTime dateTime) {
        DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
        String dateKey = dateTime.format(format);
        return (!occupied && !reservationMap.containsKey(dateKey));
    }

    /**
     * Checks if the table is large enough to occupy the specified pax.
     * @param pax Pax to check against.
     * @return True / False
     */
    boolean isLargeEnough(int pax) {
        if (pax == 5) {
            pax++;
        }

        return (capacity >= pax && capacity <= (pax + 2));
    }

    /**
     * Removes all reservations that are expired (>30 minutes).
     */
    void removeExpiredReservations() {
        reservationMap.values().stream().filter(Reservation::isExpired).forEach(this::deleteReservation);
    }

    /**
     * Attaches a new order to the table with the specified staff ID.
     * @param staffId Staff ID to be attached to the order.
     * @return The newly created order.
     */
    Order attachOrder(int staffId) {
        occupied = true;
        reserved = false;
        String orderId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        order = new Order(getId(), orderId, staffId);
        return order;
    }

    /**
     * Attaches an existing order to the table.
     * @param order The existing order to be attached.
     */
    void attachOrder(Order order) {
        occupied = true;
        reserved = false;
        this.order = order;
    }

    /**
     * Clears the table of order and sets it to non-occupied and non-reserved.
     */
    void clear() {
        occupied = false;
        reserved = false;
        order = null;
    }

    /**
     * Retrieves the reservation mapping for the table.
     * @return Reservation mapping for the table.
     */
    Map<String, Reservation> getReservationMap() {
        return reservationMap;
    }

    /**
     * Adds a reservation to the reservation map with the specified parameters.
     * @param contact Contact number of the person making the reservation.
     * @param name Name of the person making the reservation.
     * @param date Date/time of reservation.
     * @param pax No. of pax to reserve for.
     * @return True if successful, else False.
     */
    boolean addReservation(int contact, String name, LocalDateTime date, int pax) {
        Reservation r = new Reservation(contact, name, date, pax);
        String dateKey = r.getSessionString();

        if (reservationMap.containsKey(dateKey)) {
            return false;
        }

        reservationMap.put(dateKey, r);
        return true;
    }

    /**
     * Removes the specified reservation from the reservation map by obtaining the session from that reservation.
     * @param r Reservation to be removed.
     */
    void deleteReservation(Reservation r) {
        String dateKey = r.getSessionString();
        reservationMap.remove(dateKey);
    }

    /**
     * Search for reservations under the specified contact.
     * @param contact Contact number to check against.
     * @return List of reservations found.
     */
    List<Reservation> findReservationsByContact(int contact) {
        return reservationMap.values().stream().filter(reservation -> reservation.matchContact(contact)).sorted(Comparator.comparing(Reservation::getDate)).collect(Collectors.toList());
    }

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
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

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
    @Override
    public String toDisplayString() {
        return getId() + " // " + occupied + " // " + reserved;
    }

    /**
     * Reservation entity attached to the table.
     */
    class Reservation {
        /**
         * Contact number of the person making the reservation.
         */
        private int contact;

        /**
         * Name of the person making the reservation.
         */
        private String name;

        /**
         * Date/time of reservation.
         */
        private LocalDateTime date;

        /**
         * No. of pax reserved for.
         */
        private int pax;

        /**
         * Creates a new reservation with the specified parameters.
         * @param contact Contact number of the person making the reservation.
         * @param name Name of the person making the reservation.
         * @param date Date/time of reservation.
         * @param pax No. of pax reserved for.
         */
        private Reservation(int contact, String name, LocalDateTime date, int pax) {
            this.contact = contact;
            this.name = name;
            this.date = date;
            this.pax = pax;
        }

        /**
         * Retrieves the date/time of the reservation.
         * @return Date/time of reservation.
         */
        LocalDateTime getDate() {
            return date;
        }

        /**
         * Retrieves the date/time of the reservation in a more readable string format.
         * @return Date/time of reservation in string format.
         */
        String getDateStr() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return date.format(format);
        }

        /**
         * Retrieves the name of the person who made the reservation.
         * @return Name of person.
         */
        String getName() {
            return name;
        }

        /**
         * Retrieves the table ID which the reservation is attached to.
         * @return Table ID attached to.
         */
        int getTableId() {
            return getId();
        }

        /**
         * Retrieves the no. of pax the reservation is made for.
         * @return No. of pax reserved for.
         */
        int getPax() {
            return pax;
        }

        /**
         * Checks if the reservation has lapsed for over 30 minutes since the reservation time.
         * @return True / False
         */
        boolean isExpired() {
            return date.plusMinutes(30).isBefore(LocalDateTime.now());
        }

        /**
         * Checks if the reservation is of the current restaurant session.
         * @return True / False
         */
        boolean isCurrentSession() {
            DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
            return getSessionString().equals(LocalDateTime.now().format(format));
        }

        /**
         * Checks if the specified contact matches.
         * @param contact Contact to check against.
         * @return True / False
         */
        boolean matchContact(int contact) {
            return (this.contact == contact);
        }

        /**
         * Checks if the reservation is in its arrival window.
         * Arrival window is any time in the current session, since a table is reserved for the entire session, and before the expiry time.
         * @return
         */
        boolean isArrivalWindow() {
            return (isCurrentSession() && !isExpired());
        }

        /**
         * Retrieves the session information string of the reservation date.
         * @return
         */
        String getSessionString() {
            DateTimeFormatter format = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("ddMMyyyy a").toFormatter(Locale.ENGLISH);
            return date.format(format);
        }

        /**
         * Please see the method description in RestaurantData.
         * @see core.RestaurantData
         */
        String toFileString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy HHmm");
            return contact + "," + name + "," + date.format(format) + "," + pax;
        }
        /**
         *
         * Please see the method description in RestaurantData.
         * @see core.RestaurantData
         */
        String toDisplayString() {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getId() + " // " + name + " // " + contact + " // " + date.format(format) + " // " + pax;
        }
    }
}