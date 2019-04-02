package client;

import client.enums.Op;
import tools.ConsoleHelper;
import tools.Pair;

public abstract class BaseManager {
    private Restaurant restaurant;
    private ConsoleHelper cs;

    public BaseManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.cs = new ConsoleHelper();
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public ConsoleHelper getCs() {
        return cs;
    }

    public abstract Pair<Op, String> init();
    public abstract String[] getMainCLIOptions();
    public abstract Runnable[] getOptionRunnables();
}