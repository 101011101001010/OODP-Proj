package Classes;

import java.util.ArrayList;
import java.util.Scanner;
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
	
}
