package Classes;

import java.util.ArrayList;

public class OrderManager {
	ArrayList<Order> orderList = new ArrayList<Order>();
	int orderCount = 0;
	public OrderManager()
	{
	}
	
	public void addNewOrder(int staffID)
	{
		orderCount++;
		Order x = new Order(orderCount,staffID);
		orderList.add(x);
	}
	public void printBill(int orderID)
	{
		
	}
}
