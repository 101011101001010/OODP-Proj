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
import java.util.*;

public class Main {
    private Map<Integer, Class<? extends BaseManager>> commandToClassMap;
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

    private void checkFilesOrBreak() {
        try {
            (new FileIO()).checkFiles();
        } catch (IOException e) {
            System.out.println("File check has failed:");
            System.out.println(e.getMessage());
        }
    }

    private void mapManagers() {
        try {
            map(new MenuManager(restaurant));
            map(new OrderManager(restaurant));
            map(new TableManager(restaurant));
            map(new StaffManager(restaurant));
        } catch (BaseManager.ManagerInitFailedException e) {
            System.out.println("Manager initialisation failure:");
            System.out.println(e.getMessage());
        }
    }

    private void checkDataTypeExists() {
        for (DataType dataType : DataType.values()) {
            if (!restaurant.checkDataTypeExists(dataType)) {
                System.out.println("Class not registered for data type: " + dataType);
            }
        }
    }

    private void start() {
        ConsoleHelper cs = new ConsoleHelper();
        int staffIndex = staffLogin(cs);

        while (staffIndex != 0) {
            restaurant.setSessionStaffId(restaurant.getDataFromIndex(DataType.STAFF, staffIndex - 1).getId());
            cs.sendWelcome(mainCliOptions);
            int command = executeCommand(cs, cs.getInt("Select a function", -1, commandIndex - 1));
            if (command == -1) {
                return;
            }

            staffIndex = staffLogin(cs);
        }
    }

    private int staffLogin(ConsoleHelper cs) {
        List<String> staffDisplay = (new StaffManager(restaurant)).getStaffNames();
        cs.setMaxLength(60);
        cs.printChoicesSimple("Command // Staff Account", staffDisplay, new String[]{"Exit program"});
        return cs.getInt("Select staff account to begin", 0, staffDisplay.size());
    }

    private int executeCommand(ConsoleHelper cs, int command) {
        while (command != 0 && command != -1) {
            cs.clearCmd();
            Objects.requireNonNull(getManagerFromClassMap(command)).getOptionRunnables()[commandToIndexMap.get(command)].run();
            cs.sendWelcome(mainCliOptions);
            command = cs.getInt("Select a function", -1, commandIndex - 1);
        }

        return command;
    }

    private BaseManager getManagerFromClassMap(int command) {
        try {
            return commandToClassMap.get(command).getDeclaredConstructor(Restaurant.class).newInstance(restaurant);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("Failed to get manager from class map.");
            System.out.println(e.getMessage());
            return null;
        }
    }

    private void map(BaseManager manager) throws BaseManager.ManagerInitFailedException {
        if (manager.getMainCLIOptions().length != manager.getOptionRunnables().length) {
            System.out.println("CLI options and runnables mismatch for " + manager.getClass().getSimpleName() + ".");
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
            Main main = new Main();
            main.checkFilesOrBreak();
            main.mapManagers();
            main.checkDataTypeExists();
            main.start();
        } catch (Exception e) {
            System.out.println("* An unexpected exception has occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
