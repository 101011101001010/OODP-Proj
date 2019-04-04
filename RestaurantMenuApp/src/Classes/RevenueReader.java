/*package Classes;

import constants.AppConstants;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import tools.FileIOHandler;

//import java.sql.Date;

public class RevenueReader {
		
	public class RevenueObject
	{
		int tableID;
		int itemID;
		int itemCount;
		float totalPrice;
		//Date date;
		
		public RevenueObject(int tableID, int itemID, int itemCount, float totalPrice)
		{
			this.tableID = tableID;
			this.itemID = itemID;
			this.itemCount = itemCount;
			this.totalPrice = totalPrice;
		}
		
		public String toString()
		{
			return "Table: " + tableID + ", Item ID: " + itemID + ", Count: " + itemCount +", TotalPrice: " + totalPrice;
		}
		public String toString2()
		{
			return "Item ID: " + itemID + ", Sale Count: " + itemCount +", Total Revenue: " + totalPrice;
		}
	}
	
	private static List<RevenueObject> revenueList;
	private static Set<String> itemTypes;
	private static AtomicInteger cId;
	public void loadData()
	{
		List<String> fileData = FileIOHandler.read(AppConstants.FILE_NAMES[5].toLowerCase());
		String[] lineData;
		if (fileData != null) 
		{
			revenueList = new ArrayList<>();
			itemTypes = new HashSet<>();
			cId = new AtomicInteger(-1);
		
			for (String line : fileData) 
			{
				lineData = line.split(AppConstants.FILE_SEPARATOR);
		
				if (lineData.length == 4) 
				{
					RevenueObject item = new RevenueObject(Integer.parseInt(lineData[0]), Integer.parseInt(lineData[1]),Integer.parseInt(lineData[2]),Float.parseFloat(lineData[3]));
					revenueList.add(item);
				}
			}
		}
	}
	
	public void printAll()
	{
		for(int i =0;i<revenueList.size();i++)
		{
			System.out.println(revenueList.get(i).toString());
		}
	}
	
	public void itemTotal()
	{
		int temp = -1;
		ArrayList<RevenueObject> totalList = new ArrayList<RevenueObject>();
		
		for(int i = 0;i<revenueList.size();i++)
		{
			temp = searchList(revenueList.get(i).itemID,totalList);
			if(temp==-1)
			{
				totalList.add(revenueList.get(i));
			}
			else
			{
				totalList.get(temp).itemCount += revenueList.get(i).itemCount;
				totalList.get(temp).totalPrice += revenueList.get(i).totalPrice;
			}
		}
		print(totalList);
	}
	
	public int searchList(int id, ArrayList<RevenueObject> list)
	{
		for(int i = 0; i<list.size();i++)
		{
			if(list.get(i).itemID == id)
			{
				return i;
			}
		}
		return -1;
	}
	
	public void print(ArrayList<RevenueObject> list)
	{
		for(int i = 0; i<list.size();i++)
		{
			System.out.println(list.get(i).toString2());
		}
	}
}*/
