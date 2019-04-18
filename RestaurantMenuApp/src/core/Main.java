package core;

import Classes.RevenueReader;
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

public class Main {
    private Map<Integer, Class<? extends RestaurantManager>> commandToClassMap;
    private Map<Integer, Integer> commandToIndexMap;
    private List<String[]> mainCliOptions;
    private Restaurant restaurant;
    private int commandIndex = 1;

    private Main() {
        this.restaurant = new Restaurant();
        commandToClassMap = new HashMap<>();
        commandToIndexMap = new HashMap<>();
        mainCliOptions = new ArrayList<>();
    }

    /**
     * Consolidatory method that hooks up RestaurantManagers to the main function. RestaurantManagers may be substituted with objects of any of its sub-classes.
     * @throws Exception contains error messages that may be thrown when initialising each managers
     */
    private void hookManagers() throws Exception {
        hookManagerToMain(new MenuManager(restaurant));
        hookManagerToMain(new TableManager(restaurant));
        hookManagerToMain(new StaffManager(restaurant));
        hookManagerToMain(new RevenueReader(restaurant));
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
            Logger.getAnonymousLogger().log(Level.WARNING, "CLI options and runnables mismatch for " + manager.getClass().getSimpleName() + ".");
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
