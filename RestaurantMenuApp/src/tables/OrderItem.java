package tables;

import menu.MenuItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

class OrderItem {
	private int count;
	private MenuItem item;
	private BigDecimal price;
	private BigDecimal pricePer;

	OrderItem(MenuItem item, int count)
	{
		this.item = item;
		this.count = count;
		this.pricePer = item.getPrice();
		this.price = pricePer.multiply(new BigDecimal(count)).setScale(2, RoundingMode.FLOOR);
	}

	MenuItem getItem() {
		return item;
	}

	void updateCount(int count) {
		this.count += count;
		this.price = pricePer.multiply(new BigDecimal(this.count)).setScale(2, RoundingMode.FLOOR);
	}

	int getCount() {
		return count;
	}

	BigDecimal getPrice() {
		return price;
	}
}
