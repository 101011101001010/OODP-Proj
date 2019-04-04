package Classes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;


public class Table {
	private int tableID;
	private int pax;
	private int occupied;
	private int orderID;
	private ArrayList<Reservations> reservationList = new ArrayList<>();

	static class Reservations implements Comparable<Table.Reservations>{
		private int contact;
		private String name;
		private LocalDateTime date;
		private int pax;
		private String session;

		public Reservations (int contact, String name, LocalDateTime date, int pax, String session){
			this.contact = contact;
			this.name = name;
			this.date = date;
			this.pax = pax;
			this.session = session;
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
		public String getSession() {
			return session;
		}

		public String toStringTwo(){
			DateTimeFormatter tf = DateTimeFormatter.ofPattern("hh:mma");
			return "Reservation date :" + date.toLocalDate() + " " + date.toLocalTime().format(tf) +
					", Name :" + getName() + ", Contact :" + getContact() + ", Pax :" + getPax();
		}
		public int compareTo(Reservations anotherReservation){
			return getDate().compareTo(anotherReservation.getDate());
		}
	}
	public Table(int tableID)
	{
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
		boolean check = false;
		for (Reservations r : reservationList){
			if(r.getContact() == contact){
				System.out.println("Reservation Found At Table " + tableID);
				System.out.println(r.toStringTwo());
				check = true;
			}
		}
		return check;
	}
	public void checkNoShow(){
		if (reservationList.size()!=0) {
			for (int i = 0; i<reservationList.size(); i++) {
				if (reservationList.get(i).getDate().plusSeconds(30).isBefore(LocalDateTime.now())){
					System.out.println(reservationList.get(i).toStringTwo());
					getReservationList().remove(i);
				}
			}
		}
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


	public void isReserved(int contact,String name, LocalDateTime date, int pax){
		String session = "03:00pm";
		DateTimeFormatter tf = DateTimeFormatter.ofPattern("hh:mma");
		if (date.toLocalTime().isBefore(LocalTime.parse(session, tf)))
			session = "am";
		else
			session = "pm";

		reservationList.add(new Reservations(contact, name, date, pax, session));
	}

}