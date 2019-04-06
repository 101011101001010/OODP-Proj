package client;

import enums.DataType;
import menu.MenuManager;
import staff.Staff;
import staff.StaffManager;
import tables.TableManager;
import tools.ConsoleHelper;
import tools.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private Map<Integer, Class<? extends DataManager>> commandToClassMap;
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
    }

    private void start() {
        final ConsoleHelper cs = new ConsoleHelper();
        int staffIndex = staffLogin(cs);
        cs.clearCmd();

        while (staffIndex != 0) {
            Optional<Staff> oStaff = restaurant.getDataFromIndex(DataType.STAFF, staffIndex - 1);

            if (oStaff.isEmpty()) {
                return;
            }

            restaurant.setSessionStaffId(oStaff.get().getId());
            cs.sendWelcome(List.copyOf(mainCliOptions));
            int command = executeCommand(cs, cs.getInt("Select a function", -1, commandIndex - 1));

            if (command == -1) {
                return;
            }

            cs.clearCmd();
            staffIndex = staffLogin(cs);
        }
    }

    private int staffLogin(ConsoleHelper cs) {
        final List<String> staffNameList = (new StaffManager(restaurant)).getStaffNames();
        final List<String> choiceList = cs.formatChoiceList(staffNameList, Collections.singletonList("Exit Program"));
        cs.printTable("", "Command // Staff Account", choiceList, true);
        return cs.getInt("Select staff account to begin", 0, staffNameList.size());
    }

    private int executeCommand(ConsoleHelper cs, int command) {
        while (command != 0 && command != -1) {
            cs.clearCmd();
            Objects.requireNonNull(getManagerFromClassMap(command)).getOptionRunnables()[commandToIndexMap.get(command)].run();
            cs.sendWelcome(List.copyOf(mainCliOptions));
            command = cs.getInt("Select a function", -1, commandIndex - 1);
        }

        return command;
    }

    private DataManager getManagerFromClassMap(int command) {
        try {
            return commandToClassMap.get(command).getDeclaredConstructor(Restaurant.class).newInstance(restaurant);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Log.error("Failed to get manager from class map: " + e.getMessage());
            return null;
        }
    }

    private void hookManagerToMain(DataManager manager) {
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
            Log.notice("Something unexpectedly went wrong: " + e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.writeToFile("Something unexpectedly went wrong: " + e.getMessage() + "\n" + pw.toString() + "\n");
        }
    }
}
