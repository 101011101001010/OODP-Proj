package Classes;

public class MenuItem {
	String name;
	float price;
	String category;
	String desc;
	boolean promotion;
	int itemID;
	
	public MenuItem()
	{
		this.name = "";
		this.price = -1;
		this.category = "";
		this.desc = "";
		this.promotion = false;
		this.itemID = -1;
	}
	
	public void promotion()
	{
		this.promotion = true;
	}
}
