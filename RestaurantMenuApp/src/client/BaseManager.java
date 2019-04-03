package client;

import tools.ConsoleHelper;

public abstract class BaseManager {
    private Restaurant restaurant;
    private ConsoleHelper cs;

    public BaseManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.cs = new ConsoleHelper();
    }

    protected Restaurant getRestaurant() {
        return restaurant;
    }

    protected ConsoleHelper getCs() {
        return cs;
    }

    public abstract void init() throws ManagerInitFailedException;
    public abstract String[] getMainCLIOptions();
    public abstract Runnable[] getOptionRunnables();

    public class ManagerInitFailedException extends Exception {
        public ManagerInitFailedException(Object o, String rootMessage) {
            super(o.getClass().getCanonicalName() + " initialisation failed: " + rootMessage);
        }
    }
}