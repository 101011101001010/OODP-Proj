package Classes;

import constants.AppConstants;

public class PromotionItem {
	String description;
	int itemId;
	float price;
	String name;
	int salesCount;

	public PromotionItem(String name, float price, String description, int itemId, int salesCount) {
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

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
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
		stringBuilder.append(description);
		stringBuilder.append(AppConstants.FILE_SEPARATOR);
		stringBuilder.append(itemId);
		stringBuilder.append(AppConstants.FILE_SEPARATOR);
		stringBuilder.append(salesCount);
		return stringBuilder.toString();
	}
}
