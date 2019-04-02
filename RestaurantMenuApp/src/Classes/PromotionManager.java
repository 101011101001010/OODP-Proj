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

	private static List<PromotionItem> itemList;
	private static int backInput = MenuConstants.OPTIONS_TERMINATE;
	private static int returnInput = (backInput - 1);
	private static AtomicInteger cId;

	static {
		List<String> fileData = FileIOHandler.read(AppConstants.FILE_NAMES[1].toLowerCase());
		String[] lineData;

		if (fileData != null) {
			itemList = new ArrayList<>();
			cId = new AtomicInteger(-1);

			for (String line : fileData) {
				lineData = line.split(AppConstants.FILE_SEPARATOR);

				if (lineData.length == 5) {
					int itemId = Integer.parseInt(lineData[2]);
					String[] subItems = lineData[4].split("/");
					List<String> description = new ArrayList<>();
					Collections.addAll(description, subItems);
					PromotionItem item = new PromotionItem(lineData[0], Float.parseFloat(lineData[1]), itemId, Integer.parseInt(lineData[3]), description);
					itemList.add(item);

					if (itemId > cId.get()) {
						cId.set(itemId);
					}
				}
			}
		}
	}

	public List<PromotionItem> getItemList() {
		return itemList;
	}

	private List<String> getItemNames() {
		List<String> list = new ArrayList<>();

		for (PromotionItem item : itemList) {
			list.add(item.getName());
		}

		return list;
	}

	private List<String> getSortedItems(int sortOption) {
		List<String> list = new ArrayList<>();
		List<PromotionItem> tempList = sortItems(sortOption);

		for (PromotionItem item : tempList) {
			String base = item.getName() + "\nPrice: " + item.getPrice() + "\nItems included:\n";
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(base);
			List<String> subItems = item.getDescription();

			for (String subItem : subItems) {
				stringBuilder.append(subItem);
				stringBuilder.append("\n");
			}

			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			list.add(stringBuilder.toString());
		}

		return list;
	}

	public void addSalesCount(int id, int salesCount) {
		if (id >= 10000) {
			return;
		}

		for (PromotionItem item : itemList) {
			if (item.getItemId() == id) {
				item.addSalesCount(salesCount);
				return;
			}
		}
	}

	public List<PromotionItem> sortItems(int sortOption) {
		List<PromotionItem> tempList = new ArrayList<>(List.copyOf(itemList));

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

	private void view(ScannerHandler sc) {
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

	private void create(ScannerHandler sc) {
		String name = sc.getString("Enter name: ");
		float price = sc.getFloat("Enter price: ");
		int id = cId.incrementAndGet();

		int subItemsCount = sc.getInt("Enter number of sub-itemList: ");
		List<String> subItems = new ArrayList<>();

		for (int i = 0; i < subItemsCount; i++) {
			String subName = sc.getString("Enter name for item #" + (i + 1) + ": ");
			subItems.add(subName);
		}

		PromotionItem item = new PromotionItem(name, price, id, 0, subItems);
		FileIOHandler.write(AppConstants.FILE_NAMES[1].toLowerCase(), item.getWriteData());
		itemList.add(item);
	}

	private void update(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_1[2];
		List<String> list = getItemNames();
		MenuFactory.printMenu(header, list);
		int itemId = MenuFactory.loopChoice(sc, list.size());

		if (itemId == returnInput) {
			return;
		}

		if (itemId != backInput) {
			PromotionItem item = itemList.get(itemId);
			String name = sc.getString("Enter name [leave blank if no change]: ");
			float price = sc.getFloat("Enter price [enter -1 if no change]: ");
			List<String> subItems = item.getDescription();

			for (int i = 0; i < subItems.size(); i++) {
				String subName = sc.getString("Enter name for item #" + (i + 1) + " [leave blank if no change]: ");
				subItems.set(i, subName.isBlank()? subItems.get(i) : subName);
			}

			item.setDescription(subItems);
			item.setName(name.isBlank()? item.getName() : name);
			item.setPrice((price < 0)? item.getPrice() : price);
			FileIOHandler.replace(AppConstants.FILE_NAMES[1].toLowerCase(), itemId, item.getWriteData());
			itemList.set(itemId, item);
		}
	}

	private void remove(ScannerHandler sc) {
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
				itemList.remove(itemId);
			}
		}
	}

	public float getItemPrice(int id) {
		return 0;
	}

	public void start(ScannerHandler sc) {
		for (PromotionItem item : itemList) {
			if (item == null) {
				return;
			}
		}

		int action;

		do {
			String header = MenuConstants.MENU_HEADER_1;
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
