package client;

import enums.DataType;
import menu.MenuManager;
import staff.StaffManager;
import tables.TableManager;
import tools.ConsoleHelper;
import tools.FileIO;
import tools.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private Map<Integer, Class<? extends DataManager>> commandToClassMap;
    private Map<Integer, Integer> commandToIndexMap;
    private List<String[]> mainCliOptions;
    private Restaurant restaurant;
    private int commandIndex;

    private Main() {
        this.restaurant = new Restaurant();
        commandToClassMap = new HashMap<>();
        commandToIndexMap = new HashMap<>();
        mainCliOptions = new ArrayList<>();
        commandIndex = 1;
    }

    private void mapManagers() throws IOException {
        map(new MenuManager(restaurant));
        map(new TableManager(restaurant));
        map(new StaffManager(restaurant));
    }

    private void start() {
        final ConsoleHelper cs = new ConsoleHelper();
        int staffIndex = staffLogin(cs);
        cs.clearCmd();

        while (staffIndex != 0) {
            try {
                restaurant.setSessionStaffId(restaurant.getDataFromIndex(DataType.STAFF, staffIndex - 1).getId());
                cs.sendWelcome(List.copyOf(mainCliOptions));
                int command = executeCommand(cs, cs.getInt("Select a function", -1, commandIndex - 1));
                if (command == -1) {
                    return;
                }

                cs.clearCmd();
                staffIndex = staffLogin(cs);
            } catch (RuntimeException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "An unexpected exception has occurred: " + e.getMessage());
                e.printStackTrace();
            }
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
            Logger.getAnonymousLogger().log(Level.WARNING, "Failed to get manager from class map: " + e.getMessage());
            return null;
        }
    }

    private void map(DataManager manager) throws IOException {
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

    private void checkDataTypeExists() {
        for (DataType dataType : DataType.values()) {
            if (!restaurant.checkDataTypeExists(dataType)) {
                Log.warning(this, "No class registered for data type: " + dataType);
            }
        }
    }

    public static void main(String[] args) {
        try {
            final Main main = new Main();
            (new FileIO()).checkFiles();
            main.mapManagers();
            main.checkDataTypeExists();
            main.start();
        } catch (IOException e) {
            System.out.println();
            Logger.getAnonymousLogger().log(Level.SEVERE, "File IO error: " + e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            Logger.getAnonymousLogger().log(Level.SEVERE, "*An unexpected exception has occurred: " + e.getMessage());
            System.out.println();
            e.printStackTrace();
        }
    }
}
