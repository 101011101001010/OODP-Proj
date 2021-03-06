package core;

/**
 * Blueprint class for every restaurant data object.
 * Objects must extend this class to make use of the Restaurant instance to store and manipulate data.
 */
public abstract class RestaurantData {
    /**
     * Mandatory property for easier data storage and manipulation.
     */
    private int id;

    /**
     * Creates a new restaurant data object, or of any of its sub-class, with the supplied ID.
     * @param id ID of the object, can be generated by a restaurant instance
     */
    public RestaurantData(int id) {
        this.id = id;
    }

    /**
     * Checks if the given ID matches this data's ID
     * @param id ID to check
     * @return True / False
     */
    public boolean matchId(int id) {
        return (this.id == id);
    }

    /**
     * Returns the ID of this data
     * @return data ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns a self-defined string of this data object's data to be saved into its respective text file, conforming to a format that its manager can process.
     * @return string of this data object's data for file writing
     */
    public abstract String toFileString();

    /**
     * Returns a self-defined string of this data object's data to be printed in a table for display, conforming to a format that can be processed by the ConsolePrinter
     * @return string of this data object's data for display
     * @see tools.ConsolePrinter
     */
    public abstract String toDisplayString();
}
