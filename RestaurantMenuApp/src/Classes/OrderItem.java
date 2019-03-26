package Classes;

public class OrderItem{
	int count;
	int itemID;
	float price;
	boolean promotion = false;
	public OrderItem(int count,int itemID)
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
			PromotionManager.addCount(itemID,count);
		else
			MenuManager.addCount(itemID,count);
	}
	public String print()
	{
		if(promotion)
		{
			for(int i =0; i<PromotionManager.promotionList.size();i++)
			{
				if(PromotionManager.promotionList.get(i).itemID == itemID)
				{
					return PromotionManager.promotionList.get(i).name + " " + this.price;
				}
			}
		}
		else
		{
			for(int i =0; i<MenuManager.menuList.size();i++)
			{
				if(MenuManager.menuList.get(i).itemID == itemID)
				{
					return MenuManager.menuList.get(i).name+ " " + this.price;
				}
			}
		}
		return null;
	}
}
