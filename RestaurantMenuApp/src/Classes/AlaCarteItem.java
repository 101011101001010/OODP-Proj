package Classes;

import java.math.BigDecimal;

public class AlaCarteItem extends MenuItem {
	private String description;
	private String category;

	public AlaCarteItem(int id, String name, BigDecimal price, String description, String category) {
		super(id, name, price);
		this.description = description;
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public void update(String name, BigDecimal price, String description, String category) {
		setName(name);
		setPrice(price);
		this.description = description;
		this.category = category;
	}

	@Override
	public String toDisplayString() {
		return toString();
	}

	@Override
	public String toString() {
		return getId() + " // " + getName() + " // " + getPrice() + " // " + getDescription() + " // " + getCategory();
	}
}
