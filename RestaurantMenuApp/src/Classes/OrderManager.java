package Classes;
import java.util.Scanner;

import tools.InputChecker;
import tools.MenuFactory;

import java.util.ArrayList;

public class OrderManager {

	static ArrayList<Order> orderList = new ArrayList<Order>();
	static int orderCount = 0;

	public void addNewOrder(Scanner s, TableManager tm)
	{
		System.out.println("Please enter Staff ID");
		int staffID = s.nextInt();
		s.nextLine();
		System.out.println("Please enter the table ID");
		int tableID = s.nextInt();
		if(!InputChecker.checkTableID(tableID))
		{
			System.out.println("TableID Error.");
			return;
		}
		if(tm.checkOccupied(tableID))
		{
			System.out.println("Table is occupied!");
		}
		orderCount++;
		Order x = new Order(orderCount,staffID,tableID);
		orderList.add(x);
		tm.setOccupied(tableID,x.orderID);
	}

	public void viewOrder(PromotionManager pm, MenuManager mm)
	{
		for(int i = 0; i<orderList.size();i++)
		{
			orderList.get(i).view(pm,mm);
		}
	}

	public void removeOrder(Scanner s,TableManager tm)
	{
		System.out.println("Please enter the table ID");
		int tableID = s.nextInt();
		if(!InputChecker.checkTableID(tableID))
		{
			System.out.println("TableID Error.");
			return;
		}
		int orderID = -1;
		orderID = tm.getOrderID(tableID);
		if(orderID!=-1)
		{
			for(int i =0; i<orderList.size();i++)
			{
				if(orderList.get(i).orderID == orderID)
					orderList.remove(i);
			}
			tm.clear(tableID);
		}
		else
		{
			System.out.println("Table does not have an order!");
		}
	}
	public void printBill(Scanner s,TableManager tm,PromotionManager pm, MenuManager mm)
	{
		int tableID;
		int tempOrderID = -1;
		System.out.println("Please enter the table ID");
		tableID = s.nextInt();
		if(!InputChecker.checkTableID(tableID))
		{
			System.out.println("TableID Error.");
			return;
		}
		tempOrderID = tm.getOrderID(tableID);
		tm.clear(tableID);
		for(int j = 0; j<orderList.size();j++)
		{
			if(orderList.get(j).orderID == tempOrderID)
			{
				orderList.get(j).print(tableID,pm,mm);
				orderList.remove(j);
			}
		}
	}
	public void addItem(Scanner s,TableManager tm,PromotionManager pm, MenuManager mm)
	{
		int tempOrderID = -1;
		int tempItemID = -1;
		int tempItemCount = -1;
		int tableID;
		boolean  added = false;
		System.out.println("Please enter the table ID");
		tableID = s.nextInt();
		if(!InputChecker.checkTableID(tableID))
		{
			System.out.println("TableID Error.");
			return;
		}
		tempOrderID = tm.getOrderID(tableID);
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
						orderList.get(i).addItem(tempItemID, tempItemCount,pm,mm);
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
	public void choices(Scanner s, TableManager tm,PromotionManager pm, MenuManager mm)
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
				viewOrder(pm,mm);
				break;
			case 2:
				addNewOrder(s,tm);
				break;
			case 3:
				addItem(s,tm,pm,mm);
				break;
			case 4:
				removeOrder(s,tm);
				break;
			case 5:
				printBill(s,tm,pm,mm);
				break;
			case 6:
				break;
			default:
				System.out.println("Please enter a valid choice.");	
			}
		}
	}

}
