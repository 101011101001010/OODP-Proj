package Classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TableManager {

	public static ArrayList<Table> tableList = new ArrayList<Table>();
	private static Map<String, Integer> map;
	static boolean done = false;

	static
	{
		map = new HashMap<>();

		String id;
		int count = 0;

		for (int i = 0; i<10; i++)
		{
			id = "2" + i;
			tableList.add(new Table(id));
			map.put(id, count);
			count++;
		}
		for (int i = 0; i<10; i++)
		{
			id = "4" + i;
			tableList.add(new Table(id));
			map.put(id, count);
			count++;
		}
		for (int i = 0; i<5; i++)
		{
			id = "8" + i;
			tableList.add(new Table(id));
			map.put(id, count);
			count++;
		}
		for (int i = 0; i<5; i++)
		{
			id = "10" + i;
			tableList.add(new Table(id));
			map.put(id, count);
			count++;
		}

		done = true;
	}

	public static void choices(Scanner s)
	{
		int choice = -1;
		while (choice != 0){
			System.out.println("1. Show Table");
			System.out.println("1. Set Table");
			System.out.println("1. Show Table");
		}
	}

	public static void checkVacancy()
	{
		int index = 0;
		System.out.println("Tables with 2 seats.");
		for (; index < 10; index++) {
			System.out.println("Table " + tableList.get(index).getTableID() + " is ");
			switch (tableList.get(index).isOccupied()) {
				case -1:
					System.out.print("reserved.");
					break;
				case 0:
					System.out.print("occupied.");
					break;
				case 1:
					System.out.print("not occupied.");
					break;
			}
		}

		System.out.println("Tables with 4 seats.");
		for (; index < 20; index++) {
			System.out.println("Table " + tableList.get(index).getTableID() + " is ");
			switch (tableList.get(index).isOccupied()) {
				case -1:
					System.out.print("reserved.");
					break;
				case 0:
					System.out.print("occupied.");
					break;
				case 1:
					System.out.print("not occupied.");
					break;
			}
		}
		System.out.println("Tables with 8 seats.");
		for (; index < 25; index++) {
			System.out.println("Table " + tableList.get(index).getTableID() + " is ");
			switch (tableList.get(index).isOccupied()) {
				case -1:
					System.out.print("reserved.");
					break;
				case 0:
					System.out.print("occupied.");
					break;
				case 1:
					System.out.print("not occupied.");
					break;
			}
		}
		System.out.println("Tables with 10 seats.");
		for (; index < 30; index++) {
			System.out.println("Table " + tableList.get(index).getTableID() + " is ");
			switch (tableList.get(index).isOccupied()) {
				case -1:
					System.out.print("reserved.");
					break;
				case 0:
					System.out.print("occupied.");
					break;
				case 1:
					System.out.print("not occupied.");
					break;
			}
		}
	}


	public static void setOccupied(String tableID, int orderID)
	{
		int index = map.get(tableID);
		tableList.get(index).setOrderID(orderID);
		tableList.get(index).setOccupied(1);
	}

	public static void setReserved(String tableID, int pax){
		int index = map.get(tableID);
		tableList.get(index).setOccupied(-1);
		tableList.get(index).setPax(pax);
	}

	public static void clear (String tableID)
	{
		int index = map.get(tableID);
		tableList.get(index).setOccupied(0);
	}
}

