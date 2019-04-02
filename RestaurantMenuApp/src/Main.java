import Classes.MenuManager;
import Classes.OrderManager;
import Classes.StaffManager;
import client.BaseManager;
import client.Restaurant;
import client.enums.Op;
import tools.ConsoleHelper;
import tools.FileIO;
import tools.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Main {

	private Map<Integer, Class<? extends BaseManager>> classMap;
	private Map<Integer, Integer> commandMap;
	private Restaurant restaurant;
	private int commandIndex;
	private List<String[]> mainCLIOptions;

	private Main() {
		this.restaurant = new Restaurant();
		classMap = new HashMap<>();
		commandMap = new HashMap<>();
		mainCLIOptions = new ArrayList<>();
		commandIndex = 1;

		FileIO f = new FileIO();
		Pair<Op, String> response = f.prepare();
		System.out.println(response.getRight());
	}

	/**
	 * Initialises the entry command line interface with options defined in other CLI classes.
	 * Entry-point runnables are also obtained from the CLI classes to map the commands to.
	 */
	private void initCLI() {
		BaseManager manager;
		try {
			manager = new MenuManager(restaurant);
			manager.init();
			map(manager);

			manager = new OrderManager(restaurant);
			map(manager);

			manager = new StaffManager(restaurant);
			manager.init();
			map(manager);

			start();
		} catch (Exception e) {
			System.out.print("* An exception has occurred: ");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Starts the entry-point command line interface.
	 * Handles the commands entered by users and map these commands to the runnables in the other CLI classes.
	 */
	private void start() {
		int command;
		BaseManager manager;
		ConsoleHelper cs = new ConsoleHelper();
		cs.sendWelcome(mainCLIOptions);

		while ((command = cs.getInt("Enter command")) != -1) {
			while (!classMap.containsKey(command)) {
				System.out.println("Invalid command.");
				if ((command = cs.getInt("Enter command")) != -1) {
					return;
				}
			}

			try {
				manager = classMap.get(command).getDeclaredConstructor(Restaurant.class).newInstance(restaurant);
				manager.getOptionRunnables()[commandMap.get(command)].run();
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
				System.out.println("Unable to invoke command.");
			}
			cs.sendWelcome(mainCLIOptions);
		}
	}

	/**
	 * Maps input commands to console displays and their respective managers.
	 * @param manager - Manager to map commands to.
	 */
	private void map(BaseManager manager) {
		mainCLIOptions.add(manager.getMainCLIOptions());
		for (int count = 0; count < manager.getOptionRunnables().length; count++) {
			classMap.put(commandIndex, manager.getClass());
			commandMap.put(commandIndex, count);
			commandIndex++;
		}
	}

	public static void main(String[] args) {
		(new Main()).initCLI();
	}

	/*
	public static void main1(String[] args) {
		if (!FileIOHandler.buildDirectory()) {
			return;
		}

		int choice;
		int backInput = MenuConstants.OPTIONS_TERMINATE;
		OrderManager orderManager = new OrderManager();
		MenuManager menuManager = new MenuManager();
		//PromotionManager promotionManager = new PromotionManager();
		ReservationManager reservationManager = new ReservationManager();
		TableManager tableManager = new TableManager();
		Scanner s = new Scanner(System.in);
		ScannerHandler sc = new ScannerHandler(s);

		do {
			MenuFactory.printMenu(MenuConstants.APP_TITLE, MenuConstants.OPTIONS_MAIN);
			choice = MenuFactory.loopChoice(sc, MenuConstants.OPTIONS_MAIN.length);

			if (choice != backInput) {
				String className = AppConstants.CODE_NAMES[choice];
				String methodName = MenuConstants.MENU_HANDLER_CALL_METHOD;

				if (choice == 2) {
					orderManager.choices(s,tableManager,promotionManager,menuManager);
				}

				else {
					try {
						Class c = Class.forName(className);
						Method method = c.getDeclaredMethod(methodName, ScannerHandler.class);
						method.invoke(null, sc);
					}

					// die lor
					catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
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
				orderManager.choices(s,tableManager);
			
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
	*/
}
