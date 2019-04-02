package Classes;

import client.BaseManager;
import client.Restaurant;
import client.RestaurantAsset;
import client.enums.AssetType;
import client.enums.Op;
import tools.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MenuManager extends BaseManager {
	/*
	private static List<AlaCarteItem> items;
	private static Set<String> itemTypes;
	private static int backInput = MenuConstants.OPTIONS_TERMINATE;
	private static int returnInput = (backInput - 1);
	private static AtomicInteger cId;

	static {
		List<String> fileData = FileIOHandler.read(AppConstants.FILE_NAMES[0].toLowerCase());
		String[] lineData;

		if (fileData != null) {
			items = new ArrayList<>();
			itemTypes = new HashSet<>();
			cId = new AtomicInteger(-1);

			for (String line : fileData) {
				lineData = line.split(AppConstants.FILE_SEPARATOR);

				if (lineData.length == 6) {
					int itemId = Integer.parseInt(lineData[4]);
					String category = lineData[2];
					AlaCarteItem item = new AlaCarteItem(lineData[0], Float.parseFloat(lineData[1]), category, lineData[3], itemId, Integer.parseInt(lineData[5]));
					items.add(item);
					itemTypes.add(category);

					if (itemId > cId.get()) {
						cId.set(itemId);
					}
				}
			}
		}
	}
	*/

	public MenuManager(Restaurant restaurant) {
		super(restaurant);
		/*
		Pair<Op, String> response = init();
		if (response.getLeft().equals(Op.FAILED)) {
			throw new Exception(response.getRight());
		}

		System.out.println(response.getRight());
		*/
	}

	@Override
	public String[] getMainCLIOptions() {
		return new String[] {
				"View menu",
				"Add new ala-carte item",
				"Add new promotional package",
				"Manage ala-carte items",
				"Manage promotional packages",
		};
	}

	@Override
	public Runnable[] getOptionRunnables() {
		return new Runnable[] {
				this::viewMenu,
				() -> addMenuItem(AssetType.FOOD),
				() -> addMenuItem(AssetType.PROMO),
				() -> manageMenuItems(AssetType.FOOD),
				() -> manageMenuItems(AssetType.PROMO)
		};
	}

	private void viewMenu() {
		int choice = 4;
		List<String> displayList;

		do {
			displayList = getDisplay(AssetType.FOOD, choice);
			if (displayList == null) {
				System.out.println("Failed to get list.");
				return;
			}
			getCs().printDisplayTable("Menu: Ala-Carte Items", displayList);

			displayList = getDisplay(AssetType.PROMO, choice);
			if (displayList == null) {
				System.out.println("Failed to get list.");
				return;
			}
			getCs().printDisplayTable("Menu: Promotional Packages", displayList);
		} while ((choice = getCs().printChoices("Command // Corresponding Function", Arrays.asList("Sort by ID", "Sort by name", "Sort by price", "Sort by category"), new String[] {"Go back"})) != -1);
	}

	private void addMenuItem(AssetType assetType) {
		if (!assetType.equals(AssetType.FOOD) && !assetType.equals(AssetType.PROMO)) {
			return;
		}

		getCs().printInstructions(new String[] {"Note:", "Enter '/quit' in name to return to main menu."});
		String name = getCs().getString("Enter item name");
		if (name.equalsIgnoreCase("/quit")) {
			return;
		}

		if (assetType.equals(AssetType.FOOD)) {
			BigDecimal price = new BigDecimal(getCs().getDouble("Enter item's price"));
			String description = getCs().getString("Enter item description");
			String category = getCs().getString("Enter item category");
			System.out.println(addItem(name, price, description, category));
			return;
		}

		List<String> alaCarteItemList = getItemNames(AssetType.FOOD);
		if (alaCarteItemList == null) {
			System.out.println("Failed to get list.");
			return;
		}

		List<Integer> alaCarteItemIndices = new ArrayList<>();
		String[] footerOptions = new String[] {"Go back"};
		getCs().printChoicesSimple("Index // Ala-Carte Items", alaCarteItemList, footerOptions);
		getCs().printInstructions(new String[] {"Select an ala-carte item to add to the package."});
		int itemIndex = getCs().getInt("Enter choice", alaCarteItemList.size(), 0 - footerOptions.length);

		if (itemIndex == -1) {
			return;
		}

		alaCarteItemIndices.add(itemIndex - 1);
		String cont;
		while (!(cont = getCs().getString("Add another item to package? [Y = YES | N = NO]")).equalsIgnoreCase("N")) {
			if (cont.equalsIgnoreCase("Y")) {
				itemIndex = getCs().getInt("Enter choice", alaCarteItemList.size(), 0 - footerOptions.length);
				if (itemIndex == -1) {
					return;
				}
				alaCarteItemIndices.add(itemIndex - 1);
			}
		}

		System.out.println(addItem(name, alaCarteItemIndices));
	}

	private void manageMenuItems(AssetType assetType) {
		List<String> nameList = getItemNames(assetType);
		if (nameList == null) {
			System.out.println("Failed to get list.");
			return;
		}

		int itemIndex = getCs().printChoices("Index // " + (assetType.equals(AssetType.FOOD)? "Ala-Carte Items" : "Promotional Packages"), nameList, new String[]{"Go back"});
		if (itemIndex == -1) {
			return;
		}

		itemIndex -= 1;
		String[] actions;

		if (assetType.equals(AssetType.FOOD)) {
			actions = new String[] {"Change name.", "Change price.", "Change description.", "Change category", "Remove item from menu."};
		} else {
			actions = new String[] {"Change name.", "Remove package from menu."};
		}

		int action = getCs().printChoices("Index // Action", actions, new String[]{"Go back"});
		if (action == -1) {
			return;
		}

		if ((action == 1) || ((action == 3 || action == 4) && assetType.equals(AssetType.FOOD))) {
			String what = (action == 1)? "name" : (action == 3)? "description" : "category";
			getCs().printInstructions(new String[] {"Note:", "Enter '/quit' to return to main menu."});
			String input = getCs().getString("Enter the new " + what);

			if (input.equalsIgnoreCase("/quit")) {
				return;
			}

			String response;
			if (assetType.equals(AssetType.FOOD)) {
				response = updateItem(itemIndex, (action == 1) ? input : "", null, (action == 3) ? input : "", (action == 4) ? input : "");
			} else {
				response = updateItem(itemIndex, input);
			}
			System.out.println(response);
		}

		if (action == 2 && assetType.equals(AssetType.FOOD)) {
			BigDecimal price = new BigDecimal(getCs().getDouble("Enter the new price"));
			System.out.println(updateItem(itemIndex, "", price, "", ""));
		}

		if ((action == 2 && assetType.equals(AssetType.PROMO)) || (action == 5 && assetType.equals(AssetType.FOOD))) {
			getCs().printInstructions(new String[]{"Warning: This action cannot be undone.", "Y = Yes", "Any other input = NO"});
			if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
				System.out.println(removeItem(assetType, itemIndex));
			} else {
				System.out.println("Remove operation aborted.");
			}
		}
	}

	public Pair<Op, String> init() {
		FileIO f = new FileIO();
		List<String> foodData = f.read(FileIO.FileNames.FOOD_FILE);
		List<String> promoData = f.read(FileIO.FileNames.PROMO_FILE);
		String splitStr = " // ";

		if (foodData == null || promoData == null) {
			return (new Pair<>(Op.FAILED, "Failed to read files."));
		}

		if (!getRestaurant().registerClassToAsset(AlaCarteItem.class, AssetType.FOOD) || !getRestaurant().registerClassToAsset(PromoItem.class, AssetType.PROMO)) {
			return (new Pair<>(Op.FAILED, "Failed to register class."));
		}

		for (String data : foodData) {
			String[] datas = data.split(splitStr);

			if (datas.length != 5) {
				continue;
			}

			int id = Integer.parseInt(datas[0]); // risky without try/catch
			getRestaurant().add(new AlaCarteItem(id, datas[1], new BigDecimal(datas[2]), datas[3], datas[4]));

			if (id > getRestaurant().getCounter(AssetType.FOOD)) {
				getRestaurant().setCounter(AssetType.FOOD, id);
			}
		}

		for (String data : promoData) {
			String[] datas = data.split(splitStr);

			if (datas.length != 4) {
				continue;
			}

			int id = Integer.parseInt(datas[0]);
			List<AlaCarteItem> alaCarteItems = new ArrayList<>();

			for (String s : datas[3].split("--")) {
				int sId = Integer.parseInt(s);
				MenuItem item = getItemFromId(AssetType.FOOD, sId);

				if (item instanceof AlaCarteItem) {
					alaCarteItems.add((AlaCarteItem) item);
				}
			}

			getRestaurant().add(new PromoItem(id, datas[1], new BigDecimal(datas[2]), alaCarteItems));

			if (id > getRestaurant().getCounter(AssetType.PROMO)) {
				getRestaurant().setCounter(AssetType.PROMO, id);
			}
		}

		return (new Pair<>(Op.SUCCESS, "Menu OK."));
	}

	private List<String> getDisplay(AssetType asset, int... sortOptions) {
		if (!asset.equals(AssetType.FOOD) && !asset.equals(AssetType.PROMO)) {
			return null;
		}

		int sortOption = (sortOptions.length > 0)? sortOptions[0] : (asset.equals(AssetType.FOOD))? 4 : 1;
		List<? extends RestaurantAsset> masterList = new ArrayList<>(getRestaurant().getAsset(asset));

		List<String> ret = new ArrayList<>();
		if (masterList.size() == 0) {
			ret.add("There is no item added here yet.");
			return ret;
		}

		masterList.sort((item1, item2) -> {
			switch (sortOption) {
				case 2: return ((MenuItem) item1).getName().compareTo(((MenuItem) item2).getName());
				case 3: return ((MenuItem) item1).getPrice().compareTo(((MenuItem) item2).getPrice());
				case 4:
					if (item1 instanceof AlaCarteItem) {
						return ((AlaCarteItem) item1).getCategory().compareTo(((AlaCarteItem) item2).getCategory());
					}
			}

			return Integer.compare(item1.getId(), item2.getId());
		});

		ret.add((asset.equals(AssetType.FOOD)? "ID // Name // Price // Description // Category" : "ID // Name // Price // Sub-Items"));

		for (RestaurantAsset o : masterList) {
			ret.add(o.toDisplayString());
		}

		return ret;
	}

	private List<String> getItemNames(AssetType asset) {
		if (!asset.equals(AssetType.FOOD) && !asset.equals(AssetType.PROMO)) {
			return null;
		}

		List<String> ret = new ArrayList<>();
		for (RestaurantAsset o : getRestaurant().getAsset(asset)) {
			ret.add(((MenuItem) o).getName());
		}

		return ret;
	}

	public MenuItem getItemFromId(AssetType assetType, int id) {
		for (RestaurantAsset o : getRestaurant().getAsset(assetType)) {
			if (o.getId() == id) {
				if (o instanceof MenuItem) {
					return (MenuItem) o;
				}
			}
		}

		return null;
	}

	public MenuItem getItemFromIndex(AssetType assetType, int index) {
		List<? extends RestaurantAsset> assetList = getRestaurant().getAsset(assetType);

		if (index >= assetList.size()) {
			return null;
		}

		RestaurantAsset asset = assetList.get(index);
		if (asset instanceof MenuItem) {
			return (MenuItem) asset;
		}

		return null;
	}

	private String addItem(String name, BigDecimal price, String description, String category) {
		return addItem(new AlaCarteItem(getRestaurant().incrementAndGetCounter(AssetType.FOOD), name, price, description, category));
	}

	private String addItem(String name, List<Integer> alaCarteItemIndices) {
		if (alaCarteItemIndices.size() < 1) {
			return "Empty ala-carte item list.";
		}

		BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
		List<AlaCarteItem> alaCarteItemList = new ArrayList<>();
		for (int index : alaCarteItemIndices) {
			MenuItem item = getItemFromIndex(AssetType.FOOD, index);

			if (item == null) {
				return "Failed to get item from index.";
			}

			if (item instanceof AlaCarteItem) {
				alaCarteItemList.add((AlaCarteItem) item);
				price = price.add(item.getPrice());
			} else {
				return "Invalid object.";
			}
		}

		price = price.multiply(new BigDecimal(0.8));
		return addItem(new PromoItem(getRestaurant().incrementAndGetCounter(AssetType.PROMO), name, price, alaCarteItemList));
	}

	private String addItem(MenuItem item) {
		FileIO f = new FileIO();
		Pair<Op, String> response = f.writeLine((item instanceof AlaCarteItem)? FileIO.FileNames.FOOD_FILE : FileIO.FileNames.PROMO_FILE, item.toString());

		if (response.getLeft().equals(Op.SUCCESS)) {
			getRestaurant().add(item);
		}

		return response.getRight();
	}

	private String updateItem(int index, String name, BigDecimal price, String description, String category) {
		AlaCarteItem item = (AlaCarteItem) getRestaurant().getAsset(AssetType.FOOD).get(index);
		name = (name.isBlank()? item.getName() : name);
		price = ((price == null)? item.getPrice() : price);
		description = (description.isBlank()? item.getDescription() : description);
		category = (category.isBlank()? item.getCategory() : category);

		AlaCarteItem tempItem = new AlaCarteItem(item.getId(), name, price, description, category);
		return updateItem(index, item, tempItem);
	}

	private String updateItem(int index, String name) {
		PromoItem item = (PromoItem) getRestaurant().getAsset(AssetType.PROMO).get(index);
		name = (name.isBlank()? item.getName() : name);

		PromoItem tempItem = new PromoItem(item.getId(), name, item.getPrice(), item.getAlaCarteItems());
		return updateItem(index, item, tempItem);
	}

	private String updateItem(int index, MenuItem item, MenuItem tempItem) {
		FileIO f = new FileIO();
		int fileId = Integer.parseInt(f.read((item instanceof AlaCarteItem)? FileIO.FileNames.FOOD_FILE : FileIO.FileNames.PROMO_FILE).get(index).split(" // ")[0]);

		if (fileId != item.getId()) {
			return "File ID mismatch for update. (" + fileId + " VS " + item.getId() + ")";
		}

		Pair<Op, String> response = f.updateLine((item instanceof AlaCarteItem)? FileIO.FileNames.FOOD_FILE : FileIO.FileNames.PROMO_FILE, index, tempItem.toString());

		if (response.getLeft().equals(Op.SUCCESS)) {
			if (item instanceof AlaCarteItem) {
				((AlaCarteItem) item).update(tempItem.getName(), tempItem.getPrice(), ((AlaCarteItem) tempItem).getDescription(), ((AlaCarteItem) tempItem).getCategory());
			} else {
				((PromoItem) item).update(tempItem.getName(), tempItem.getPrice(), ((PromoItem) tempItem).getAlaCarteItems());
			}
		}

		return response.getRight();
	}

	private String removeItem(AssetType asset, int index) {
		if (!asset.equals(AssetType.FOOD) && !asset.equals(AssetType.PROMO)) {
			return "Invalid asset type.";
		}

		MenuItem item = (MenuItem) getRestaurant().getAsset(asset).get(index);
		FileIO f = new FileIO();
		int fileId = Integer.parseInt(f.read((item instanceof AlaCarteItem)? FileIO.FileNames.FOOD_FILE : FileIO.FileNames.PROMO_FILE).get(index).split(" // ")[0]);

		if (fileId != item.getId()) {
			return "File ID mismatch for remove. (" + fileId + " VS " + item.getId() + ")";
		}

		Pair<Op, String> response = f.removeLine((item instanceof AlaCarteItem)? FileIO.FileNames.FOOD_FILE : FileIO.FileNames.PROMO_FILE, index);

		if (response.getLeft().equals(Op.SUCCESS)) {
			getRestaurant().remove(item);
		}

		return response.getRight();
	}










	/*
	public List<AlaCarteItem> getItems() {
		return items;
	}

	private List<String> getItemNames() {
		List<String> list = new ArrayList<>();

		for (AlaCarteItem item : items) {
			list.add(item.getName());
		}

		return list;
	}

	private List<String> getSortedItems(int sortOption) {
		List<String> list = new ArrayList<>();
		List<AlaCarteItem> tempList = sortItems(sortOption);

		for (AlaCarteItem item : tempList) {
			String base = item.getName() + "\n" + item.getDescription() + "\nCategory: " + item.getCategory() + "\nPrice: " + item.getPrice() + "\nID: " + item.getItemId();
			list.add(base);
		}

		return list;
	}

	public void addSalesCount(int id, int salesCount) {
		if (id >= 10000) {
			return;
		}

		for (AlaCarteItem item : items) {
			if (item.getItemId() == id) {
				item.addSalesCount(salesCount);
				return;
			}
		}
	}

	public List<AlaCarteItem> sortItems(int sortOption) {
		List<AlaCarteItem> tempList = new ArrayList<>(List.copyOf(items));

		System.out.println("HI");
			switch(sortOption) {
				//Name
				case 0:
					tempList.sort(Comparator.comparing(AlaCarteItem::getName));
					break;
				//Price

				case 1:
					tempList.sort((s1, s2) -> Float.compare(s1.getPrice(), s2.getPrice()));
					break;

				//Category
				case 2:
					tempList.sort(Comparator.comparing(AlaCarteItem::getName));
					tempList.sort(Comparator.comparing(AlaCarteItem::getCategory));
					break;
			}

		return tempList;
	}

	private void view(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_0[0];
		String[] options = MenuConstants.MENU_SORT_ACTIONS_0;
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
		String category = sc.getString("Enter category: ");
		String description = sc.getString("Enter description: ");
		int id = cId.incrementAndGet();

		AlaCarteItem item = new AlaCarteItem(name, price, category, description, id, 0);
		FileIOHandler.write(AppConstants.FILE_NAMES[0].toLowerCase(), item.getWriteData());
		items.add(item);
	}

	private void update(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_0[2];
		List<String> list = getItemNames();
		MenuFactory.printMenu(header, list);
		int itemId = MenuFactory.loopChoice(sc, list.size());

		if (itemId == returnInput) {
			return;
		}

		if (itemId != backInput) {
			AlaCarteItem item = items.get(itemId);
			String name = sc.getString("Enter name [leave blank if no change]: ");
			float price = sc.getFloat("Enter price [enter -1 if no change]: ");
			String category = sc.getString("Enter category [leave blank if no change]: ");
			String desc = sc.getString("Enter description [leave blank if no change]: ");

			item.setName(name.isBlank()? item.getName() : name);
			item.setPrice((price < 0)? item.getPrice() : price);
			item.setCategory(category.isBlank()? item.getCategory() : category);
			item.setDescription(desc.isBlank()? item.getDescription() : desc);
			FileIOHandler.replace(AppConstants.FILE_NAMES[0].toLowerCase(), itemId, item.getWriteData());
			items.set(itemId, item);
		}
	}

	private void remove(ScannerHandler sc) {
		String header = MenuConstants.MENU_SUB_0[3];
		List<String> list = getItemNames();
		MenuFactory.printMenu(header, list);
		int itemId = MenuFactory.loopChoice(sc, list.size());

		if (itemId == returnInput) {
			return;
		}

		if (itemId != backInput) {
			String choice = sc.getString("Confirm remove? [y = remove]: ");

			if (choice.equalsIgnoreCase("y")) {
				FileIOHandler.remove(AppConstants.FILE_NAMES[0].toLowerCase(), itemId);
				items.remove(itemId);
			}
		}
	}

	public float getItemPrice(int id) {
		if (id >= 10000) {
			return 0;
		}

		for (AlaCarteItem item : items) {
			if (item.getItemId() == id) {
				return item.getPrice();
			}
		}

		return 0;
	}

	public void start(ScannerHandler sc) {
		for (AlaCarteItem item : items) {
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
					Method method = MenuManager.class.getDeclaredMethod(actions[action].toLowerCase(), ScannerHandler.class);
					method.invoke(null, sc);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					//App.printError(e, AppConstants.MENU_ID + 1);
				}
			}
		} while (action != backInput);
	}
	*/
}
