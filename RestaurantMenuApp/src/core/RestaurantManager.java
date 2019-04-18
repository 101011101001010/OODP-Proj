package core;

import tools.InputHelper;

/**
 * Blueprint class for every restaurant data object manager.
 * Managers must extend this class to make use of the Restaurant instance to store and manipulate data.
 */
public abstract class RestaurantManager {
    /**
     * Holds the restaurant instance for the current application session.
     */
    private Restaurant restaurant;

    /**
     * Holds the inputHelper instance for the current application session.
     */
    private InputHelper in;

    /**
     * Creates a new manager instance, taking in a restaurant instance created for the application session.
     * Initialises an inputHelper object to assist in input collection through the CLI.
     * @param restaurant a initialised restaurant instance
     */
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
     * Initialisation method for the manager - only runs at application start-up. Not compulsory for classes that extend RestaurantManager to implement this.
     * @throws Exception errors that occurred while initialising the manager
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