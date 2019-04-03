package menu;

import client.BaseManager;
import client.Restaurant;
import client.RestaurantAsset;
import enums.AssetType;
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
		FileIO f = new FileIO();
		List<String> foodData;
		List<String> promoData;

		try {
			foodData = f.read(AssetType.ALACARTE);
			promoData = f.read(AssetType.PROMO_PACKAGE);
		} catch (IOException e) {
			throw (new ManagerInitFailedException(this, "Unable to load menu items from file: " + e.getMessage()));
		}

		String splitStr = " // ";

		if (!getRestaurant().mapClassToAssetType(AlaCarteItem.class, AssetType.ALACARTE) || !getRestaurant().mapClassToAssetType(PromotionPackage.class, AssetType.PROMO_PACKAGE)) {
			throw (new ManagerInitFailedException(this, "Failed to register class and asset to restaurant."));
		}

		for (String data : foodData) {
			String[] datas = data.split(splitStr);

			if (datas.length != 5) {
				continue;
			}

			int id = Integer.parseInt(datas[0]); // risky without try/catch

			try {
				getRestaurant().addFromFile(new AlaCarteItem(id, datas[1], new BigDecimal(datas[2]), datas[3], datas[4]));
			} catch (Restaurant.AssetNotRegisteredException | IOException e) {
				throw (new ManagerInitFailedException(this, e.getMessage()));
			}

			if (id > getRestaurant().getCounter(AssetType.ALACARTE)) {
				getRestaurant().setCounter(AssetType.ALACARTE, id);
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
				MenuItem item;

				try {
					item = (MenuItem) getRestaurant().getItemFromId(AssetType.ALACARTE, sId);
				} catch (Restaurant.AssetNotRegisteredException e) {
					throw (new ManagerInitFailedException(this, e.getMessage()));
				}

				if (item instanceof AlaCarteItem) {
					alaCarteItems.add((AlaCarteItem) item);
				}
			}

			try {
				getRestaurant().addFromFile(new PromotionPackage(id, datas[1], new BigDecimal(datas[2]), alaCarteItems));
			} catch (Restaurant.AssetNotRegisteredException | IOException e) {
				throw (new ManagerInitFailedException(this, e.getMessage()));
			}

			if (id > getRestaurant().getCounter(AssetType.PROMO_PACKAGE)) {
				getRestaurant().setCounter(AssetType.PROMO_PACKAGE, id);
			}
		}
	}

	private List<String> getDisplay(AssetType assetType, int sortOption) throws InvalidAssetTypeException, Restaurant.AssetNotRegisteredException {
		if (!assetType.equals(AssetType.ALACARTE) && !assetType.equals(AssetType.PROMO_PACKAGE)) {
			throw (new InvalidAssetTypeException(assetType));
		}

		List<? extends RestaurantAsset> masterList = new ArrayList<>(getRestaurant().getAsset(assetType));
		List<String> ret = new ArrayList<>();
		if (masterList.size() == 0) {
			ret.add("There is no " + (assetType.equals(AssetType.ALACARTE)? "ala-carte item" : "promotion package") + " on the menu.");
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

		ret.add((assetType.equals(AssetType.ALACARTE)? "ID // Name // Price // Description // Category" : "ID // Name // Price // Sub-Items"));
		for (RestaurantAsset o : masterList) {
			ret.add(o.toTableString());
		} return ret;
	}

	private List<String> getItemNames(AssetType assetType) throws Restaurant.AssetNotRegisteredException, InvalidAssetTypeException {
		if (!assetType.equals(AssetType.ALACARTE) && !assetType.equals(AssetType.PROMO_PACKAGE)) {
			throw (new InvalidAssetTypeException(assetType));
		}

		List<String> ret = new ArrayList<>();
		for (RestaurantAsset o : getRestaurant().getAsset(assetType)) {
			ret.add(((MenuItem) o).getName());
		}

		return ret;
	}

	private void addNewItem(String name, BigDecimal price, String description, String category) throws IOException, Restaurant.AssetNotRegisteredException {
		addNewItem(new AlaCarteItem(getRestaurant().incrementAndGetCounter(AssetType.ALACARTE), name, price, description, category));
	}

	private void addNewItem(String name, List<Integer> alaCarteItemIndices) throws Exception {
		if (alaCarteItemIndices.size() < 1) {
			throw (new Exception("Ala-carte item list is empty?"));
		}

		BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
		List<AlaCarteItem> alaCarteItemList = new ArrayList<>();

		for (int index : alaCarteItemIndices) {
			MenuItem item = (MenuItem) getRestaurant().getItemFromIndex(AssetType.ALACARTE, index);

			if (item instanceof AlaCarteItem) {
				alaCarteItemList.add((AlaCarteItem) item);
				price = price.add(item.getPrice());
			} else {
				throw (new Exception("Object instance is wrong?"));
			}
		}

		price = price.multiply(new BigDecimal(0.8));
		addNewItem(new PromotionPackage(getRestaurant().incrementAndGetCounter(AssetType.PROMO_PACKAGE), name, price, alaCarteItemList));
	}

	private void addNewItem(MenuItem item) throws IOException, Restaurant.AssetNotRegisteredException {
		getRestaurant().addNew(item);
	}

	private void updateItem(int index, String name, BigDecimal price, String description, String category) throws Restaurant.FileIDMismatchException, IOException, Restaurant.AssetNotRegisteredException {
		AlaCarteItem item = (AlaCarteItem) getRestaurant().getAsset(AssetType.ALACARTE).get(index);
		name = (name.isBlank()? item.getName() : name);
		price = ((price == null)? item.getPrice() : price);
		description = (description.isBlank()? item.getDescription() : description);
		category = (category.isBlank()? item.getCategory() : category);

		AlaCarteItem tempItem = new AlaCarteItem(item.getId(), name, price, description, category);
		updateItem(item, tempItem);
	}

	private void updateItem(int index, String name) throws Restaurant.FileIDMismatchException, IOException, Restaurant.AssetNotRegisteredException {
		PromotionPackage item = (PromotionPackage) getRestaurant().getAsset(AssetType.PROMO_PACKAGE).get(index);
		name = (name.isBlank()? item.getName() : name);

		PromotionPackage tempItem = new PromotionPackage(item.getId(), name, item.getPrice(), item.getAlaCarteItems());
		updateItem(item, tempItem);
	}

	private void updateItem(MenuItem item, MenuItem tempItem) throws Restaurant.AssetNotRegisteredException, Restaurant.FileIDMismatchException, IOException {
		getRestaurant().update(tempItem);

		if (item instanceof AlaCarteItem) {
			((AlaCarteItem) item).update(tempItem.getName(), tempItem.getPrice(), ((AlaCarteItem) tempItem).getDescription(), ((AlaCarteItem) tempItem).getCategory());
		} else {
			((PromotionPackage) item).update(tempItem.getName());
		}
	}

	private void removeItem(AssetType assetType, int index) throws Restaurant.AssetNotRegisteredException, Restaurant.FileIDMismatchException, IOException {
		MenuItem item = (MenuItem) getRestaurant().getAsset(assetType).get(index);
		getRestaurant().remove(item);
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
				() -> addMenuItem(AssetType.ALACARTE),
				() -> addMenuItem(AssetType.PROMO_PACKAGE),
				() -> manageMenuItems(AssetType.ALACARTE),
				() -> manageMenuItems(AssetType.PROMO_PACKAGE)
		};
	}

	private void viewMenu() {
		int choice = 4;
		List<String> displayList;

		do {
			try {
				displayList = getDisplay(AssetType.ALACARTE, choice);
			} catch (InvalidAssetTypeException | Restaurant.AssetNotRegisteredException e) {
				System.out.println(e.getMessage());
				return;
			}

			getCs().printDisplayTable("Menu: Ala-Carte Items", displayList);

			try {
				displayList = getDisplay(AssetType.PROMO_PACKAGE, choice);
			} catch (InvalidAssetTypeException | Restaurant.AssetNotRegisteredException e) {
				System.out.println(e.getMessage());
				return;
			}

			getCs().printDisplayTable("Menu: Promotional Packages", displayList);
		} while ((choice = getCs().printChoices("Command // Corresponding Function", Arrays.asList("Sort by ID", "Sort by name", "Sort by price", "Sort by category"), new String[] {"Go back"})) != -1);
	}

	private void addMenuItem(AssetType assetType) {
		getCs().printInstructions(new String[] {"Note:", "Enter '/quit' in name to return to main menu."});
		String name = getCs().getString("Enter item name");
		if (name.equalsIgnoreCase("/quit")) {
			return;
		}

		if (assetType.equals(AssetType.ALACARTE)) {
			BigDecimal price = new BigDecimal(getCs().getDouble("Enter item's price"));
			String description = getCs().getString("Enter item description");
			String category = getCs().getString("Enter item category");

			try {
				addNewItem(name, price, description, category);
				System.out.println("Item has been added successfully.");
			} catch (Restaurant.AssetNotRegisteredException | IOException e) {
				System.out.println(e.getMessage());
			}

			return;
		}

		List<String> alaCarteItemList;

		try {
			alaCarteItemList = getItemNames(AssetType.ALACARTE);
		} catch (Restaurant.AssetNotRegisteredException | InvalidAssetTypeException e) {
			System.out.println(e.getMessage());
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

		try {
			addNewItem(name, alaCarteItemIndices);
			System.out.println("Item has been added successfully.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void manageMenuItems(AssetType assetType) {
		List<String> nameList;

		try {
			nameList = getItemNames(assetType);
		} catch (Restaurant.AssetNotRegisteredException | InvalidAssetTypeException e) {
			System.out.println(e.getMessage());
			return;
		}

		int itemIndex = getCs().printChoices("Index // " + (assetType.equals(AssetType.ALACARTE)? "Ala-Carte Items" : "Promotional Packages"), nameList, new String[]{"Go back"}) - 1;
		if (itemIndex == -2) {
			return;
		}

		String[] actions = assetType.equals(AssetType.ALACARTE)? new String[] {"Change name.", "Change price.", "Change description.", "Change category", "Remove item from menu."} : new String[] {"Change name.", "Remove package from menu."};

		int action = getCs().printChoices("Index // Action", actions, new String[]{"Go back"});
		if (action == -1) {
			return;
		}

		if ((action == 1) || ((action == 3 || action == 4) && assetType.equals(AssetType.ALACARTE))) {
			String what = (action == 1)? "name" : (action == 3)? "description" : "category";
			getCs().printInstructions(new String[] {"Note:", "Enter '/quit' to return to main menu."});
			String input = getCs().getString("Enter the new " + what);

			if (input.equalsIgnoreCase("/quit")) {
				return;
			}

			try {
				if (assetType.equals(AssetType.ALACARTE)) {
					updateItem(itemIndex, (action == 1) ? input : "", null, (action == 3) ? input : "", (action == 4) ? input : "");
				} else {
					updateItem(itemIndex, input);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return;
			}
		}

		if (action == 2 && assetType.equals(AssetType.ALACARTE)) {
			BigDecimal price = new BigDecimal(getCs().getDouble("Enter the new price"));

			try {
				updateItem(itemIndex, "", price, "", "");
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return;
			}
		}

		if ((action == 2 && assetType.equals(AssetType.PROMO_PACKAGE)) || (action == 5 && assetType.equals(AssetType.ALACARTE))) {
			getCs().printInstructions(new String[]{"Warning: This action cannot be undone.", "Y = Yes", "Any other input = NO"});
			if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
				try {
					removeItem(assetType, itemIndex);
				} catch (Restaurant.AssetNotRegisteredException | IOException | Restaurant.FileIDMismatchException e) {
					System.out.println(e.getMessage());
				}
			} else {
				System.out.println("Remove operation aborted.");
			}
		}
	}

	private class InvalidAssetTypeException extends Exception {
		private InvalidAssetTypeException(AssetType assetType) {
			super("Invalid asset type: " + assetType);
		}
	}
}
