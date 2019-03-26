package Classes;

import constants.AppConstants;

import java.util.List;

public class PromotionItem {
	private List<String> description;
	private int itemId;
	private float price;
	private String name;
	private int salesCount;

	public PromotionItem(String name, float price, int itemId, int salesCount, List<String> description) {
		this.name = name;
		this.price = price;
		this.description = description;
		this.itemId = itemId;
		this.salesCount = salesCount;
	}

	public int getItemId() {
		return itemId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getPrice() {
		return price;
	}

	public void setDescription(List<String> description) {
		this.description = description;
	}

	public List<String> getDescription() {
		return description;
	}

	public void addSalesCount(int salesCount) {
		this.salesCount += salesCount;
	}

	public int getSalesCount() {
		return salesCount;
	}

	public String getWriteData() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(name);
		stringBuilder.append(AppConstants.FILE_SEPARATOR);
		stringBuilder.append(price);
		stringBuilder.append(AppConstants.FILE_SEPARATOR);
		stringBuilder.append(itemId);
		stringBuilder.append(AppConstants.FILE_SEPARATOR);
		stringBuilder.append(salesCount);
		stringBuilder.append(AppConstants.FILE_SEPARATOR);

		for (int i = 0; i < description.size(); i++) {
			stringBuilder.append(description.get(i));

			if (i != (description.size() - 1)) {
				stringBuilder.append("/");
			}
		}

		return stringBuilder.toString();
	}
}
