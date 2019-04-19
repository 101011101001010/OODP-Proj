package menu;

import java.math.BigDecimal;

/**
 * Ala-carte menu item entity class
 */
public class AlaCarteItem extends MenuItem {
	/**
	 * Category of the ala-carte item.
	 */
	private String category;

	/**
	 * Creates a new ala-carte item object with the specified parameters.
	 * @param id ID of the item - usually auto-generated using the restaurant's unique ID generator. Passed into the parent class.
	 * @param name Name of the item to be passed into the parent class.
	 * @param price Price of the item to be passed into the parent class.
	 * @param category Category of the item.
	 */
	AlaCarteItem(int id, String name, BigDecimal price, String category) {
		super(id, name, price);
		this.category = category;
	}

	/**
	 * Updates the category of the item.
	 * @param category New category of the item.
	 */
	void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Checks if the specified category matches the item's category, ignoring case.
	 * @param category Category to check against.
	 * @return True / False
	 */
	public boolean matchCategory(String category) {
		return this.category.equalsIgnoreCase(category);
	}

	/**
	 * Retrieves the category of the item.
	 * @return Category of the item.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Please see the method description in RestaurantData.
	 * @see core.RestaurantData
	 */
	@Override
	public String toDisplayString() {
		return getName() + " // " + getPrice();
	}

	/**
	 * Please see the method description in RestaurantData.
	 * @see core.RestaurantData
	 */
	@Override
	public String toFileString() {
		return getId() + " // " + getName() + " // " + getPrice() + " // " + getCategory();
	}
}
