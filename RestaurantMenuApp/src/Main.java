import java.util.Scanner;

import Classes.*;
public class Main {
	public static void main(String[] args) {
		//Start by loading files
		//loadReservations();
		//loadMenu();
		//loadPromotions();
		//loadStaff();
		//loadSales();
		Scanner scanner = new Scanner(System.in);
		int choice = -1;
		while(choice!=0)
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
			choice = scanner.nextInt();
			}
			catch(Exception e)
			{
				System.out.println("Please enter a valid choice");
			}
			
			switch(choice) {
			//Orders
			case 1:
				break;
			
			//Print bill
			case 2:
				break;
			
			//Reservations
			case 3:
				break;
			
			//Menu 
			case 4:
			{
				System.out.println("Please select an option");
				System.out.println("1. Orders");
				System.out.println("2. Print bill");
				System.out.println("3. Reservation");
				System.out.println("4. Menu options");
				System.out.println("5. Admin menu");
				System.out.println("6. Exit");
			}
			
			//Admin
			case 5:
				break;
			
			case 6:
				break;
			
			//False input
			default:
				System.out.println("Please enter a valid choice.");	
			break;
			
			}
		}
		
		
		//Closing
		scanner.close();
		//Saving
		//saveReservations();
		//saveMenu();
		//savePromotions();
		//saveStaff();
		//saveSales();
		//Should add these into "Add" functions
	}

}
