package Classes;

public class PromotionItem {
	String description;
	int itemID;
	float price;
	String name;
	int saleCount;
	
	public PromotionItem()
	{
		name = "";
		description = "";
		itemID = -1;
		price = -1;
	}
	public void print()
	{
		System.out.println("ID :" + this.itemID);
		System.out.println("Name :" + this.name);
		System.out.println("Price :" + this.price);
		System.out.println("Description :" + this.description);
		System.out.println("=================================");
	}
}
