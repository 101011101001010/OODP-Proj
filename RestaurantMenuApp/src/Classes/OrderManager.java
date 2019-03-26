package Classes;
import java.util.Scanner;
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
	public void addItem(String tableID)
	{
		int tempOrderID;
		for(int i = 0;i<TableManager.tableList.size(); i++)
		{
			if(TableManager.tableList.get(i).tableID == tableID)
			{
				tempOrderID = TableManager.tableList.get(i).billID;
			}
		}
	}
	public void choices(Scanner s)
	{
		//choices
	}
}
