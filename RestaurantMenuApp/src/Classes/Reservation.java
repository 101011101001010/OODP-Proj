package Classes;

import java.sql.Date;

public class Reservation {
	Date date;
	int pax;
	String name;
	String contact;
	String period;
	int tableid;
	
	public Reservation()
	{
		this.date = new Date(00000000);
		pax = 0;
		name = "";
		contact = "";
		period = "";
		tableid = -1;
	}
}
