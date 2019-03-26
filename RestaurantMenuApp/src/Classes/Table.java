package Classes;

public class Table {
	String tableID;
	int pax;
	boolean occupied;
	int orderID;
	public Table(int pax,String tableID)
	{
		this.occupied = false;
		this.orderID = -1;
		this.pax = pax;
		this.tableID = tableID;
	}
}
