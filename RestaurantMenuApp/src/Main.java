import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

import constants.AppConstants;
import constants.MenuConstants;
import tools.*;
public class Main {
	public static void main(String[] args) {
		if (!FileIOHandler.buildDirectory()) {
			return;
		}

		int choice;
		int backInput = MenuConstants.OPTIONS_TERMINATE;
		Scanner s = new Scanner(System.in);
		ScannerHandler sc = new ScannerHandler(s);

		do {
			MenuFactory.printMenu(MenuConstants.APP_TITLE, MenuConstants.OPTIONS_MAIN);
			choice = MenuFactory.loopChoice(sc, MenuConstants.OPTIONS_MAIN.length);

			if (choice != backInput) {
				String className = AppConstants.CODE_NAMES[choice];
				String methodName = MenuConstants.MENU_HANDLER_CALL_METHOD;

				try {
					Class c = Class.forName(className);
					Method method = c.getDeclaredMethod(methodName, ScannerHandler.class);
					method.invoke(null, sc);
				}

				// die lor
				catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					try {
						Class c = Class.forName(className);
						Method method = c.getDeclaredMethod("choices", Scanner.class);
						method.invoke(null, s);
					} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ee) {
						System.out.println("WORK IN PROGRESS!");
					}
				}
			}
		} while (choice != backInput);

		/*
		OrderManager orderManager = new OrderManager();
		MenuManager menuManager = new MenuManager();
		ReservationManager reservationManager = new ReservationManager();
		StaffManager staffManager = new StaffManager();
		TableManager tableManager = new TableManager();
		Scanner s = new Scanner(System.in);
		int choice = -1;
		while(choice!=6)
		{
			System.out.println("Please select an option");
			System.out.println("1. Orders");
			System.out.println("2. Print bill");
			System.out.println("3. Reservation");
			System.out.println("4. Menu options");
			System.out.println("5. Admin menu");
			System.out.println("6. Exit");
			try
			{
			choice = s.nextInt();
			}
			catch(Exception e)
			{
				System.out.println("Please enter a valid choice");
			}
			
			switch(choice) {
			//Orders
			case 1:
				orderManager.choices(s);
			
			//Print bill
			case 2:
				break;
			
			//Reservations
			case 3:
				reservationManager.choices(s);
			
			//Menu 
			case 4:
				menuManager.choices(s);
			
			//Admin
			case 5:
				break;
			
			case 6:
				break;
			
			//False input
			default:
				System.out.println("Please enter a valid choice.");				
			}
		}
		
		
		//Closing
		s.close();
		//Saving
		//saveReservations();
		//saveMenu();
		//savePromotions();
		//saveStaff();
		//saveSales();
		//Should add these into "Add" functions
		*/
	}

}
