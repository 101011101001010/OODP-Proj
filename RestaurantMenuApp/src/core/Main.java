package core;

import revenue.RevenueManager;
import enums.DataType;
import menu.MenuManager;
import staff.Staff;
import staff.StaffManager;
import tables.TableManager;
import tools.ConsolePrinter;
import tools.InputHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The entry point of the application. Main extends the capability of the application by hooking up classes that extends RestaurantManager, as long as the managers provide an entry point into them. See RestaurantManager for details.
 * @see RestaurantManager
 */
public class Main {
    /**
     * Maps command indices to the manager containing the runnable to be called by the command.
     * @see RestaurantManager
     */
    private Map<Integer, Class<? extends RestaurantManager>> commandToClassMap;

    /**
     * Maps command indices to the indices of a runnable array in the respective manager classes.
     * @see RestaurantManager
     */
    private Map<Integer, Integer> commandToIndexMap;

    /**
     * List of string arrays to be displayed on the main CLI. One array per manager.
     * @see RestaurantManager
     */
    private List<String[]> mainCliOptions;

    /**
     * Restaurant instance required for this class to function.
     */
    private Restaurant restaurant;

    /**
     * Index used for mapping commands.
     */
    private int commandIndex = 1;

    /**
     * Initialises the list and hash-maps used in this class.
     * Also initialises a restaurant object that will be used for the lifespan duration of the application.
     */
    private Main() {
        this.restaurant = new Restaurant();
        commandToClassMap = new HashMap<>();
        commandToIndexMap = new HashMap<>();
        mainCliOptions = new ArrayList<>();
    }

    /**
     * Hooks up RestaurantManagers to the main function. RestaurantManagers may be substituted with objects of any of its sub-classes.
     * Changes are made primarily to this function when extending the application with new managers:
     * Add a new 'hookManagerToMain(newManagerInstance)' line to hook the particular manager.
     * @throws Exception contains error messages that may be thrown when initialising each managers
     */
    private void hookManagers() throws Exception {
        hookManagerToMain(new MenuManager(restaurant));
        hookManagerToMain(new TableManager(restaurant));
        hookManagerToMain(new StaffManager(restaurant));
        hookManagerToMain(new RevenueManager(restaurant));
    }

    /**
     * Starts the CLI prompts for the application, looping infinitely until the user exits the program.
     */
    private void start() {
        final InputHelper in = new InputHelper();
        int staffIndex;
        int command;

        do {
            ConsolePrinter.clearCmd();

            try {
                staffIndex = staffLogin(in) - 1;
                if (staffIndex == -1) {
                    return;
                }

                restaurant.setSessionStaffId(restaurant.getDataFromIndex(DataType.STAFF, staffIndex).getId());
            } catch (Exception e) {
                ConsolePrinter.logToFile(e.getMessage(), e);
            }

            do {
                ConsolePrinter.clearCmd();
                ConsolePrinter.sendWelcome(List.copyOf(mainCliOptions));
                command = in.getInt("Select a function", -1, commandIndex - 1);
                if (command == -1 || command == 0) {
                    break;
                }

                try {
                    Objects.requireNonNull(commandToClassMap.get(command).getDeclaredConstructor(Restaurant.class).newInstance(restaurant)).getOptionRunnables()[commandToIndexMap.get(command)].run();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Failed to get manager from class map: " + e.getMessage());
                } catch (Exception e) {
                    ConsolePrinter.logToFile("Unexpected error, please check logs: " + e.getMessage(), e);
                }
            } while (true);

            if (command == -1) {
                return;
            }
        } while (true);
    }

    /**
     * Captures the staff ID for the current restaurant session.
     * @param in initialised inputHelper object
     * @return staff ID
     * @throws Exception contains error messages that may be thrown by StaffManager
     */
    private int staffLogin(InputHelper in) throws Exception {
        final List<Staff> staffList = restaurant.getDataList(DataType.STAFF);
        final List<String> staffNameList = staffList.stream().map(Staff::getName).collect(Collectors.toList());
        final List<String> choiceList = ConsolePrinter.formatChoiceList(staffNameList, Collections.singletonList("Exit Program"));
        ConsolePrinter.printTable("", "Command // Staff Account", choiceList, true);
        return in.getInt("Select staff account to begin", 0, staffNameList.size());
    }

    /**
     * Hooks up individual managers to the main function
     * @param manager instance of manager to be hooked
     * @throws Exception contains error messages that may be thrown when initialising the manager
     */
    private void hookManagerToMain(RestaurantManager manager) throws Exception {
        if (manager.getMainCLIOptions().length != manager.getOptionRunnables().length) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "CLI options and runnables mismatch for " + manager.getClass().getSimpleName() + ".");
            return;
        }

        manager.init();
        mainCliOptions.add(manager.getMainCLIOptions());

        for (int index = 0; index < manager.getOptionRunnables().length; index++) {
            commandToClassMap.put(commandIndex, manager.getClass());
            commandToIndexMap.put(commandIndex, index);
            commandIndex++;
        }
    }

    /**
     * public static void main(String[] args)
     * @param args CLI arguments
     */
    public static void main(String[] args) {
        try {
            final Main main = new Main();
            main.hookManagers();
            main.start();
        } catch (Exception e) {
            ConsolePrinter.logToFile("Unexpected fatal error, please check logs: " + e.getMessage(), e);
        }
    }
}
