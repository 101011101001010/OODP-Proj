package core;

import tools.InputHelper;

public abstract class RestaurantDataManager {
    private Restaurant restaurant;
    private InputHelper in;

    public RestaurantDataManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.in = new InputHelper();
    }

    protected Restaurant getRestaurant() {
        return restaurant;
    }

    protected InputHelper getInputHelper() {
        return in;
    }

    public abstract void init();
    public abstract String[] getMainCLIOptions();
    public abstract Runnable[] getOptionRunnables();
}