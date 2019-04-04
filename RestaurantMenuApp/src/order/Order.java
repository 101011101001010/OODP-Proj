package order;

import client.RestaurantData;
import enums.DataType;
import menu.MenuItem;
import tools.FileIO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Order extends RestaurantData {

    private List<OrderItem> orderItemList;
    private int staffId;
    private String orderId;
    //Date orderDate;

    public Order(int tableId, String orderId, int staffId) {
        super(tableId);
        this.orderId = orderId;
        this.staffId = staffId;
        orderItemList = new ArrayList<>();
        //orderDate.toLocalDate();
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public int getStaffID() {
        return staffId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void addItem(MenuItem item, int count) {
        orderItemList.add(new OrderItem(item, count));
    }

    @Override
    public String toPrintString() {
        StringBuilder sb = new StringBuilder();
        String head = getId() + " // " + orderId + " // " + staffId;
        sb.append(head);
        BigDecimal totalPrice = new BigDecimal(0);

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
    public String toTableString() {
        StringBuilder sb = new StringBuilder();
        String head = getId() + " // " + orderId + " // " + staffId + " // ";
        sb.append(head);
        BigDecimal totalPrice = new BigDecimal(0);

        for (int index = 0; index < orderItemList.size(); index++) {
            String s = orderItemList.get(index).getItem().getName() + " x " + orderItemList.get(index).getCount() + " - " + orderItemList.get(index).getPrice();
            totalPrice = totalPrice.add(orderItemList.get(index).getPrice());
            sb.append(s);
            sb.append("--");
        }

        sb.append("Total: " + totalPrice.toString());
        return sb.toString();
    }

    public List<String> toInvoiceString() {
        List<String> list = new ArrayList<>();
        BigDecimal totalPrice = new BigDecimal(0);
        list.add("Order ID: " + getOrderId());
        list.add("QTY // ITEM DESCRIPTION // TOTAL");

        for (int index = 0; index < orderItemList.size(); index++) {
            OrderItem item = orderItemList.get(index);
            String s = item.getCount() + " // " + item.getItem().getName() + " // " + item.getPrice();
            list.add(s);
            totalPrice = totalPrice.add(item.getPrice());
        }

        list.add("Total payable // " + totalPrice.toString());

        FileIO f = new FileIO();
        String writeData;
        for (int i = 0; i < orderItemList.size(); i++) {
            writeData = getId() + ", " + orderId + ", ";
            writeData += orderItemList.get(i).getItem().getId() + ", " + orderItemList.get(i).getCount() + ", " + orderItemList.get(i).getPrice();

            try {
                f.writeLine(DataType.REVENUE, writeData);
            } catch (IOException ignored) {

            }
        }
        return list;
    }
}
