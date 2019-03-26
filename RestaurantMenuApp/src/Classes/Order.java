package Classes;

import java.sql.Date;
import java.util.ArrayList;

import tools.MenuFactory;

public class Order {
	ArrayList<OrderItem> orderItemList = new ArrayList<OrderItem>();
	int orderID;
	int staffID;
	Date orderDate;
	String sessions;
	
	public Order(int orderID, int staffID){
		this.orderID = orderID;
		this.staffID = staffID;
		orderDate.toLocalDate();
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
	public void view()
	{
		for(int i = 0; i<TableManager.tableList.size();i++)
		{
			String tableID = TableManager.tableList.get(i).tableID;
			String title = "Bill for "+tableID;
			String[] items = new String[this.orderItemList.size()];
			for(int j =0; j<orderItemList.size();j++)
			{
				items[j] = orderItemList.get(j).print();
			}
			MenuFactory.printMenu(title, items);
		}
	}
	public void print(String tableID)
	{
		String title = "Bill for "+tableID;
		String[] items = new String[this.orderItemList.size()];
		for(int i =0; i<orderItemList.size();i++)
		{
			orderItemList.get(i).addCount();
			items[i] = orderItemList.get(i).print();
		}
		MenuFactory.printMenu(title, items);
		//saveItemToRevenue
	}
}
