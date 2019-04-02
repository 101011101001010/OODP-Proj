package Classes;

import java.math.BigDecimal;
import java.util.List;

public class PromoItem extends MenuItem {
	private List<AlaCarteItem> alaCarteItems;

	PromoItem(int id, String name, BigDecimal price, List<AlaCarteItem> alaCarteItems) {
		super(id, name, price);
		this.alaCarteItems = alaCarteItems;
	}

	List<AlaCarteItem> getAlaCarteItems() {
		return alaCarteItems;
	}

	void update(String name, BigDecimal price, List<AlaCarteItem> alaCarteItems) {
		setName(name);
		setPrice(price);
		this.alaCarteItems = alaCarteItems;
	}

	@Override
	public String toDisplayString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getId()).append(" // ").append(getName()).append(" // ").append(getPrice()).append(" // ");

		for (int index = 0; index < alaCarteItems.size(); index++) {
			sb.append(alaCarteItems.get(index).getName());

			if (index != (alaCarteItems.size() - 1)) {
				sb.append("--");
			}
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getId()).append(" // ").append(getName()).append(" // ").append(getPrice()).append(" // ");

		for (int index = 0; index < alaCarteItems.size(); index++) {
			sb.append(alaCarteItems.get(index).getId());

			if (index != (alaCarteItems.size() - 1)) {
				sb.append("--");
			}
		}

		return sb.toString();
	}
}
