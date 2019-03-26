package Classes;

import constants.AppConstants;
import constants.MenuConstants;
import tools.FileIOHandler;
import tools.MenuFactory;
import tools.ScannerHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PromotionManager {
	static ArrayList<PromotionItem> promotionList = new ArrayList<PromotionItem>();

	private static List<PromotionItem> items;
	private static int backInput = MenuConstants.OPTIONS_TERMINATE;
	private static int returnInput = (backInput - 1);
	private static AtomicInteger cId;

	static {
		List<String> fileData = FileIOHandler.read(AppConstants.FILE_NAMES[0].toLowerCase());
		String[] lineData;

		if (fileData != null) {
			items = new ArrayList<>();
			cId = new AtomicInteger(-1);

			for (String line : fileData) {
				lineData = line.split(AppConstants.FILE_SEPARATOR);

				if (lineData.length == 5) {
					int itemId = Integer.parseInt(lineData[3]);
					PromotionItem item = new PromotionItem(lineData[0], Float.parseFloat(lineData[1]), lineData[2], itemId, Integer.parseInt(lineData[4]));
					items.add(item);

					if (itemId > cId.get()) {
						cId.set(itemId);
					}
				}
			}
		}
	}

	public static List<PromotionItem> getItems() {
		return items;
	}

	private static List<String> getItemNames() {
		List<String> list = new ArrayList<>();

		for (PromotionItem item : items) {
			list.add(item.getName());
		}

		return list;
	}

	private static List<String> getSortedItems(int sortOption) {
		List<String> list = new ArrayList<>();
		List<PromotionItem> tempList = sortItems(sortOption);

		for (PromotionItem item : tempList) {
			String base = item.getName() + "\n" + item.getDescription() + "\nPrice: " + item.getPrice() + "\nID: " + item.getItemId();
			list.add(base);
		}

		return list;
	}

	public static void addSalesCount(int id, int salesCount) {
		if (id >= 10000) {
			return;
		}

		for (PromotionItem item : items) {
			if (item.getItemId() == id) {
				item.addSalesCount(salesCount);
				return;
			}
		}
	}

	public static List<PromotionItem> sortItems(int sortOption) {
		List<PromotionItem> tempList = new ArrayList<>(List.copyOf(items));

		System.out.println("HI");
		switch(sortOption) {
			//Name
			case 0:
				tempList.sort(Comparator.comparing(PromotionItem::getName));
				break;
			//Price

			case 1:
				tempList.sort((s1, s2) -> Float.compare(s1.getPrice(), s2.getPrice()));
				break;
		}

		return tempList;
	}

	private static void view(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_1[0];
		String[] options = MenuConstants.MENU_SORT_ACTIONS_1;
		MenuFactory.printMenu(header, options);
		int sortOption = MenuFactory.loopChoice(sc, options.length);

		if (sortOption == returnInput) {
			return;
		}

		if (sortOption != backInput) {
			List<String> list = getSortedItems(sortOption);
			MenuFactory.printMenuX(header, list.toArray(new String[0]));
			sc.getString("Enter anything to continue...");
		}
	}

	private static void create(ScannerHandler sc) {
		String name = sc.getString("Enter name: ");
		float price = sc.getFloat("Enter price: ");
		String description = sc.getString("Enter description: ");
		int id = cId.incrementAndGet();

		PromotionItem item = new PromotionItem(name, price, description, id, 0);
		FileIOHandler.write(AppConstants.FILE_NAMES[1].toLowerCase(), item.getWriteData());
		items.add(item);
	}

	private static void update(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_1[2];
		List<String> list = getItemNames();
		MenuFactory.printMenu(header, list);
		int itemId = MenuFactory.loopChoice(sc, list.size());

		if (itemId == returnInput) {
			return;
		}

		if (itemId != backInput) {
			PromotionItem item = items.get(itemId);
			String name = sc.getString("Enter name [leave blank if no change]: ");
			float price = sc.getFloat("Enter price [enter -1 if no change]: ");
			String desc = sc.getString("Enter description [leave blank if no change]: ");

			item.setName(name.isBlank()? item.getName() : name);
			item.setPrice((price < 0)? item.getPrice() : price);
			item.setDescription(desc.isBlank()? item.getDescription() : desc);
			FileIOHandler.replace(AppConstants.FILE_NAMES[1].toLowerCase(), itemId, item.getWriteData());
			items.set(itemId, item);
		}
	}

	private static void remove(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_1[3];
		List<String> list = getItemNames();
		MenuFactory.printMenu(header, list);
		int itemId = MenuFactory.loopChoice(sc, list.size());

		if (itemId == returnInput) {
			return;
		}

		if (itemId != backInput) {
			String choice = sc.getString("Confirm remove? [y = remove]: ");

			if (choice.equalsIgnoreCase("y")) {
				FileIOHandler.remove(AppConstants.FILE_NAMES[1].toLowerCase(), itemId);
				items.remove(itemId);
			}
		}
	}

	public static float getItemPrice(int id) {
		return 0;
	}
	public static void start(ScannerHandler sc) {
		for (PromotionItem item : items) {
			if (item == null) {
				return;
			}
		}

		int action;

		do {
			String header = MenuConstants.MENU_HEADER_0;
			String[] actions = MenuConstants.MENU_ACTIONS;
			MenuFactory.printMenu(header, actions);
			action = MenuFactory.loopChoice(sc, actions.length);

			if (action == returnInput) {
				return;
			}

			if (action != backInput) {
				try {
					Method method = PromotionManager.class.getDeclaredMethod(actions[action].toLowerCase(), ScannerHandler.class);
					method.invoke(null, sc);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					//App.printError(e, AppConstants.MENU_ID + 1);
				}
			}
		} while (action != backInput);
	}
}
