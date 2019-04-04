package client;

import enums.DataType;
import menu.MenuManager;
import order.OrderManager;
import staff.StaffManager;
import tables.TableManager;
import tools.ConsoleHelper;
import tools.FileIO;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private Map<String, Class<? extends BaseManager>> classMap;
    private Map<String, Integer> commandMap;
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
    private void initCLI() {
        try {
            (new FileIO()).checkFiles();
        } catch (IOException e) {
            System.out.println("File check failed: " + e.getMessage());
            return;
        }

        try {
            map(new MenuManager(restaurant));
            map(new OrderManager(restaurant));
            map(new TableManager(restaurant));
            map(new StaffManager(restaurant));
        } catch (BaseManager.ManagerInitFailedException e) {
            System.out.println(e.getMessage());
            return;
        }

        for (DataType dataType : DataType.values()) {
            if (!restaurant.checkDataTypeExists(dataType)) {
                System.out.println("Class not registered for data type: " + dataType);
            }
        } start();
    }

    /**
     * Starts the entry-point command line interface.
     * Handles the commands entered by users and map these commands to the runnables in the other CLI classes.
     */
    private void start() {
        String command;
        BaseManager manager;
        ConsoleHelper cs = new ConsoleHelper();

        int staffIndex;
        do {
            List<String> staffDisplay = (new StaffManager(restaurant)).getStaffNames();
            cs.setMaxLength(60);
            cs.printChoicesSimple("Command // Staff Account", staffDisplay, new String[]{"Exit program"});
            if ((staffIndex = cs.getInt("Select staff account to begin", staffDisplay.size()) - 1) == -2) {
                return;
            }

            restaurant.setSessionStaffId(restaurant.getDataFromIndex(DataType.STAFF, staffIndex).getId());
            cs.clearCmd();

            do {
                cs.sendWelcome(mainCLIOptions);
                command = cs.getString("Select a function");

                if (command.equalsIgnoreCase("-1")) {
                    break;
                }

                if (command.equalsIgnoreCase("-2")) {
                    return;
                }

                try {
                    cs.clearCmd();
                    manager = classMap.get(command).getDeclaredConstructor(Restaurant.class).newInstance(restaurant);
                    manager.getOptionRunnables()[commandMap.get(command)].run();
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    System.out.println("Command failed to execute.");
                } catch (NullPointerException e) {
                    System.out.println("Invalid command.");
                }
            } while (!command.equalsIgnoreCase("-1"));

            cs.clearCmd();
        } while (true);
    }

    /**
     * Maps input commands to console displays and their respective managers.
     * @param manager - Manager to map commands to.
     */
    private void map(BaseManager manager) throws BaseManager.ManagerInitFailedException {
        if (manager.getMainCLIOptions().length != manager.getOptionRunnables().length) {
            System.out.println("CLI options and runnables mismatch for " + manager.getClass().getSimpleName() + ".");
            return;
        }

        manager.init();
        mainCLIOptions.add(manager.getMainCLIOptions());
        for (int count = 0; count < manager.getOptionRunnables().length; count++) {
            classMap.put(Integer.toString(commandIndex), manager.getClass());
            commandMap.put(Integer.toString(commandIndex), count);
            commandIndex++;
        }
    }

    public static void main(String[] args) {
        try {
            (new Main()).initCLI();
        } catch (Exception e) {
            System.out.println("* An unexpected exception has occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
