package order;
import java.util.*;

import Classes.Table;
import client.BaseManager;
import client.Restaurant;
import client.RestaurantAsset;
import enums.AssetType;
import menu.MenuItem;
import tools.Pair;

public class OrderManager extends BaseManager {

	public OrderManager(Restaurant restaurant) {
		super(restaurant);

		if (!getRestaurant().mapClassToAssetType(Order.class, AssetType.ORDER)) {
			System.out.println("FAIL");
		}
	}

	@Override
	public Pair<Op, String> init() {
		return null;
	}

	@Override
	public String[] getMainCLIOptions() {
		return new String[] {
				"View orders",
				"Add new ORDER",
				"Add item to ORDER",
				"Remove ORDER",
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

	public void addNewOrder()
	{
		int staffId = getCs().getInt("Please enter STAFF ID");
		int tableId = getCs().getInt("Please enter table ID");

		if(!((tableId>=20 && tableId<=29)||(tableId>=40 && tableId<=49)||(tableId>=80 && tableId<=84)||(tableId>=100 && tableId<=104))) {
			System.out.println("TableID error.");
			return;
		}

		if(getRestaurant().getTableManager().checkOccupied(tableId)) {
			System.out.println("Table is occupied!");
			return;
		}

		Order x = new Order(getRestaurant().incrementAndGetCounter(AssetType.ORDER), staffId, tableId);
		getRestaurant().add(x);
		getRestaurant().getTableManager().setOccupied(tableId, x.orderID);
		System.out.println("Order added");
	}

	private void viewOrder() {
		int choice = 1;
		List<String> displayList;

		do {
			displayList = getDisplay(choice);
			if (displayList == null) {
				System.out.println("Failed to get list.");
				return;
			}
			getCs().printDisplayTable("View Order", displayList);
		} while ((choice = getCs().printChoices("Command // Corresponding Function", Arrays.asList("Sort by ID", "Sort by STAFF ID", "Sort by table ID"), new String[] {"Go back"})) != -1);
	}

	public void removeOrder()
	{
		List<Table> activeTableList = getActiveTables();
		List<String> nameList = new ArrayList<>();

		for (Table table : activeTableList) {
			nameList.add("Table " + table.getTableID());
		}

		if (activeTableList == null) {
			System.out.println("Failed to get list.");
			return;
		}

		int tableIndex = getCs().printChoices("Index // Active Tables", nameList, new String[]{"Go back"});
		if (tableIndex == -1) {
			return;
		}

		tableIndex -= 1;
		Table table = activeTableList.get(tableIndex);

		for (RestaurantAsset o : getRestaurant().getAsset(AssetType.ORDER)) {
			if (o.getId() == table.getOrderID()) {
				if (o instanceof Order) {
					getRestaurant().remove(o);
					getRestaurant().getTableManager().clear(table.getTableID());
					return;
				}
			}
		}

		System.out.println("Error!");
	}


	public void addItem() {
		List<Table> activeTableList = getActiveTables();
		List<String> nameList = new ArrayList<>();

		for (Table table : activeTableList) {
			nameList.add("Table " + table.getTableID());
		}

		if (activeTableList == null) {
			System.out.println("Failed to get list.");
			return;
		}

		int tableIndex = getCs().printChoices("Index // Active Tables", nameList, new String[]{"Go back"});
		if (tableIndex == -1) {
			return;
		}

		tableIndex -= 1;
		int itemId = getCs().getInt("Please enter the item ID");
		int itemCount = getCs().getInt("Please enter the item count");
		Table table = activeTableList.get(tableIndex);

		for (RestaurantAsset o : getRestaurant().getAsset(AssetType.ORDER)) {
			if (o.getId() == table.getOrderID()) {
				if (o instanceof Order) {
					RestaurantAsset item;
					if (itemId >= 100000) {
						item = getRestaurant().getItemFromId(AssetType.PROMO_PACKAGE, itemId);
					} else {
						 item = getRestaurant().getItemFromId(AssetType.ALACARTE, itemId);
					}

					if (item instanceof MenuItem) {
						((Order) o).addItem(new OrderItem((MenuItem) item, itemCount));
						System.out.println("Order has been added.");
						return;
					}
				}
			}
		}

		System.out.println("Error. Order not added.");
	}

	public void printBill()
	{
		List<Table> activeTableList = getActiveTables();
		List<String> nameList = new ArrayList<>();

		for (Table table : activeTableList) {
			nameList.add("Table " + table.getTableID());
		}

		if (activeTableList == null) {
			System.out.println("Failed to get list.");
			return;
		}

		int tableIndex = getCs().printChoices("Index // Active Tables", nameList, new String[]{"Go back"});
		if (tableIndex == -1) {
			return;
		}

		tableIndex -= 1;
		Table table = activeTableList.get(tableIndex);

		for (RestaurantAsset o : getRestaurant().getAsset(AssetType.ORDER)) {
			if (o.getId() == table.getOrderID()) {
				if (o instanceof Order) {
					getCs().printDisplayTable("Invoice for table " + table.getTableID(), Collections.singletonList(((Order) o).toInvoiceString()));
					o.toString();
					getRestaurant().remove(o);
					getRestaurant().getTableManager().clear(table.getTableID());
					return;
				}
			}
		}

		/*
		int tableID;
		int tempOrderID = -1;
		System.out.println("Please enter the table ID");
		tableID = s.nextInt();
		if(!InputChecker.checkTableID(tableID))
		{
			System.out.println("TableID Error.");
			return;
		}
		tempOrderID = tm.getOrderID(tableID);
		tm.clear(tableID);
		for(int j = 0; j<orderList.size();j++)
		{
			if(orderList.get(j).orderID == tempOrderID)
			{
				System.out.println("Test1");
				orderList.get(j).print(tableID,pm,mm);
				orderList.remove(j);
			}
		}
		*/
	}

	private List<String> getDisplay(int... sortOptions) {
		int sortOption = (sortOptions.length > 0)? sortOptions[0] : 1;
		List<? extends RestaurantAsset> masterList = new ArrayList<>(getRestaurant().getAsset(AssetType.ORDER));

		List<String> ret = new ArrayList<>();
		if (masterList.size() == 0) {
			ret.add("There is no ORDER added yet.");
			return ret;
		}

		masterList.sort((item1, item2) -> {
			switch (sortOption) {
				case 2: return Integer.compare(((Order) item1).getStaffID(), ((Order) item2).getStaffID());
				case 3: return Integer.compare(((Order) item1).getTableID(), ((Order) item2).getTableID());
			}

			return Integer.compare(((Order) item1).getOrderID(), ((Order) item2).getOrderID());
		});

		ret.add("Order ID // Table ID // Staff ID // Order Items");

		for (RestaurantAsset o : masterList) {
			ret.add(o.toTableString());
		}

		return ret;
	}

	private List<Table> getActiveTables() {
		List<Table> nameList = new ArrayList<>();

		for (Table table : getRestaurant().getTableManager().tableList) {
			if (table.getOrderID() != -1) {
				nameList.add(table);
			}
		}

		return nameList;
	}
}
