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
	public void printBill(Scanner s)
	{
		String tableID = "";
		int tempOrderID = -1;
		System.out.println("Please enter the table ID");
		tableID = s.next();
		for(int i = 0;i<TableManager.tableList.size(); i++)
		{
			if(TableManager.tableList.get(i).tableID == tableID)
			{
				TableManager.tableList.get(i).occupied=false;
				tempOrderID = TableManager.tableList.get(i).orderID;
				TableManager.tableList.get(i).orderID = 0;
				for(int j = 0; j<orderList.size();j++)
				{
					if(orderList.get(j).orderID == tempOrderID)
					{
						Order tempOrder = orderList.get(j);
						orderList.remove(j);
						tempOrder.print(tableID);
						//saveItemToRevenue
					}
				}
			}
		}
	}
	public void addItem(Scanner s)
	{
		int tempOrderID = -1;
		int tempItemID = -1;
		int tempItemCount = -1;
		String tableID="";
		boolean  added = false;
		System.out.println("Please enter the table ID");
		tableID = s.next();
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
		int choice = -1;
		while(choice!= 6)
		{
			System.out.println("1. View Orders");
			System.out.println("2. Add New Order");
			System.out.println("3. Add Item To Order");
			System.out.println("4. Remove Order");
			System.out.println("5. Print Bill");
			System.out.println("6. Exit");
			try {
				choice = s.nextInt();
			}
			catch(Exception e)
			{
				System.out.println("Please enter a valid choice.");	
			}
			
			switch(choice) {
			case 1:
				break;
			case 2:
				break;
			case 3:
				addItem(s);
				break;
			case 4:
				break;
			case 5:
				printBill(s);
				break;
			case 6:
				break;
			default:
				System.out.println("Please enter a valid choice.");	
			}
		}
	}
}
