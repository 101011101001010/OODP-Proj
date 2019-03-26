package Classes;
import java.util.Scanner;

import tools.MenuFactory;

import java.util.ArrayList;

public class OrderManager {

	static ArrayList<Order> orderList = new ArrayList<Order>();
	static int orderCount = 0;

	public static void addNewOrder(Scanner s)
	{
		System.out.println("Please enter Staff ID");
		int staffID = s.nextInt();
		System.out.println("Please enter the table ID");
		String tableID = s.next();
		orderCount++;
		Order x = new Order(orderCount,staffID,tableID);
		orderList.add(x);
		TableManager.setOccupied(tableID,x.orderID);
	}
	public static void viewOrder()
	{
		for(int i = 0; i<orderList.size();i++)
		{
			orderList.get(i).view();
		}
	}
	public static void removeOrder(Scanner s)
	{
		System.out.println("Please enter the table ID");
		String tableID = s.next();
		int orderID = -1;
		for(int i = 0; i<TableManager.tableList.size();i++)
		{
			if(TableManager.tableList.get(i).tableID == tableID)
				orderID=TableManager.tableList.get(i).orderID;
		}
		if(orderID!=-1)
		{
			for(int i =0; i<orderList.size();i++)
			{
				if(orderList.get(i).orderID == orderID)
					orderList.remove(i);
			}
			TableManager.clear(tableID);
		}
		else
		{
			System.out.println("Error removing order!");
		}
	}
	public static void printBill(Scanner s)
	{
		String tableID = "";
		int tempOrderID = -1;
		System.out.println("Please enter the table ID");
		tableID = s.next();
		for(int i = 0;i<TableManager.tableList.size(); i++)
		{
			if(TableManager.tableList.get(i).tableID == tableID)
			{
				TableManager.tableList.get(i).occupied=0;
				tempOrderID = TableManager.tableList.get(i).orderID;
				TableManager.tableList.get(i).orderID = 0;
				for(int j = 0; j<orderList.size();j++)
				{
					if(orderList.get(j).orderID == tempOrderID)
					{
						orderList.get(j).print(tableID);
						orderList.remove(j);
					}
				}
			}
		}
	}
	public static void addItem(Scanner s)
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
	public static void choices(Scanner s)
	{
		int choice = -1;
		while(choice!= 6)
		{
			System.out.println("1. View Orders");//done
			System.out.println("2. Add New Order");//done
			System.out.println("3. Add Item To Order");//done
			System.out.println("4. Remove Order");//done
			System.out.println("5. Print Bill");//done
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
				viewOrder();
				break;
			case 2:
				addNewOrder(s);
				break;
			case 3:
				addItem(s);
				break;
			case 4:
				removeOrder(s);
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
