package order;

import menu.MenuItem;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderItem {
	int count;
	int itemID;
	private MenuItem item;
	BigDecimal price;
	//boolean promotion = false;
	BigDecimal pricePer;

	public OrderItem(MenuItem item, int count)
	{
		this.item = item;
		this.count = count;
		this.pricePer = item.getPrice();
		this.price = pricePer.multiply(new BigDecimal(count)).setScale(2, RoundingMode.FLOOR);
	}

	public MenuItem getItem() {
		return item;
	}

	/*
	public String print(PromotionManager pm, MenuManager mm)
	{
		if(promotion)
		{
			for(int i =0; i<pm.getItemList().size();i++)
			{
				PromotionItem item = pm.getItemList().get(i);
				if(item.getItemId() == itemID)
				{
					return item.getName() + " " + this.price;
				}
			}
		}
		else
		{
			for(int i =0; i<mm.getItems().size();i++)
			{
				MenuItem item = mm.getItems().get(i);
				if(item.getItemId() == itemID)
				{
					return item.getName()+ " " + this.price;
				}
			}
		}
		return null;
	}
	*/

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getItemID() {
		return itemID;
	}
	public void setItemID(int itemID) {
		this.itemID = itemID;
	}
	public BigDecimal getPrice() {
		return price;
	}
}
