package menu;

import client.BaseManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import tools.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MenuManager extends BaseManager {

	public MenuManager(Restaurant restaurant) {
		super(restaurant);
	}

	@Override
	public void init() throws ManagerInitFailedException {
		try {
			getRestaurant().registerClass(AlaCarteItem.class, DataType.ALACARTE);
			getRestaurant().registerClass(PromotionPackage.class, DataType.PROMO_PACKAGE);
		} catch (Restaurant.ClassNotRegisteredException e) {
			throw (new ManagerInitFailedException(this, "Class registration failed: " + e.getMessage()));
		}

		FileIO f = new FileIO();
		List<String> foodData;
		List<String> promoData;

		try {
			foodData = f.read(DataType.ALACARTE);
			promoData = f.read(DataType.PROMO_PACKAGE);
		} catch (IOException e) {
			throw (new ManagerInitFailedException(this, "Unable to load menu items from file: " + e.getMessage()));
		}

		getRestaurant().setCounter(DataType.PROMO_PACKAGE, 99999);

		for (String data : foodData) {
			String[] datas = data.split(" // ");

			if (datas.length != 5) {
				continue;
			}

			int id;
			try {
				id = Integer.parseInt(datas[0]);
				getRestaurant().load(new AlaCarteItem(id, datas[1], new BigDecimal(datas[2]), datas[3], datas[4]));
			} catch (NumberFormatException e) {
				throw (new ManagerInitFailedException(this, "Invalid file data: " + e.getMessage()));
			}

			if (id > getRestaurant().getCounter(DataType.ALACARTE)) {
				getRestaurant().setCounter(DataType.ALACARTE, id);
			}
		}

		for (String data : promoData) {
			String[] datas = data.split(" // ");

			if (datas.length != 4) {
				continue;
			}

			List<AlaCarteItem> alaCarteItems = new ArrayList<>();
			for (String s : datas[3].split("--")) {
				int sId;
				try {
					sId = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					throw (new ManagerInitFailedException(this, "Invalid file data: " + e.getMessage()));
				}

				MenuItem item = (MenuItem) getRestaurant().getDataFromId(DataType.ALACARTE, sId);
				if (item instanceof AlaCarteItem) {
					alaCarteItems.add((AlaCarteItem) item);
				}
			}

			int id;
			try {
				id = Integer.parseInt(datas[0]);
				getRestaurant().load(new PromotionPackage(id, datas[1], new BigDecimal(datas[2]), alaCarteItems));
			} catch (NumberFormatException e) {
				throw (new ManagerInitFailedException(this, "Invalid file data: " + e.getMessage()));
			}

			if (id > getRestaurant().getCounter(DataType.PROMO_PACKAGE)) {
				getRestaurant().setCounter(DataType.PROMO_PACKAGE, id);
			}
		}
	}

	private Set<String> getAlaCarteCategories() {
		List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.ALACARTE);
		Set<String> ret = new TreeSet<>();

		for (RestaurantData data : dataList) {
			if (data instanceof AlaCarteItem) {
				ret.add(((AlaCarteItem) data).getCategory());
			}
		} return ret;
	}

	private List<String> getAlaCarteDisplayData(String category) {
		List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.ALACARTE);
		List<String> ret = new ArrayList<>();

		if (dataList.size() == 0) {
			ret.add("Database is empty for " + DataType.ALACARTE.name() + ".");
		}

		for (RestaurantData data : dataList) {
			if ((data instanceof AlaCarteItem) && (((AlaCarteItem) data).getCategory().equalsIgnoreCase(category))) {
				ret.add(((AlaCarteItem) data).getName() + "\n" + ((AlaCarteItem) data).getDescription() + " // " + ((AlaCarteItem) data).getPrice());
			}
		}
		Collections.sort(ret);

		int startIndex = (int) Math.ceil(1.0 * ret.size() / 2);
		for (int index = startIndex; index < ret.size(); index++) {
			ret.set(index - startIndex, ret.get(index - startIndex) + " // " + ret.get(index));
		}

		for (int index = ret.size() - 1; index >= startIndex; index--) {
			ret.remove(index);
		}

		for (int index = 1; index < ret.size(); index += 2) {
			ret.add(index, "");
		}

		return ret;
	}

	public List<String> getItemNames(DataType dataType) {
		List<String> ret = new ArrayList<>();
		for (RestaurantData o : getRestaurant().getData(dataType)) {
			ret.add(((MenuItem) o).getName());
		} return ret;
	}

	private boolean existsInPackage(AlaCarteItem item) {
		for (RestaurantData data : getRestaurant().getData(DataType.PROMO_PACKAGE)) {
			if (data instanceof PromotionPackage) {
				for (AlaCarteItem alaCarteItem : ((PromotionPackage) data).getAlaCarteItems()) {
					if (alaCarteItem.getId() == item.getId()) {
						return true;
					}
				}
			}
		} return false;
	}

	private void addNewItem(String name, BigDecimal price, String description, String category) throws IOException {
		addNewItem(new AlaCarteItem(getRestaurant().incrementAndGetCounter(DataType.ALACARTE), name, price, description, category));
	}

	private void addNewItem(String name, List<Integer> alaCarteItemIndices) throws Exception {
		if (alaCarteItemIndices.size() < 1) {
			throw (new Exception("Ala-carte item list is empty?"));
		}

		BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
		List<AlaCarteItem> alaCarteItemList = new ArrayList<>();

		for (int index : alaCarteItemIndices) {
			MenuItem item = (MenuItem) getRestaurant().getDataFromIndex(DataType.ALACARTE, index);
			alaCarteItemList.add((AlaCarteItem) item);
			price = price.add(item.getPrice());
		}

		price = price.multiply(new BigDecimal(0.8));
		addNewItem(new PromotionPackage(getRestaurant().incrementAndGetCounter(DataType.PROMO_PACKAGE), name, price, alaCarteItemList));
	}

	private void addNewItem(MenuItem item) throws IOException {
		getRestaurant().save(item);
	}

	private void updateItem(int index, String name, BigDecimal price, String description, String category) throws Restaurant.FileIDMismatchException, IOException {
		AlaCarteItem item = (AlaCarteItem) getRestaurant().getData(DataType.ALACARTE).get(index);
		name = (name.isBlank()? item.getName() : name);
		price = ((price == null)? item.getPrice() : price);
		description = (description.isBlank()? item.getDescription() : description);
		category = (category.isBlank()? item.getCategory() : category);

		AlaCarteItem tempItem = new AlaCarteItem(item.getId(), name, price, description, category);
		updateItem(item, tempItem);
	}

	private void updateItem(int index, String name) throws Restaurant.FileIDMismatchException, IOException {
		PromotionPackage item = (PromotionPackage) getRestaurant().getData(DataType.PROMO_PACKAGE).get(index);
		name = (name.isBlank()? item.getName() : name);

		PromotionPackage tempItem = new PromotionPackage(item.getId(), name, item.getPrice(), item.getAlaCarteItems());
		updateItem(item, tempItem);
	}

	private void updateItem(MenuItem item, MenuItem tempItem) throws Restaurant.FileIDMismatchException, IOException {
		getRestaurant().update(tempItem);

		if (item instanceof AlaCarteItem) {
			((AlaCarteItem) item).update(tempItem.getName(), tempItem.getPrice(), ((AlaCarteItem) tempItem).getDescription(), ((AlaCarteItem) tempItem).getCategory());
		} else {
			((PromotionPackage) item).update(tempItem.getName());
		}
	}

	private boolean removeItem(DataType dataType, int index) throws Restaurant.FileIDMismatchException, IOException {
		MenuItem item = (MenuItem) getRestaurant().getData(dataType).get(index);

		if (dataType.equals(DataType.ALACARTE)) {
			return !existsInPackage((AlaCarteItem) item);
		}

		if (getRestaurant().getData(DataType.ORDER).size() > 0) {
			return false;
		}

		getRestaurant().remove(item);
		return true;
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
				() -> addMenuItem(DataType.ALACARTE),
				() -> addMenuItem(DataType.PROMO_PACKAGE),
				() -> manageMenuItems(DataType.ALACARTE),
				() -> manageMenuItems(DataType.PROMO_PACKAGE)
		};
	}

	private void viewMenu() {
		List<String> displayList;
		Set<String> categoryList = getAlaCarteCategories();
		System.out.println();

		for (String category : categoryList) {
			displayList = getAlaCarteDisplayData(category);
			getCs().printTitle(category, true);
			getCs().printColumns(displayList, false, false, false, false, true);
			getCs().printDivider('=');
		}

		displayList = getDisplayData(DataType.PROMO_PACKAGE);
		displayList.add(0, "ID // Package // Price // Items in Package");
		for (int index = 2; index < displayList.size(); index += 2) {
			displayList.add(index, "");
		}

		getCs().printDisplayTable("Promotion Packages", displayList, false, false);
		getCs().printInstructions(new String[] {"Enter -1 to go back"});
		getCs().getInt("Enter -1 to go back", -1, -1);
	}

	private void addMenuItem(DataType dataType) {
		getCs().printInstructions(new String[] {"Note:", "Enter '/quit' in name to return to main menu."});
		String name = getCs().getString("Enter item name");
		if (name.equalsIgnoreCase("/quit")) {
			return;
		}

		if (dataType.equals(DataType.ALACARTE)) {
			BigDecimal price = new BigDecimal(getCs().getDouble("Enter item's price"));
			String description = getCs().getString("Enter item description");
			String category = getCs().getString("Enter item category");

			try {
				addNewItem(name, price, description, category);
				System.out.println("Item has been added successfully.");
			} catch (IOException e) {
				System.out.println("Failed to add item: " + e.getMessage());
			}
		} else {
			List<String> alaCarteItemList = getItemNames(DataType.ALACARTE);
			List<Integer> alaCarteItemIndices = new ArrayList<>();
			String[] footerOptions = new String[]{"Go back"};
			getCs().printChoicesSimple("Index // Ala-Carte Items", alaCarteItemList, footerOptions);
			getCs().printInstructions(new String[]{"Select an ala-carte item to add to the package."});
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

			try {
				addNewItem(name, alaCarteItemIndices);
				System.out.println("Item has been added to menu successfully.");
			} catch (Exception e) {
				System.out.println("Failed to add item: " + e.getMessage());
			}
		}
	}

	private void manageMenuItems(DataType dataType) {
		List<String> nameList = getItemNames(dataType);
		int itemIndex = getCs().printChoices("Select an item to manage", "Index // " + (dataType.equals(DataType.ALACARTE)? "Ala-Carte Items" : "Promotional Packages"), nameList, new String[]{"Go back"}) - 1;
		if (itemIndex == -2) {
			return;
		}

		String[] actions = dataType.equals(DataType.ALACARTE)?
				new String[] {"Change name.", "Change price.", "Change description.", "Change category", "Remove item from menu."} :
				new String[] {"Change name.", "Remove package from menu."};

		int action = getCs().printChoices("Select an action", "Index // Action", actions, new String[]{"Go back"});
		if (action == -1) {
			return;
		}

		if ((action == 1) || ((action == 3 || action == 4) && dataType.equals(DataType.ALACARTE))) {
			String what = (action == 1)? "name" : (action == 3)? "description" : "category";
			getCs().printInstructions(new String[] {"Note:", "Enter '/quit' to return to main menu."});
			String input = getCs().getString("Enter the new " + what);

			if (input.equalsIgnoreCase("/quit")) {
				return;
			}

			try {
				if (dataType.equals(DataType.ALACARTE)) {
					updateItem(itemIndex, (action == 1) ? input : "", null, (action == 3) ? input : "", (action == 4) ? input : "");
				} else {
					updateItem(itemIndex, input);
				}

				System.out.println("Item has been updated successfully.");
			} catch (Exception e) {
				System.out.println("Failed to update item: " + e.getMessage());
				return;
			}
		}

		if (action == 2 && dataType.equals(DataType.ALACARTE)) {
			BigDecimal price = new BigDecimal(getCs().getDouble("Enter the new price"));

			try {
				updateItem(itemIndex, "", price, "", "");
			} catch (Exception e) {
				System.out.println("Failed to update item: " + e.getMessage());
				return;
			}
		}

		if ((action == 2 && dataType.equals(DataType.PROMO_PACKAGE)) || (action == 5 && dataType.equals(DataType.ALACARTE))) {
			getCs().printInstructions(new String[]{"Warning: This action cannot be undone.", "Y = Yes", "Any other input = NO"});
			if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
				try {
					if (removeItem(dataType, itemIndex)) {
						System.out.println("Item has been removed from menu successfully.");
					} else {
						System.out.println("Failed to remove item: Please check that there is no active order and that the item is not part of a package.");
					}
				} catch (IOException | Restaurant.FileIDMismatchException e) {
					System.out.println("Failed to remove item: " + e.getMessage());
				}
			} else {
				System.out.println("Remove operation aborted.");
			}
		}
	}
}
