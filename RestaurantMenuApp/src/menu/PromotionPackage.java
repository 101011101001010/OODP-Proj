package menu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PromotionPackage extends MenuItem {
	private List<AlaCarteItem> alaCarteItems;

	PromotionPackage(int id, String name, BigDecimal price, List<AlaCarteItem> alaCarteItems) {
		super(id, name, price);
		this.alaCarteItems = alaCarteItems;
	}

	public List<AlaCarteItem> getAlaCarteItems() {
		return alaCarteItems;
	}

	void update(String name) {
		setName(name);
	}

	void refreshPrice() {
		BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);

		for (AlaCarteItem item : alaCarteItems) {
			price = price.add(item.getPrice());
		}

		setPrice(price);
	}

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
