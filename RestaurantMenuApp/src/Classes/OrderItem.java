package Classes;

public class OrderItem{
	int count;
	int itemID;
	float price;
	public OrderItem(int count,int itemID)
	{
		this.itemID = itemID;
		this.count = count;
		this.price = MenuManager.getItemPrice(this.itemID) * count;
	}
}
