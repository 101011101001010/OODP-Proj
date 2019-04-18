package core;

import tools.InputHelper;

public abstract class RestaurantManager {
    private Restaurant restaurant;
    private InputHelper in;

    public RestaurantManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.in = new InputHelper();
    }

    /**
     * Returns the restaurant object stored in the base class
     * @return restaurant object
     */
    protected Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     * Returns the input helper object stored initialised in the base class
     * @return inputHelper object
     */
    protected InputHelper getInputHelper() {
        return in;
    }

    /**
     * Initialisation method for the manager -> only runs at application start-up. Not compulsory for classes that extend RestaurantManager to implement this.
     * @throws Exception
     */
    public void init() throws Exception {}

    /**
     * Returns an array of menu option strings to be displayed by the application for users to select from. Acts as the text-version of the entry points into the managers.
     * @return self-defined array of menu option strings
     */
    public abstract String[] getMainCLIOptions();

    /**
     * Returns an array of runnable / methods. These are the entry points into the managers from the main function.
     * @return self-defined array of runnables / methods
     */
    public abstract Runnable[] getOptionRunnables();
}