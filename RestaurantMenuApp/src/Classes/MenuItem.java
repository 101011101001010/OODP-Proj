package Classes;

public class MenuItem {
	String name;
	float price;
	String category;
	String description;
	boolean promotion;
	int itemID;
	
	public MenuItem()
	{
		this.name = "";
		this.price = -1;
		this.category = "";
		this.description = "";
		this.promotion = false;
		this.itemID = -1;
	}
	
	public void promotion()
	{
		this.promotion = true;
	}

	public int getItemID() {
		return itemID;
	}
	
	public void print()
	{
		System.out.println("ID :" + this.itemID);
		System.out.println("Name :" + this.name);
		System.out.println("Category :" + this.category);
		System.out.println("Price :" + this.price);
		System.out.println("Description :" + this.description);
		System.out.println("=================================");
	}
}
