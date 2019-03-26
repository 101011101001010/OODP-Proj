package Classes;

public class OrderItem{
	int count;
	int itemID;
	float price;
	boolean promotion = false;
	public OrderItem(int itemID,int count)
	{
		this.itemID = itemID;
		this.count = count;
		this.price = MenuManager.getItemPrice(this.itemID) * count;
		if(itemID >= 10000)
			promotion = true;
		
	}
	public void addCount()
	{
		if(promotion)
			PromotionManager.addSalesCount(itemID,count);
		else
			MenuManager.addSalesCount(itemID,count);
	}
	public String print()
	{
		if(promotion)
		{
			for(int i =0; i<PromotionManager.getItemList().size();i++)
			{
				PromotionItem item = PromotionManager.getItemList().get(i);
				if(item.getItemId() == itemID)
				{
					return item.getName() + " " + this.price;
				}
			}
		}
		else
		{
			for(int i =0; i<MenuManager.getItems().size();i++)
			{
				MenuItem item = MenuManager.getItems().get(i);
				System.out.println("*" + itemID);
				if(item.getItemId() == itemID)
				{
					return item.getName()+ " " + this.price;
				}
			}
		}
		return null;
	}
}
