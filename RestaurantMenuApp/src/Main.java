import java.util.Scanner;
import tools.*;
import Classes.*;
public class Main {
	public static void main(String[] args) {
		//Start by loading files
		FileIOHandler.buildDirectory();
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
	}

}
