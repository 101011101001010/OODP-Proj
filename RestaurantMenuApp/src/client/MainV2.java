package client;

import Classes.MenuManager;
import client.enums.Op;
import tools.ConsoleHelper;
import tools.FileIO;
import tools.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainV2 {
    private Map<Integer, Class<? extends BaseManager>> classMap;
    private Map<Integer, Integer> commandMap;
    private Restaurant restaurant;
    private int commandIndex;
    private List<String[]> mainCLIOptions;

    private MainV2() {
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
            map(manager);

            //manager = new StaffManager(restaurant);
            //map(manager);

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
        (new MainV2()).initCLI();
    }
}
