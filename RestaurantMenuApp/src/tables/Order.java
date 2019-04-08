package tables;

import core.RestaurantData;
import menu.MenuItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Order extends RestaurantData {

    private List<OrderItem> orderItemList;
    private int staffId;
    private String orderId;

    Order(int tableId, String orderId, int staffId) {
        super(tableId);
        this.orderId = orderId;
        this.staffId = staffId;
        orderItemList = new ArrayList<>();
        //orderDate.toLocalDate();
    }

    List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    String getOrderId() {
        return orderId;
    }

    void addItem(MenuItem item, int count) {
        orderItemList.add(new OrderItem(item, count));
    }

    boolean ifItemExists(OrderItem item) {
        return orderItemList.stream().anyMatch(orderItem -> orderItem.matchItemId(item));
    }

    boolean matchItemCount(OrderItem item, int count) {
        return (item.matchCount(count));
    }

    void updateItemCount(OrderItem item, int count) {
        item.updateCount(count);
    }

    String getItemName(OrderItem item) {
        return item.getItem().getName();
    }

    int getItemCount(OrderItem item) {
        return item.getCount();
    }

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

    /*
    public List<String> toInvoiceString() {
        List<String> list = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal(0);
        list.add("Order ID: " + getOrderId());
        list.add("QTY // ITEM DESCRIPTION // TOTAL");

        for (OrderItem item : orderItemList) {
            String s = item.getCount() + " // " + item.getItem().getName() + " // " + item.getPrice();
            list.add(s);
            totalPrice = totalPrice.add(item.getPrice());
        }

        list.add("Total payable // " + totalPrice.toString());

        FileIO f = new FileIO();
        String writeData;
        for (OrderItem orderItem : orderItemList) {
            writeData = getId() + ", " + orderId + ", ";
            writeData += orderItem.getItem().getId() + ", " + orderItem.getCount() + ", " + orderItem.getPrice();
            f.writeLine(DataType.REVENUE, writeData);
        }
        return list;
    }
    */

    class OrderItem {
        private int count;
        private MenuItem item;
        private BigDecimal price;
        private BigDecimal pricePer;

        private OrderItem(MenuItem item, int count) {
            this.item = item;
            this.count = count;
            this.pricePer = item.getPrice();
            this.price = pricePer.multiply(new BigDecimal(count)).setScale(2, RoundingMode.FLOOR);
        }

        private boolean matchItemId(OrderItem item) {
            return (this.getItem().getId() == item.getItem().getId());
        }

        private MenuItem getItem() {
            return item;
        }

        private void updateCount(int count) {
            this.count += count;
            this.price = pricePer.multiply(new BigDecimal(this.count)).setScale(2, RoundingMode.FLOOR);
        }

        private int getCount() {
            return count;
        }

        private boolean matchCount(int count) {
            return (this.count == count);
        }

        private BigDecimal getPrice() {
            return price;
        }
    }
}
