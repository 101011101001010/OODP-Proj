package Classes;

import java.util.ArrayList;

public class Order {
	ArrayList<OrderItem> orderItemList = new ArrayList<OrderItem>();
	int orderID;
	int staffID;
	
	public Order(int orderID, int staffID){
		this.orderID = orderID;
		this.staffID = staffID;
	}
	
	public void addItem(int itemID,int count)
	{
		OrderItem x = new OrderItem(itemID,count);
		orderItemList.add(x);
	}
	
	public float calculatePrice()
	{
		float price = 0;
		
		  for(int i = 0; i<orderItemList.size();i++)
		  {
		  	price += orderItemList.get(i).price;
		  }
		return price;
	}
}
