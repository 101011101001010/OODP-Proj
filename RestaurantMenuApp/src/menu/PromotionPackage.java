package menu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Promotional package menu item entity class
 */
public class PromotionPackage extends MenuItem {
	/**
	 * List of ala-carte items in the package.
	 */
	private List<AlaCarteItem> alaCarteItems;

	/**
	 * Creates a new promotional package with the specified parameters.
	 * @param id ID of the package - usually auto-generated using the restaurant's unique ID generator. Passed into the parent class.
	 * @param name Name of the package to be passed into the parent class.
	 * @param price Price of the package to be passed into the parent class.
	 * @param alaCarteItems List of ala-carte items. If null, an empty list will be assigned instead.
	 */
	PromotionPackage(int id, String name, BigDecimal price, List<AlaCarteItem> alaCarteItems) {
		super(id, name, price);

		if (alaCarteItems == null) {
			alaCarteItems = new ArrayList<>();
		}

		this.alaCarteItems = alaCarteItems;
	}

	/**
	 * Retrieves the list of ala-carte items in the package.
	 * @return
	 */
	List<AlaCarteItem> getAlaCarteItems() {
		return alaCarteItems;
	}

	/**
	 * Refreshes the price of the package based on the price of its contents.
	 */
	void refreshPrice() {
		BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);

		for (AlaCarteItem item : alaCarteItems) {
			price = price.add(item.getPrice());
		}

		setPrice(price);
	}

	/**
	 * Please see the method description in RestaurantData.
	 * @see core.RestaurantData
	 */
	@Override
	public String toDisplayString() {
		StringBuilder sb = new StringBuilder(getName() + "\n");

		for (int index = 0; index < alaCarteItems.size(); index++) {
			sb.append("- ").append(alaCarteItems.get(index).getName());

			if (index != (alaCarteItems.size() - 1)) {
				sb.append("\n");
			}
		}

		sb.append(" // ").append(getPrice());
		return sb.toString();
	}

	/**
	 * Please see the method description in RestaurantData.
	 * @see core.RestaurantData
	 */
	@Override
	public String toFileString() {
		String head = super.toFileString() + " // ";
		StringBuilder sb = new StringBuilder(head);

		for (int index = 0; index < alaCarteItems.size(); index++) {
			sb.append(alaCarteItems.get(index).getId());

			if (index != (alaCarteItems.size() - 1)) {
				sb.append("--");
			}
		}

		return sb.toString();
	}
}
