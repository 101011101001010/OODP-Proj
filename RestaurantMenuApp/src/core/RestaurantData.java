package core;

public abstract class RestaurantData {
    private int id;

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
