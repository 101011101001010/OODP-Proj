package core;

import enums.DataType;
import menu.MenuManager;
import staff.Staff;
import staff.StaffManager;
import tables.TableManager;
import tools.ConsolePrinter;
import tools.InputHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private Map<Integer, Class<? extends RestaurantDataManager>> commandToClassMap;
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

    private void hookManagers() {
        hookManagerToMain(new MenuManager(restaurant));
        hookManagerToMain(new TableManager(restaurant));
        hookManagerToMain(new StaffManager(restaurant));
        //hookManagerToMain(new Classes.TableManager(restaurant));
    }

    private void start() {
        final InputHelper in = new InputHelper();
        int staffIndex = staffLogin(in);
        ConsolePrinter.clearCmd();

        while (staffIndex != 0) {
            Optional<Staff> oStaff = restaurant.getDataFromIndex(DataType.STAFF, staffIndex - 1);

            if (oStaff.isEmpty()) {
                return;
            }

            restaurant.setSessionStaffId(oStaff.get().getId());
            ConsolePrinter.sendWelcome(List.copyOf(mainCliOptions));
            int command = executeCommand(in, in.getInt("Select a function", -1, commandIndex - 1));

            if (command == -1) {
                return;
            }

            ConsolePrinter.clearCmd();
            staffIndex = staffLogin(in);
        }
    }

    private int staffLogin(InputHelper in) {
        final List<String> staffNameList = (new StaffManager(restaurant)).getStaffNames();
        final List<String> choiceList = ConsolePrinter.formatChoiceList(staffNameList, Collections.singletonList("Exit Program"));
        ConsolePrinter.printTable("", "Command // Staff Account", choiceList, true);
        return in.getInt("Select staff account to begin", 0, staffNameList.size());
    }

    private int executeCommand(InputHelper in, int command) {
        while (command != 0 && command != -1) {
            ConsolePrinter.clearCmd();
            Objects.requireNonNull(getManagerFromClassMap(command)).getOptionRunnables()[commandToIndexMap.get(command)].run();
            ConsolePrinter.sendWelcome(List.copyOf(mainCliOptions));
            command = in.getInt("Select a function", -1, commandIndex - 1);
        }

        return command;
    }

    private RestaurantDataManager getManagerFromClassMap(int command) {
        try {
            return commandToClassMap.get(command).getDeclaredConstructor(Restaurant.class).newInstance(restaurant);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Failed to get manager from class map: " + e.getMessage());
            return null;
        }
    }

    private void hookManagerToMain(RestaurantDataManager manager) {
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

    public static void main(String[] args) {
        try {
            final Main main = new Main();
            main.hookManagers();
            main.start();
        } catch (RuntimeException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Something unexpectedly went wrong: " + e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            ConsolePrinter.logToFile("Something unexpectedly went wrong: " + e.getMessage() + "\n" + pw.toString() + "\n");
        }
    }
}