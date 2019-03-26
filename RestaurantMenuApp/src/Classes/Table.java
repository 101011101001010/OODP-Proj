package Classes;

public class Table {
	public String tableID;
	public int pax;
	public int occupied;
	public int orderID;

	public Table(String tableID)
	{
		this.occupied = 0;
		this.orderID = -1;
		this.pax = 0;
		this.tableID = tableID;
	}

	public String getTableID() {
		return tableID;
	}

	public void setOccupied(int occupied){
		this.occupied = occupied;
	}

	public int isOccupied() {
		return occupied;
	}

	public void setOrderID(int orderID){
		this.orderID = orderID;
	}

	public void setPax(int pax){
		this.pax = pax;
	}
}
