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
	public void addItem(String tableID,Scanner s)
	{
		int tempOrderID = -1;
		int tempItemID = -1;
		int tempItemCount = -1;
		boolean  added = false;
		for(int i = 0;i<TableManager.tableList.size(); i++)
		{
			if(TableManager.tableList.get(i).tableID == tableID)
			{
				tempOrderID = TableManager.tableList.get(i).orderID;
			}
		}
		if(tempOrderID != -1)
		{
			System.out.println("Please enter the item ID");
			tempItemID = s.nextInt();
			System.out.println("Please enter the item count");
			tempItemCount = s.nextInt();
			try {
				for(int i = 0; i<orderList.size();i++)
				{
					if(orderList.get(i).orderID == tempOrderID)
					{
						orderList.get(i).addItem(tempItemID, tempItemCount);
						added = true;
					}
				}
			}
			catch(Exception e)
			{
				System.out.println("Error. Order not added.");
			}
			if(added)
				System.out.println("Order has been added!");
			else
				System.out.println("Error. Order not added.");
				
		}
	}
	public void choices(Scanner s)
	{
		//choices
	}
}
