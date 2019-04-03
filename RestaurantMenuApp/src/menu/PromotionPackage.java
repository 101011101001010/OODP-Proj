package menu;

import java.math.BigDecimal;
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

	@Override
	public String toTableString() {
		String head = super.toTableString() + " // ";
		StringBuilder sb = new StringBuilder(head);

		for (int index = 0; index < alaCarteItems.size(); index++) {
			sb.append(alaCarteItems.get(index).getName());

			if (index != (alaCarteItems.size() - 1)) {
				sb.append("--");
			}
		}

		return sb.toString();
	}

	@Override
	public String toPrintString() {
		String head = super.toPrintString() + " // ";
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
