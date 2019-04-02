package Classes;

public class OrderItem{
	int count;
	int itemID;
	float price;
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
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public boolean isPromotion() {
		return promotion;
	}
	public void setPromotion(boolean promotion) {
		this.promotion = promotion;
	}
	boolean promotion = false;
	public OrderItem(int itemID,int count,PromotionManager pm,MenuManager mm)
	{
		this.itemID = itemID;
		this.count = count;
		if(itemID >= 10000)
		{
			this.price = pm.getItemPrice(this.itemID) * count;
			promotion = true;
		}
		else
			this.price = mm.getItemPrice(this.itemID) * count;
		
	}
	public void addCount(PromotionManager pm, MenuManager mm)
	{
		if(promotion)
			pm.addSalesCount(itemID,count);
		else
			mm.addSalesCount(itemID,count);
	}
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
}
