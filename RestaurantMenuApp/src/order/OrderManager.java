package order;
import Classes.Table;
import client.BaseManager;
import client.Restaurant;
import client.RestaurantAsset;
import enums.AssetType;
import menu.MenuItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderManager extends BaseManager {

	public OrderManager(Restaurant restaurant) {
		super(restaurant);

		if (!getRestaurant().mapClassToAssetType(Order.class, AssetType.ORDER)) {
			System.out.println("FAIL");
		}
	}

	@Override
	public void init() {

	}

	@Override
	public String[] getMainCLIOptions() {
		return new String[] {
				"View orders",
				"Add new order",
				"Add item to order",
				"Remove order",
				"Print bill",
		};
	}

	@Override
	public Runnable[] getOptionRunnables() {
		return new Runnable[] {
				this::viewOrder,
				this::addNewOrder,
				this::addItem,
				this::removeOrder,
				this::printBill
		};
	}

	private void addNewOrder()
	{
		int staffId = getCs().getInt("Please enter staff ID");
		int tableId = getCs().getInt("Please enter table ID");

		if(!((tableId>=20 && tableId<=29)||(tableId>=40 && tableId<=49)||(tableId>=80 && tableId<=84)||(tableId>=100 && tableId<=104))) {
			System.out.println("TableID error.");
			return;
		}

		try {
			Table table = (Table) getRestaurant().getAssetFromId(AssetType.TABLE, tableId);
			if (table.checkOccupied()) {
				System.out.println("Table is occupied!");
				return;
			}

			int orderId = getRestaurant().incrementAndGetCounter(AssetType.ORDER);
			Order order = table.attachOrder(orderId, staffId);
			getRestaurant().addNew(order);
			System.out.println("Order added");
		} catch (Restaurant.AssetNotRegisteredException | IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private void viewOrder() {
		int choice = 1;
		List<String> displayList;

		do {
			try {
				displayList = getDisplay(choice);
			} catch (Restaurant.AssetNotRegisteredException e) {
				System.out.println(e.getMessage());
				return;
			}
			getCs().printDisplayTable("View Order", displayList);
		} while ((choice = getCs().printChoices("Command // Corresponding Function", Arrays.asList("Sort by ID", "Sort by STAFF ID", "Sort by table ID"), new String[] {"Go back"})) != -1);
	}

	private void removeOrder()
	{
		List<Table> activeTableList = getActiveTables();
		List<String> nameList = new ArrayList<>();

		for (Table table : activeTableList) {
			nameList.add("Table " + table.getId());
		}

		int tableIndex = getCs().printChoices("Index // Active Tables", nameList, new String[]{"Go back"});
		if (tableIndex == -1) {
			return;
		}

		tableIndex -= 1;
		Table table = activeTableList.get(tableIndex);

		try {
			for (RestaurantAsset o : getRestaurant().getAsset(AssetType.ORDER)) {
				if (o.getId() == table.getId()) {
					if (o instanceof Order) {
						getRestaurant().remove(o);
						table.clear();
						System.out.println("Order has been voided.");
						return;
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}

		System.out.println("Error!");
	}


	private void addItem() {
		List<Table> activeTableList = getActiveTables();
		List<String> nameList = new ArrayList<>();

		for (Table table : activeTableList) {
			nameList.add("Table " + table.getId());
		}

		int tableIndex = getCs().printChoices("Index // Active Tables", nameList, new String[]{"Go back"});
		if (tableIndex == -1) {
			return;
		}

		tableIndex -= 1;
		int type = getCs().getInt("Please enter the item type");
		AssetType assetTYpe = (type == 1)? AssetType.ALACARTE : AssetType.PROMO_PACKAGE;
		int itemId = getCs().getInt("Please enter the item ID");
		int itemCount = getCs().getInt("Please enter the item count");
		Table table = activeTableList.get(tableIndex);

		try {
			for (RestaurantAsset o : getRestaurant().getAsset(AssetType.ORDER)) {
				if (o.getId() == table.getId()) {
					if (o instanceof Order) {
						RestaurantAsset item = getRestaurant().getAssetFromId(assetTYpe, itemId);

						if (item instanceof MenuItem) {
							((Order) o).addItem(new OrderItem((MenuItem) item, itemCount));
							getRestaurant().update(o);
							System.out.println("Order has been added.");
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("Error. Order not added.");
	}

	private void printBill()
	{
		List<Table> activeTableList = getActiveTables();
		List<String> nameList = new ArrayList<>();

		for (Table table : activeTableList) {
			nameList.add("Table " + table.getId());
		}

		int tableIndex = getCs().printChoices("Index // Active Tables", nameList, new String[]{"Go back"});
		if (tableIndex == -1) {
			return;
		}

		tableIndex -= 1;
		Table table = activeTableList.get(tableIndex);

		try {
			for (RestaurantAsset o : getRestaurant().getAsset(AssetType.ORDER)) {
				if (o.getId() == table.getId()) {
					if (o instanceof Order) {
						getCs().printDisplayTable("Invoice for table " + table.getId(), Collections.singletonList(((Order) o).toInvoiceString()));
						o.toPrintString();
						getRestaurant().remove(o);
						table.clear();
						return;
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private List<String> getDisplay(int... sortOptions) throws Restaurant.AssetNotRegisteredException {
		int sortOption = (sortOptions.length > 0)? sortOptions[0] : 1;
		List<? extends RestaurantAsset> masterList = new ArrayList<>(getRestaurant().getAsset(AssetType.ORDER));

		List<String> ret = new ArrayList<>();
		if (masterList.size() == 0) {
			ret.add("There is no ORDER added yet.");
			return ret;
		}

		masterList.sort((item1, item2) -> {
			switch (sortOption) {
				case 2: return Integer.compare(((Order) item1).getOrderId(), ((Order) item2).getOrderId());
				case 3: return Integer.compare(((Order) item1).getStaffID(), ((Order) item2).getStaffID());
			}

			return Integer.compare(item1.getId(), item2.getId());
		});

		ret.add("Order ID // Table ID // Staff ID // Order Items");

		for (RestaurantAsset o : masterList) {
			ret.add(o.toTableString());
		}

		return ret;
	}

	private List<Table> getActiveTables() {
		List<Table> nameList = new ArrayList<>();

		try {
			for (RestaurantAsset o : getRestaurant().getAsset(AssetType.TABLE)) {
				if (o instanceof Table) {
					if (((Table) o).hasOrder()) {
						nameList.add((Table) o);
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return nameList;
	}
}
