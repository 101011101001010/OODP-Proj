package Classes;

import java.util.ArrayList;

public class MenuManager {
	
	static ArrayList<MenuItem> menuList = new ArrayList<MenuItem>();
	
	public MenuManager()
	{
		//load Menu from txt file
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
	
}
