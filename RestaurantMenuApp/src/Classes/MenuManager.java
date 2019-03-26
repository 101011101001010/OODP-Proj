package Classes;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;
import java.util.Comparator;
public class MenuManager {
	
	static ArrayList<MenuItem> menuList = new ArrayList<MenuItem>();
	static ArrayList<PromotionItem> promotionList = new ArrayList<PromotionItem>();
	
	public MenuManager()
	{
		//load Menu from txt file
		//load promotion from txt file
	}
	
	public static float getItemPrice(int itemID)
	{
		for(int i = 0; i< menuList.size();i++)
		{
			if(menuList.get(i).itemID == itemID)
			{
				return menuList.get(i).price;
			}
		}
		return -1;
	}
	public void choices(Scanner s)
	{
		int choice = -1;
		while(choice!= 5)
		{
			System.out.println("1. View Items");
			System.out.println("2. Add Items");
			System.out.println("3. Edit Items");
			System.out.println("4. Remove Items");
			System.out.println("5. Exit");
			try {
			choice = s.nextInt();
			}
			catch(Exception e)
			{
				System.out.println("Please enter a valid choice.");	
			}
			switch(choice)
			{
			//View Items
			case 1:
				int choice2 = -1;
				while(choice2!=5)
				{
					System.out.println("View by: ");
					System.out.println("1. ID ");
					System.out.println("2. Name ");
					System.out.println("3. Price");
					System.out.println("4. Category");
					System.out.println("5. Return");
					try {
						choice2 = s.nextInt();
						}
						catch(Exception e)
						{
							System.out.println("Please enter a valid choice.");	
						}
					viewItems(choice2);
				}
				break;
				
			//Add Items
			case 2 :
				break;
			
			//Edit Items
			case 3 :
				break;
			
			//Remove Items
			case 4 :
				break;
				
			//Exit
			case 5 :
				break;
				
			default:
				System.out.println("Please enter a valid choice.");	
			}
		}
	}
	public void viewItems(int sortChoice)
	{
		ArrayList<MenuItem> tempList = menuList;
		switch(sortChoice) {
		//ID
		case 1:
			Collections.sort(tempList, new Comparator<MenuItem>(){
	            public int compare(MenuItem s1, MenuItem s2) {
	            	return Integer.compare(s1.itemID, s2.itemID);
	           }
	       });
		break;
		
		//Name
		case 2:
			Collections.sort(tempList, new Comparator<MenuItem>(){
	            public int compare(MenuItem s1, MenuItem s2) {
	            	return s1.name.compareTo(s2.name);
	           }
	       });
		break;
		
		//Price
				case 3:
					Collections.sort(tempList, new Comparator<MenuItem>(){
			            public int compare(MenuItem s1, MenuItem s2) {
			            	return Float.compare(s1.price, s2.price);
			           }
			       });
				break;
				}
		
		//Category	
		case 4:
			Collections.sort(tempList, new Comparator<MenuItem>(){
	            public int compare(MenuItem s1, MenuItem s2) {
	            	return s1.name.compareTo(s2.name);
	           }
	       });
			Collections.sort(tempList, new Comparator<MenuItem>(){
	            public int compare(MenuItem s1, MenuItem s2) {
	            	return  s1.category.compareTo(s2.category);
	           }
	       });
		break;
		
		
		
		for(int i = 0; i<tempList.size(); i++)
		{
			tempList.get(i).print();
		}
		
	}
}
