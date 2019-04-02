package Classes;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TableManager {

	public static ArrayList<Table> tableList = new ArrayList<>();
	private static Map<Integer, Integer> map;

	public TableManager() {
		map = new HashMap<>();

		int id = 20;

		for (int i = 0; i < 30; i++) {

			tableList.add(new Table(id));
			map.put(id++, i);
			if (id == 30)
				id = 40;
			if (id == 50)
				id = 80;
			if (id == 85)
				id = 100;
		}
	}

	public static void setOccupied(int tableID, int orderID) {
		int index = map.get(tableID);
		tableList.get(index).setOrderID(orderID);
		tableList.get(index).setOccupied(1);

	}

	public static void setReserved(int tableID, int pax) {
		int index = map.get(tableID);
		tableList.get(index).setOccupied(-1);
		tableList.get(index).setPax(pax);
	}

	public static void clear(int tableID) {
		int index = map.get(tableID);
		tableList.get(index).setOccupied(0);
		tableList.get(index).setOrderID(-1);
	}

	public void choices(Scanner s) {
		System.out.println("1. Show Table");
		System.out.println("1. Set Table");
		System.out.println("1. Set reservation");
		System.out.println("");

	}

	public void checkVacancy() {
		for (Table t : tableList) {
			System.out.println(t.toString());
		}
	}

	public int getOrderID(int tableID){
		return tableList.get(map.get(tableID)).getOrderID();
	}
	public boolean checkOccupied(int tableID){
		if (tableList.get(map.get(tableID)).isOccupied()!=0)
			return true;
		else
			return false;

	}

	//
	//
	//

	public void addReservation(Scanner s) {
		String date, name, contact;
		int pax = 0, index=-1;
		boolean done = false;
		String test1 = "0204201909:30pm";
		String test2 = "0204201910:30pm";
		String test3 = "0204201909:45pm";
		String test4 = "0204201909:15pm";
		String test5 = "0204201907:30pm";
		tableList.get(25).isReserved("12345", "A", test1, 10);
		tableList.get(26).isReserved("67890", "B", test2, 10);
		tableList.get(27).isReserved("54321", "C", test3, 10);
		tableList.get(28).isReserved("09876", "D", test4, 10);
		tableList.get(29).isReserved("24680", "E", test5, 10);
		showReservation();

		System.out.println("Enter reserving date(ddmmyyyy) : ");
		date = s.next();
		System.out.println("Enter reserving time(hh:mm(am/pm))");
		date = date + s.next();
		while(!done) {
			try {
				do {
					System.out.println("Enter number of people.");

					pax = s.nextInt();

					if (pax > 10)
						System.out.println("Tables are limited to a maximum number of 10 people, please enter again.");
					else if (pax < 1)
						System.out.println("Unable to book a table with less than a person, please enter again.");
					else
						done = true;
				} while (pax > 10 || pax < 1);

			} catch (Exception e) {
				s.next();
				System.out.println("Please enter a number");
			}
		}
		index = checkReservation(date, pax);

		if(index != -1){
			System.out.println(index);
			System.out.println("Enter Your Name.");
			name = s.next();
			System.out.println("Enter Your Contact.");
			contact = s.next();
			tableList.get(index).getReservationList().add(new Table.Reservations(contact, name, date, pax));
		}
		else
			System.out.println("Sorry, Booking Full.");
	}

	public int checkReservation(String datetime, int pax) {
		DateTimeFormatter session = DateTimeFormatter.ofPattern("a");
		for (Table t : tableList) {
			if (pax <= 2 && (t.getTableID() / 10 == 2)) {
				for (Table.Reservations r : t.getReservationList()) {
					if (compareDate(datetime, r.getDate())!=0)
						return map.get(t.getTableID());
					else if (!formatting(datetime).format(session).equals(formatting(r.getDate()).format(session))) {
						return map.get(t.getTableID());
					}
				}
			}
			if ((pax == 3 || pax == 4) && (t.getTableID() / 10 == 4)) {
				for (Table.Reservations r : t.getReservationList()) {
					if (compareDate(datetime, r.getDate())!=0)
						return map.get(t.getTableID());
					else if (!formatting(datetime).format(session).equals(formatting(r.getDate()).format(session))) {
						return map.get(t.getTableID());
					}
				}
			}
			if (pax>=5 && pax<=8 && (t.getTableID() / 10 == 8)) {
				for (Table.Reservations r : t.getReservationList()) {
					if (compareDate(datetime, r.getDate())!=0)
						return map.get(t.getTableID());
					else if (!formatting(datetime).format(session).equals(formatting(r.getDate()).format(session))) {
						return map.get(t.getTableID());
					}
				}
			}
			if (pax>=9 && (t.getTableID() / 10 == 10)) {
				for (Table.Reservations r : t.getReservationList()) {
					if (compareDate(datetime, r.getDate())!=0)
						return map.get(t.getTableID());
					else if (!formatting(datetime).format(session).equals(formatting(r.getDate()).format(session))) {
						return map.get(t.getTableID());
					}
				}
			}
		}
		return -1;


	}
	public void showReservation (){
		for (int i =0; i <tableList.size();i++){
			System.out.println("For Table " + tableList.get(i).getTableID());
			for (int j =0; j <tableList.get(i).getReservationList().size(); j++){
				System.out.println(tableList.get(i).getReservationList().get(j).toStringTwo());
			}
		}
	}
	public LocalDateTime formatting (String datetime) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
		return LocalDateTime.parse(datetime, formatter);
	}
	public int compareDate (String compare1, String compare2){
		return formatting(compare1).toLocalDate().compareTo(formatting(compare2).toLocalDate());
	}
}


