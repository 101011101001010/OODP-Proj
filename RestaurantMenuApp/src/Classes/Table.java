package Classes;

import client.Restaurant;
import client.RestaurantAsset;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;



public class Table extends RestaurantAsset {
	private int tableID;
	private int pax;
	private int occupied;
	private int orderID;
	private ArrayList<Reservations> reservationList = new ArrayList<>();

	static class Reservations{
		private int contact;
		private String name;
		private LocalDateTime date;
		private int pax;


		public Reservations (int contact,String name, LocalDateTime date, int pax){
			this.contact = contact;
			this.name = name;
			this.date = date;
			this.pax = pax;
		}
		public LocalDateTime getDate(){
			return date;
		}
		public String getName(){
			return name;
		}
		public int getContact(){
			return contact;
		}
		public int getPax(){
			return pax;
		}
		public String toStringTwo(){
			//DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
			//LocalDateTime date = LocalDateTime.parse(getDate(),format);
			return "Reservation date :" + date.toLocalDate() + " " + date.toLocalTime() +
					", Name :" + getName() + ", Contact :" + getContact() + ", Pax :" + getPax();
		}
	}

	public Table(int tableID)
	{
		super(tableID);
		this.occupied = 0;
		this.orderID = -1;
		this.pax = 0;
		this.tableID = tableID;

	}

	public int getTableID() {
		return tableID;
	}
	public int getOrderID() {
		return orderID;
	}

	public  void setOccupied(int occupied){
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

	public ArrayList<Reservations> getReservationList(){
		return reservationList;
	}
	public void showReservationList (){
		int index=1;
		System.out.println("For Table " + getTableID());
		for (Reservations r : reservationList){
				System.out.println(index++ + ". " + r.toStringTwo());
		}

	}
	public boolean findReservation(int contact){

		for (Reservations r : reservationList){
			if(r.getContact() == contact){
				System.out.println("Reservation Found At Table " + tableID);
				System.out.println(r.toStringTwo());
				return true;
			}
		}
		return false;
	}

	public String toString(){
		String check=null;
		if (isOccupied()==-1)
			check = "Reserved.";
		if (isOccupied()==0)
			check = "not occupied.";
		if (isOccupied()==1)
			check = "occupied.";
		return "Table " + getTableID() + " is " + check;
	}

	@Override
	public String toDisplayString() {
		return null;
	}


	public void isReserved(int contact,String name, LocalDateTime date, int pax){
		reservationList.add(new Reservations(contact, name, date, pax));
	}

}

