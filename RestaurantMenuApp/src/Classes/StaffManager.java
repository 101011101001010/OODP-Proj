package Classes;

import constants.AppConstants;
import constants.MenuConstants;
import tools.FileIOHandler;
import tools.MenuFactory;
import tools.ScannerHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StaffManager {
	private static List<Staff> staffList;
	private static AtomicInteger cId;
	private static final String fileName = AppConstants.FILE_NAMES[4];
	private static int backInput = MenuConstants.OPTIONS_TERMINATE;
	private static int returnInput = (backInput - 1);

	static {
		List<String> fileData = FileIOHandler.read(fileName);
		String[] lineData;
		int staffId;

		if (fileData != null) {
			staffList = new ArrayList<>();
			cId = new AtomicInteger(-1);

			for (String line : fileData) {
				lineData = line.split(",");
				staffId = Integer.parseInt(lineData[0]);
				Staff staffMember = new Staff(staffId, lineData[1], lineData[2], lineData[3]);
				staffList.add(staffMember);

				if (staffId > cId.get()) {
					cId.set(staffId);
				}
			}
		}
	}

	public static List<Staff> getStaffList() {
		return staffList;
	}

	private static List<String> getStaffNames() {
		List<String> list = new ArrayList<>();

		for (Staff staff : staffList) {
			list.add(staff.getName());
		}

		return list;
	}

	private static List<String> getStaffInfo() {
		List<String> list = new ArrayList<>();

		for (Staff staff : staffList) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(staff.getName());
			stringBuilder.append("\n");
			stringBuilder.append("Title: ");
			stringBuilder.append(staff.getTitle());
			stringBuilder.append("\n");
			stringBuilder.append("Gender:  ");
			stringBuilder.append(staff.getGender());
			stringBuilder.append("\n");
			stringBuilder.append("ID: ");
			stringBuilder.append(staff.getId());
			list.add(stringBuilder.toString());
		}

		return list;
	}

	private static void view(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_5[0];
		List<String> list = getStaffInfo();
		MenuFactory.printMenuX(header, list);
		sc.getString("Enter anything to continue...");
	}

	private static void create(ScannerHandler sc) {
		String name = sc.getString("Enter name: ");
		String title = sc.getString("Enter title: ");
		String gender;

		do {
			gender = sc.getString("Enter gender [M / F]: ");

			if (!gender.equalsIgnoreCase("M") && !gender.equalsIgnoreCase("F")) {
				System.out.println(MenuConstants.MSG_INVALID_CHOICE);
			}
		} while (!gender.equalsIgnoreCase("M") && !gender.equalsIgnoreCase("F"));

		int id = cId.incrementAndGet();
		Staff staff = new Staff(id, name, title, gender.toUpperCase());
		FileIOHandler.write(fileName, staff.getWriteData());
		staffList.add(staff);
	}

	private static void update(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_5[2];
		List<String> list = getStaffNames();
		MenuFactory.printMenu(header, list);
		int staffIndex = MenuFactory.loopChoice(sc, list.size());

		if (staffIndex == returnInput) {
			return;
		}

		if (staffIndex != backInput) {
			Staff staff = staffList.get(staffIndex);
			String name = sc.getString("Enter name [leave blank if no change]: ");
			String title = sc.getString("Enter title [leave blank if no change]: ");
			String gender;

			do {
				gender = sc.getString("Enter gender [M / F] [leave blank if no change]: ");

				if (gender.isBlank()) {
					break;
				}

				if (!gender.equalsIgnoreCase("M") && !gender.equalsIgnoreCase("F")) {
					System.out.println(MenuConstants.MSG_INVALID_CHOICE);
				}
			} while (!gender.equalsIgnoreCase("M") && !gender.equalsIgnoreCase("F"));

			staff.setName(name.isBlank()? staff.getName() : name);
			staff.setTitle(title.isBlank()? staff.getTitle() : title);
			staff.setGender((gender.isBlank())? staff.getGender() : gender.toUpperCase());
			FileIOHandler.replace(fileName, staffIndex, staff.getWriteData());
			staffList.set(staffIndex, staff);
		}
	}

	private static void remove(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_5[3];
		List<String> list = getStaffNames();
		MenuFactory.printMenu(header, list);
		int staffIndex = MenuFactory.loopChoice(sc, list.size());

		if (staffIndex == returnInput) {
			return;
		}

		if (staffIndex != backInput) {
			String choice = sc.getString("Confirm remove? [y = remove]: ");

			if (choice.equalsIgnoreCase("y")) {
				FileIOHandler.remove(fileName, staffIndex);
				staffList.remove(staffIndex);
			}
		}
	}

	public static void start(ScannerHandler sc) {
		for (Staff staff : staffList) {
			if (staff == null) {
				return;
			}
		}

		int action;

		do {
			String header = MenuConstants.MENU_HEADER_5;
			String[] actions = MenuConstants.MENU_ACTIONS;
			MenuFactory.printMenu(header, actions);
			action = MenuFactory.loopChoice(sc, actions.length);

			if (action == returnInput) {
				return;
			}

			if (action != backInput) {
				try {
					Method method = StaffManager.class.getDeclaredMethod(actions[action].toLowerCase(), ScannerHandler.class);
					method.invoke(null, sc);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					//App.printError(e, AppConstants.MENU_ID + 1);
				}
			}
		} while (action != backInput);
	}

}
