package menu;

import java.math.BigDecimal;

public class AlaCarteItem extends MenuItem {
	private String category;

	AlaCarteItem(int id, String name, BigDecimal price, String category) {
		super(id, name, price);
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	void update(String name, BigDecimal price, String category) {
		setName(name);
		setPrice(price);
		this.category = category;
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
