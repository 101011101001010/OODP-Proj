package Classes;

public class Table {
	String tableID;
	int pax;
	boolean occupied;
	int billID;
	public Table(int pax,String tableID)
	{
		this.occupied = false;
		this.billID = -1;
		this.pax = pax;
		this.tableID = tableID;
	}
}
