package menu;

import core.RestaurantData;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Menu item entity class
 */
public class MenuItem extends RestaurantData {
    /**
     * Name of the item.
     */
    private String name;

    /**
     * Price of the item.
     */
    private BigDecimal price;

    /**
     * Creates a new menu item with the specified parameters.
     * @param id ID of the item - usually auto-generated using the restaurant's unique ID generator. Passed into the parent class.
     * @param name Name of the item.
     * @param price Price of the item.
     */
    MenuItem(int id, String name, BigDecimal price) {
        super(id);
        this.name = name;
        this.price = price;
        this.price = this.price.setScale(2, RoundingMode.FLOOR);
    }

    /**
     * Updates the name of the item.
     * @param name New name of the item.
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of the item.
     * @return Name of the item.
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the price of the item.
     * @param price New price of the item in BigDecimal.
     */
    void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Retrieves the price of the item.
     * @return Price of the item in BigDecimal.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
    public String toFileString() {
        return (getId() + " // " + name + " // " + price);
    }

    /**
     * Please see the method description in RestaurantData.
     * @see core.RestaurantData
     */
    public String toDisplayString() {
        return (name + " // " + price);
    }


}
