package client;

import tables.TableManager;
import menu.MenuManager;
import order.OrderManager;
import staff.StaffManager;
import tools.ConsoleHelper;
import tools.FileIO;

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
	}

	/**
	 * Initialises the entry command line interface with options defined in other CLI classes.
	 * Entry-point runnables are also obtained from the CLI classes to map the commands to.
	 */
	private void initCLI() throws Exception {
		(new FileIO()).checkFiles();
		BaseManager manager;

		manager = new MenuManager(restaurant);
		manager.init();
		map(manager);

		manager = new TableManager(restaurant);
		manager.init();
		map(manager);

		manager = new OrderManager(restaurant);
		map(manager);

		manager = new StaffManager(restaurant);
		manager.init();
		map(manager);

		start();
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
			} catch (Exception e) {
				System.out.print("* An exception has occurred: ");
				System.out.println(e.getMessage());
			}

			cs.sendWelcome(mainCLIOptions);
		}
	}

	/**
	 * Maps input commands to console displays and their respective managers.
	 * @param manager - Manager to map commands to.
	 */
	private void map(BaseManager manager) throws Exception {
		if (manager.getMainCLIOptions().length != manager.getOptionRunnables().length) {
			throw (new Exception("CLI options and runnables mismatch for " + manager.getClass().getCanonicalName() + "."));
		}

		mainCLIOptions.add(manager.getMainCLIOptions());
		for (int count = 0; count < manager.getOptionRunnables().length; count++) {
			classMap.put(commandIndex, manager.getClass());
			commandMap.put(commandIndex, count);
			commandIndex++;
		}
	}

	public static void main(String[] args) {
		try {
			(new Main()).initCLI();
		} catch (Exception e) {
			System.out.print("* A fatal exception has occurred: ");
			System.out.println(e.getMessage());
		}
	}
}
