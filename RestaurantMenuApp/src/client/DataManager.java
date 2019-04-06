package client;

import tools.ConsoleHelper;

public abstract class DataManager {
    private Restaurant restaurant;
    private ConsoleHelper cm;

    public DataManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.cm = new ConsoleHelper();
    }

    protected Restaurant getRestaurant() {
        return restaurant;
    }

    protected ConsoleHelper getCm() {
        return cm;
    }

    public abstract void init();
    public abstract String[] getMainCLIOptions();
    public abstract Runnable[] getOptionRunnables();
}