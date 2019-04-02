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
		int count = 0;
		for (int i = 0; i < 30; i++) {

			tableList.add(new Table(id));
			map.put(id++, count++);
			if (id == 30)
				id = 40;
			if (id == 50)
				id = 80;
			if (id == 85)
				id = 100;
		}
	}

	public static void setOccupied(String tableID, int orderID) {
		int index = map.get(tableID);
		tableList.get(index).setOrderID(orderID);
		tableList.get(index).setOccupied(1);

	}

	public static void setReserved(String tableID, int pax) {
		int index = map.get(tableID);
		tableList.get(index).setOccupied(-1);
		tableList.get(index).setPax(pax);
	}

	public static void clear(String tableID) {
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

	public int getOrderID(String tableID){
		return tableList.get(map.get(tableID)).getOrderID();
	}

	//
	//
	//

	public void addReservation(Scanner s) {
		String date, name, contact;
		int pax, index;
		System.out.println("Enter reserving date(ddmmyyyy) : ");
		date = s.next();
		System.out.println("Enter reserving time(hh:mm(am/pm))");
		date = date + s.next();

		System.out.println(format(date).toLocalTime());

		do {
			System.out.println("Enter number of people.");
			pax = s.nextInt();
			if (pax > 10) {
				System.out.println("Tables are limited to a maximum number of 10 people, please enter again.");
			}
			if (pax < 1) {
				System.out.println("Unable to book a table with less than a person, please enter again.");
			}
		} while (pax > 10 || pax < 1);

		/*index = checkReservation(date, pax);

		if(index != -1){
			System.out.println("Enter Your Name.");
			name = s.next();
			System.out.println("Enter Your Contact.");
			contact = s.next();
			tableList.get(index).getReservationList().add(new Table.Reservations(contact, name, date, pax));
		}*/
	}

	public int checkReservation(String datetime, int pax) {
		int index;
		int noOfTables = 0;
		DateTimeFormatter session = DateTimeFormatter.ofPattern("a");

		for (Table t : tableList) {
			if (pax <= 2 && (t.getTableID() / 10 == 2)) {
				for (Table.Reservations r : t.getReservationList()) {
					if ((format(datetime).toLocalDate().compareTo(format(r.getDate()).toLocalDate()) != 0)
							&& (format(datetime).format(session) == format(r.getDate()).format(session))) {
						noOfTables = noOfTables+1;
						if (noOfTables>=10)
							return -1;
					}
				}
			}
			if ((pax == 3 || pax == 4) && (t.getTableID() / 10 == 4)) {
				for (Table.Reservations r : t.getReservationList()) {
					if ((format(datetime).toLocalDate().compareTo(format(r.getDate()).toLocalDate()) == 0)
							&& (format(datetime).format(session) == format(r.getDate()).format(session))) {
						noOfTables = noOfTables+1;
						if (noOfTables>=10)
							return -1;
					}
				}
			}
			if (pax>=5 && pax<=8 && (t.getTableID() / 10 == 8)) {
				for (Table.Reservations r : t.getReservationList()) {
					if ((format(datetime).toLocalDate().compareTo(format(r.getDate()).toLocalDate()) == 0)
							&& (format(datetime).format(session) == format(r.getDate()).format(session))) {
						noOfTables = noOfTables+1;
						if (noOfTables>=10)
							return -1;
					}
				}
			}
			if (pax>=9 && (t.getTableID() / 10 == 10)) {
				for (Table.Reservations r : t.getReservationList()) {
					if ((format(datetime).toLocalDate().compareTo(format(r.getDate()).toLocalDate()) == 0)
							&& (format(datetime).format(session) == format(r.getDate()).format(session))) {
						noOfTables = noOfTables+1;
						if (noOfTables>=10)
							return -1;
					}
				}
			}
		}


		//index = map.get(tableID);
		return 0;


	}
	public LocalDateTime format (String datetime) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyhh:mma");
		return LocalDateTime.parse(datetime, formatter);
	}
}


