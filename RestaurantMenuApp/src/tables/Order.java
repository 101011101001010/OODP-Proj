package tables;

import client.RestaurantData;
import menu.MenuItem;

import java.math.BigDecimal;
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

    void removeItem(OrderItem item) {
        orderItemList.remove(item);
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
}
