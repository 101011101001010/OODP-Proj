package tables;

import core.RestaurantData;
import menu.MenuItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity class.
 */
public class Order extends RestaurantData {
    /**
     * List of order items attached to the order.
     */
    private List<OrderItem> orderItemList;

    /**
     * The last staff that managed the order.
     */
    private int staffId;

    /**
     * Order ID in the form of the date-time at order creation.
     */
    private String orderId;

    /**
     * Creates an order with the specified parameters.
     * @param tableId Table ID to attach the order to. Passed into the parent class as the unique ID.
     * @param orderId Order ID in the form of the date-time at order creation.
     * @param staffId The last staff that managed the order.
     */
    Order(int tableId, String orderId, int staffId) {
        super(tableId);
        this.orderId = orderId;
        this.staffId = staffId;
        orderItemList = new ArrayList<>();
    }

    /**
     * Retrieves the list of order items attached to the order.
     * @return List of order items.
     */
    List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    /**
     * Retrieves the order ID.
     * @return Order ID.
     */
    String getOrderId() {
        return orderId;
    }

    /**
     * Adds an item to the list of order items.
     * @param item MenuItem to be added.
     * @param count Amount of the item to be added.
     */
    void addItem(MenuItem item, int count) {
        orderItemList.add(new OrderItem(item, count));
    }

    /**
     * Checks if an order item exists in the order.
     * @param item OrderItem to check against.
     * @return True / False
     */
    boolean ifItemExists(OrderItem item) {
        return orderItemList.stream().anyMatch(orderItem -> orderItem.matchItemId(item));
    }

    /**
     * Checks if the specified order item and its count matches exactly as the order item in the order.
     * @param item OrderItem to check against.
     * @param count Count to check against.
     * @return True / False
     */
    boolean matchItemCount(OrderItem item, int count) {
        return (item.matchCount(count));
    }

    /**
     * Updates the specified order item's count in the order.
     * @param item OrderItem to update.
     * @param count New item count.
     */
    void updateItemCount(OrderItem item, int count) {
        item.updateCount(count);
    }

    /**
     * Retrieves the name of the MenuItem assigned to the OrderItem.
     * @param item OrderItem to retrieve the name from.
     * @return Name of the MenuItem
     */
    String getItemName(OrderItem item) {
        return item.getItem().getName();
    }

    /**
     * Retrieves the item count of the MenuItem assigned to the specified OrderItem.
     * @param item OrderItem to retrieve the count from.
     * @return Count of the MenuItem assigned to the OrderItem.
     */
    int getItemCount(OrderItem item) {
        return item.getCount();
    }

    /**
     * Retrieves the final item price of the specified OrderItem.
     * @param item OrderItem to retrieve the price from.
     * @return Final price of the OrderItem.
     */
    BigDecimal getItemPrice(OrderItem item) {
        return item.getPrice();
    }

    /**
     * Removes a specified count of MenuItem from the specified OrderItem.
     * @param item OrderItem to update the count of.
     * @param count New count of the MenuItem.
     * @return True / False
     */
    boolean removeItems(OrderItem item, int count) {
        if (!ifItemExists(item)) {
            return false;
        }

        if (count < 0) {
            count *= -1;
        }

        if (matchItemCount(item, count)) {
            orderItemList.remove(item);
            return true;
        }

        count *= -1;
        updateItemCount(item, count);
        return true;
    }

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
    @Override
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        String head = getId() + " // " + orderId + " // " + staffId;
        sb.append(head);

        if (orderItemList.size() > 0) {
            sb.append(" // ");
        }

        for (int index = 0; index < orderItemList.size(); index++) {
            String s = orderItemList.get(index).getItem().getId() + "x" + orderItemList.get(index).getCount();
            sb.append(s);

            if (index != orderItemList.size() - 1) {
                sb.append("--");
            }
        }

        return sb.toString();
    }

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        String head = getId() + " // " + orderId + " // " + staffId + " // ";
        sb.append(head);
        BigDecimal totalPrice = new BigDecimal(0);

        for (OrderItem orderItem : orderItemList) {
            String s = orderItem.getItem().getName() + " x " + orderItem.getCount() + " - " + orderItem.getPrice();
            totalPrice = totalPrice.add(orderItem.getPrice());
            sb.append(s);
            sb.append("\n");
        }

        sb.append("Total: ").append(totalPrice.toString());
        return sb.toString();
    }

    /**
     * Entity that is assigned a MenuItem and a count of the MenuItem.
     */
    class OrderItem {
        /**
         * MenuItem assigned to the OrderItem.
         */
        private MenuItem item;

        /**
         * Count of the MenuItem assigned to the OrderItem.
         */
        private int count;

        /**
         * Final price of the OrderItem.
         */
        private BigDecimal price;

        /**
         * Price of each of the MenuItem.
         */
        private BigDecimal pricePer;

        /**
         * Creates a new OrderItem with the specified parameters.
         * @param item MenuItem to be assigned.
         * @param count Count of the MenuItem assigned.
         */
        private OrderItem(MenuItem item, int count) {
            this.item = item;
            this.count = count;
            this.pricePer = item.getPrice();
            this.price = pricePer.multiply(new BigDecimal(count)).setScale(2, RoundingMode.FLOOR);
        }

        /**
         * Checks if the MenuItem assigned to the specified OrderItem matches.
         * @param item OrderItem to check against.
         * @return True / False.
         */
        private boolean matchItemId(OrderItem item) {
            return (this.getItem().getId() == item.getItem().getId());
        }

        /**
         * Retrieves the assigned MenuItem.
         * @return The MenuItem assigned.
         */
        private MenuItem getItem() {
            return item;
        }

        /**
         * Updates the count of the assigned MenuItem.
         * @param count New count.
         */
        private void updateCount(int count) {
            this.count += count;
            this.price = pricePer.multiply(new BigDecimal(this.count)).setScale(2, RoundingMode.FLOOR);
        }

        /**
         * Retrieves the count of the assigned MenuItem.
         * @return Count of the assigned MenuItem.
         */
        private int getCount() {
            return count;
        }

        /**
         * Checks if the specified count matches..
         * @param count Count to check against.
         * @return True / False
         */
        private boolean matchCount(int count) {
            return (this.count == count);
        }

        /**
         * Retrieves the final price of the OrderItem.
         * @return Final price of the OrderItem.
         */
        private BigDecimal getPrice() {
            return price;
        }
    }
}
