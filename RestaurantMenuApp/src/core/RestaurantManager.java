package core;

import tools.InputHelper;

public abstract class RestaurantManager {
    private Restaurant restaurant;
    private InputHelper in;

    public RestaurantManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.in = new InputHelper();
    }

    protected Restaurant getRestaurant() {
        return restaurant;
    }

    protected InputHelper getInputHelper() {
        return in;
    }

    public void init() throws Exception {}
    public abstract String[] getMainCLIOptions();
    public abstract Runnable[] getOptionRunnables();
}