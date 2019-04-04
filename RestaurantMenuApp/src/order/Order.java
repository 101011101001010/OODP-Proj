package order;

import client.RestaurantAsset;
import enums.FileName;
import tools.FileIO;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Order extends RestaurantAsset {

    List<OrderItem> orderItemList = new ArrayList<>();
    int staffId;
    int orderId;
    //Date orderDate;

    public Order(int tableId, int orderId, int staffId) {
        super(tableId);
        this.orderId = orderId;
        this.staffId = staffId;
        //orderDate.toLocalDate();
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public int getStaffID() {
        return staffId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void addItem(OrderItem item) {
        orderItemList.add(item);
    }

    @Override
    public String toPrintString() {
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

    public String toInvoiceString() {
        StringBuilder sb = new StringBuilder();
        String head = "Order ID: " + getId() + "--";
        sb.append(head);
        BigDecimal totalPrice = new BigDecimal(0);

        for (int index = 0; index < orderItemList.size(); index++) {
            String s = orderItemList.get(index).getItem().getName() + " x " + orderItemList.get(index).getCount() + " - " + orderItemList.get(index).getPrice();
            totalPrice = totalPrice.add(orderItemList.get(index).getPrice());
            sb.append(s);
            sb.append("--");
        }

        sb.append("Total: " + totalPrice.toString());

        FileIO f = new FileIO();
        String writeData;
        for (int i = 0; i < orderItemList.size(); i++) {
            writeData = getId() + ", " + orderId + ", ";
            writeData += orderItemList.get(i).getItem().getId() + ", " + orderItemList.get(i).getCount() + ", " + orderItemList.get(i).getPrice();

            try {
                f.writeLine(FileName.REVENUE, writeData);
            } catch (IOException ignored) {

            }
        }
        return sb.toString();
    }
}
