package menu;

import client.DataManager;
import client.Restaurant;
import enums.DataType;
import tools.FileIO;
import tools.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class MenuManager extends DataManager {

    public MenuManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() {
        getRestaurant().registerClassToDataType(AlaCarteItem.class, DataType.ALA_CARTE_ITEM);
        getRestaurant().registerClassToDataType(PromotionPackage.class, DataType.PROMO_PACKAGE);
        getRestaurant().setUniqueId(DataType.PROMO_PACKAGE, 99999);

        Comparator<MenuItem> comparator = Comparator.comparing(MenuItem::getName);
        getRestaurant().setDefaultComparator(DataType.ALA_CARTE_ITEM, comparator);
        getRestaurant().setDefaultComparator(DataType.PROMO_PACKAGE, comparator);

        final FileIO f = new FileIO();
        final List<String[]> alaCarteData = f.read(DataType.ALA_CARTE_ITEM).stream().map(data -> data.split(" // ")).filter(data -> data.length == 4).collect(Collectors.toList());
        final List<String[]> promoPackageData = f.read(DataType.PROMO_PACKAGE).stream().map(data -> data.split(" // ")).filter(data -> data.length == 4).collect(Collectors.toList());

        for (String[] data : alaCarteData) {
            try {
                final int id = getRestaurant().generateUniqueId(DataType.ALA_CARTE_ITEM);
                final String name = data[1];
                final BigDecimal price = new BigDecimal(data[2]);
                final String category = data[3];
                getRestaurant().load(new AlaCarteItem(id, name, price, category));
            } catch (NumberFormatException e) {
                getCm().clearCmd();
                Log.warning("Invalid file data for detected for " + DataType.ALA_CARTE_ITEM.name() + ": " + e.getMessage());
            }
        }

        for (String[] data : promoPackageData) {
            final List<AlaCarteItem> itemList = new ArrayList<>();

            for (String itemData : data[3].split("--")) {
                final int itemId = Integer.parseInt(itemData);
                final Optional<AlaCarteItem> item = getRestaurant().getDataFromId(DataType.ALA_CARTE_ITEM, itemId);
                item.ifPresent(itemList::add);
            }

            try {
                final int id = getRestaurant().generateUniqueId(DataType.PROMO_PACKAGE);
                final String name = data[1];
                final BigDecimal price = new BigDecimal(data[2]);
                getRestaurant().load(new PromotionPackage(id, name, price, itemList));
            } catch (NumberFormatException e) {
                getCm().clearCmd();
                Log.warning("Invalid file data for detected for " + DataType.PROMO_PACKAGE.name() + ": " + e.getMessage());
            }
        }

        getRestaurant().bulkSave(DataType.ALA_CARTE_ITEM);
        getRestaurant().bulkSave(DataType.PROMO_PACKAGE);
    }

    public Set<String> getAlaCarteCategories() {
        final Set<String> ret = new TreeSet<>();
        final Optional<List<AlaCarteItem>> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        dataList.ifPresent(data -> data.forEach(x -> ret.add(x.getCategory())));
        return ret;
    }

    private List<String> getAlaCarteDisplayList(String category) {
        final Optional<List<AlaCarteItem>> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        return dataList.map(data -> data.stream().filter(x -> x.getCategory().equals(category)).sorted(Comparator.comparing(MenuItem::getName)).map(AlaCarteItem::toDisplayString).collect(Collectors.toList())).get();
    }

    private List<String> getPromoPackageDisplayList() {
        final Optional<List<PromotionPackage>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        return dataList.map(data -> data.stream().sorted(Comparator.comparing(MenuItem::getName)).map(PromotionPackage::toDisplayString).collect(Collectors.toList())).get();
    }

    private List<String> getItemNames(DataType dataType) {
        final Optional<List<MenuItem>> dataList = getRestaurant().getDataList(dataType);
        return dataList.map(data -> data.stream().sorted(Comparator.comparing(MenuItem::getName)).map(MenuItem::getName).collect(Collectors.toList())).get();
    }

    private boolean ifNameExists(DataType dataType, String name) {
        final Optional<List<MenuItem>> dataList = getRestaurant().getDataList(dataType);
        return dataList.map(data -> data.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name))).get();
    }

    private void refreshPromoPrices() {
        final Optional<List<PromotionPackage>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        dataList.ifPresent(data -> data.forEach(PromotionPackage::refreshPrice));
    }

    private boolean isItemInPackages(AlaCarteItem item) {
        final Optional<List<PromotionPackage>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        return dataList.map(data -> data.stream().anyMatch(x -> x.getAlaCarteItems().stream().anyMatch(y -> y.getId() == item.getId()))).get();
    }

    @Override
    public String[] getMainCLIOptions() {
        return new String[]{
                "View menu",
                "Add new ala-carte item",
                "Add new promotional package",
                "Manage ala-carte items",
                "Manage promotional packages",
        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[]{
                this::viewMenu,
                () -> addMenuItem(DataType.ALA_CARTE_ITEM),
                () -> addMenuItem(DataType.PROMO_PACKAGE),
                () -> manageMenuItems(DataType.ALA_CARTE_ITEM),
                () -> manageMenuItems(DataType.PROMO_PACKAGE)
        };
    }

    private void viewMenu() {
        final Set<String> categoryList = getAlaCarteCategories();
        List<String> displayList = new ArrayList<>();
        getCm().clearCmd();

        for (String category : categoryList) {
            displayList.add("\\SUB" + category);
            displayList.addAll(getAlaCarteDisplayList(category));
            displayList.add("---");
        }

        displayList.add("\\SUB" + "Promotion Packages");
        displayList.addAll(getPromoPackageDisplayList());
        getCm().printTable("View Menu Items", "", displayList, false);
        getCm().getInt("Enter 0 to go back", 0, 0);
        getCm().clearCmd();
    }

    private void addMenuItem(DataType dataType) {
        getCm().printInstructions(Collections.singletonList("Enter -back in item name to go back."));
        final String name = getCm().getString("Enter item name");

        if (name.equalsIgnoreCase("-back")) {
            return;
        }

        if (dataType.equals(DataType.ALA_CARTE_ITEM) && (addAlaCarteItem(name)) || dataType.equals(DataType.PROMO_PACKAGE) && (addPromoPackage(name))) {
            getCm().clearCmd();
            System.out.println("Item has been added successfully.");
        }
    }

    private boolean addAlaCarteItem(String name) {
        final BigDecimal price = new BigDecimal(getCm().getDouble("Enter item price"));
        final String category = getCm().getString("Enter item category");

        if (ifNameExists(DataType.ALA_CARTE_ITEM, name)) {
            getCm().clearCmd();
            Log.notice("Failed to add item as it already exists on the menu.");
            return false;
        }

        final int id = getRestaurant().generateUniqueId(DataType.ALA_CARTE_ITEM);
        AlaCarteItem item = new AlaCarteItem(id, name, price, category);
        return getRestaurant().save(item);
    }

    private boolean addPromoPackage(String name) {
        final List<String> nameList = getItemNames(DataType.ALA_CARTE_ITEM);
        final List<String> choiceList = getCm().formatChoiceList(nameList, Collections.singletonList("Go back"));
        getCm().printTable("Command // Ala-Carte Items", choiceList, true);

        final List<AlaCarteItem> itemList = new ArrayList<>();
        BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
        int itemIndex;
        String cont = "Y";

        do {
            if (cont.equalsIgnoreCase("Y")) {
                itemIndex = getCm().getInt("Select an item to add to the package", 0, nameList.size()) - 1;
                Optional<AlaCarteItem> item = getRestaurant().getDataFromIndex(DataType.ALA_CARTE_ITEM, itemIndex);
                if (item.isPresent()) {
                    itemList.add(item.get());
                    price = price.add(item.get().getPrice());
                    System.out.println(item.get().getName() + " added to package successfully.");
                } else {
                    return false;
                }
            }

            cont = getCm().getString("Add another item to package? [Y = YES | N = NO]");
        } while (!cont.equalsIgnoreCase("N"));

        if (ifNameExists(DataType.PROMO_PACKAGE, name)) {
            getCm().clearCmd();
            Log.notice("Failed to add item as it already exists on the menu.");
            return false;
        }

        final int id = getRestaurant().generateUniqueId(DataType.PROMO_PACKAGE);
        PromotionPackage item = new PromotionPackage(id, name, price.multiply(new BigDecimal(0.8)), itemList);
        return getRestaurant().save(item);
    }

    private void manageMenuItems(DataType dataType) {
        final List<String> itemList = getItemNames(dataType);
        List<String> choiceList = getCm().formatChoiceList(itemList, null);
        getCm().printTable("Manage Menu Items", "Command // Menu Item", choiceList, true);
        int itemIndex = getCm().getInt("Select an item to manage", 0, itemList.size()) - 1;

        if (itemIndex == -1) {
            return;
        }

        Optional<MenuItem> oItem = getRestaurant().getDataFromIndex(dataType, itemIndex);

        if (oItem.isEmpty()) {
            return;
        }

        final String[] actions = dataType.equals(DataType.ALA_CARTE_ITEM) ? new String[]{"Change name.", "Change price.", "Change category", "Remove item from menu."} : new String[]{"Change name.", "Remove package from menu."};
        choiceList = getCm().formatChoiceList(Arrays.asList(actions), null);
        getCm().printTable("Command // Action", choiceList, true);
        final int action = getCm().getInt("Select an action", 0, actions.length);

        if (action == 0) {
            return;
        }

        MenuItem item = oItem.get();

        if ((action == 2 && dataType.equals(DataType.PROMO_PACKAGE)) || (action == 4 && dataType.equals(DataType.ALA_CARTE_ITEM))) {
            getCm().printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

            if (getCm().getString("Confirm remove?").equalsIgnoreCase("Y") && (removeItem(item))) {
                getCm().clearCmd();
                System.out.println("Item has been removed successfully.");
            }
        } else {
            if (updateItem(item, action)) {
                getCm().clearCmd();
                System.out.println("Item has been updated successfully.");
            }
        }
    }

    private boolean updateItem(MenuItem item, int action) {
        switch (action) {
            case 1:
            case 3:
                getCm().printInstructions(Collections.singletonList("Enter -back to go back."));
                final String input = getCm().getString("Enter the new " + ((action == 1) ? "name" : "category"));

                if (!input.equalsIgnoreCase("-back")) {
                    if (action == 1) {
                        item.setName(input);
                    } else {
                        ((AlaCarteItem) item).setCategory(input);
                    }

                    if (getRestaurant().save(item)) {
                        refreshPromoPrices();
                        return true;
                    }
                }
                break;

            case 2:
                final BigDecimal price = new BigDecimal(getCm().getDouble("Enter the new price")).setScale(2, RoundingMode.FLOOR);
                item.setPrice(price);

                if (getRestaurant().save(item)) {
                    return true;
                }
                break;
        }

        return false;
    }

    private boolean removeItem(MenuItem item) {
        if (getRestaurant().getDataList(DataType.ORDER).map(List::size).get() > 0) {
            getCm().clearCmd();
            Log.notice("Failed to remove item as there are active orders. Clear all orders before removing items.");
            return false;
        }

        if (item instanceof AlaCarteItem) {
            if (isItemInPackages((AlaCarteItem) item)) {
                getCm().clearCmd();
                Log.notice("This item is part of a promotion package. Please remove the package first.");
                return false;
            }
        }

        return getRestaurant().remove(item);
    }
}
