package client;

import enums.DataType;
import tools.ConsoleHelper;

import java.io.IOException;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class DataManager {
    private Restaurant restaurant;
    private ConsoleHelper cs;

    public DataManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.cs = new ConsoleHelper();
    }

    protected Restaurant getRestaurant() {
        return restaurant;
    }

    protected ConsoleHelper getCs() {
        return cs;
    }

    public abstract void init() throws IOException;
    public abstract String[] getMainCLIOptions();
    public abstract Runnable[] getOptionRunnables();
}