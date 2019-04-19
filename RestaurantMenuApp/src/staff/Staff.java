package staff;

import core.RestaurantData;

/**
 * Staff entity class
 */
public class Staff extends RestaurantData {
    /**
     * Name of the staff
     */
    private String name;

    /**
     * Title of the staff
     */
    private String title;

    /**
     * Creates a new staff with the specified parameters.
     * @param id ID of the staff - usually auto-generated using the restaurant's unique ID generator. Passed into the parent class.
     * @param name Name of the staff.
     * @param title Title of the staff.
     */
    Staff(int id, String name, String title) {
        super(id);
        this.name = name;
        this.title = title;
    }

    /**
     * Updates the name of the staff.
     * @param name New name of the staff.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of the staff.
     * @return Name of the staff.
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the title of the staff.
     * @param title New title of the staff.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Retrieves the title of the staff.
     * @return Title of the staff.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
    @Override
    public String toFileString() {
        return getId() + " // " + getName() + " // " + getTitle();
    }

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
    @Override
    public String toDisplayString() {
        return toFileString();
    }
}
