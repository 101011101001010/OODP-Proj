package menu;

import java.math.BigDecimal;

public class AlaCarteItem extends MenuItem {
	private String category;

	AlaCarteItem(int id, String name, BigDecimal price, String category) {
		super(id, name, price);
		this.category = category;
	}

	void setCategory(String category) {
		this.category = category;
	}

	public boolean matchCategory(String category) {
		return this.category.equalsIgnoreCase(category);
	}

	public String getCategory() {
		return category;
	}

	@Override
	public String toDisplayString() {
		return getName() + " // " + getPrice();
	}

	@Override
	public String toFileString() {
		return getId() + " // " + getName() + " // " + getPrice() + " // " + getCategory();
	}
}
