package menu;

import client.DataManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import tools.FileIO;

import javax.xml.transform.Result;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MenuManager extends DataManager {

    public MenuManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws IOException {
        getRestaurant().registerClass(AlaCarteItem.class, DataType.ALA_CARTE_ITEM);
        getRestaurant().registerClass(PromotionPackage.class, DataType.PROMO_PACKAGE);
        getRestaurant().setCounter(DataType.PROMO_PACKAGE, 99999);

        final FileIO f = new FileIO();
        final List<String[]> alaCarteData = f.read(DataType.ALA_CARTE_ITEM).stream().map(data -> data.split(" // ")).filter(data -> data.length == 4).collect(Collectors.toList());
        final List<String[]> promoPackageData = f.read(DataType.PROMO_PACKAGE).stream().map(data -> data.split(" // ")).filter(data -> data.length == 4).collect(Collectors.toList());

        for (String[] data : alaCarteData) {
            try {
                final String name = data[1];
                final BigDecimal price = new BigDecimal(data[2]);
                final String category = data[3];
                getRestaurant().load(new AlaCarteItem(getRestaurant().incrementAndGetCounter(DataType.ALA_CARTE_ITEM), name, price, category));
            } catch (NumberFormatException e) {
                Logger.getGlobal().logp(Level.WARNING, "", "", "Invalid file data: " + e.getMessage());
            }
        }

        for (String[] data : promoPackageData) {
            final List<AlaCarteItem> alaCarteItems = new ArrayList<>();

            for (String itemData : data[3].split("--")) {
                final int itemId = Integer.parseInt(itemData);
                final MenuItem item = (MenuItem) getRestaurant().getDataFromId(DataType.ALA_CARTE_ITEM, itemId);

                if (item instanceof AlaCarteItem) {
                    alaCarteItems.add((AlaCarteItem) item);
                }
            }

            final String name = data[1];
            final BigDecimal price = new BigDecimal(data[2]);
            getRestaurant().load(new PromotionPackage(getRestaurant().incrementAndGetCounter(DataType.PROMO_PACKAGE), name, price, alaCarteItems));
        }

        getRestaurant().bulkSave(DataType.ALA_CARTE_ITEM);
        getRestaurant().bulkSave(DataType.PROMO_PACKAGE);
    }

    private Set<String> getAlaCarteCategories() {
        final Set<String> ret = new TreeSet<>();
        getRestaurant().getData(DataType.ALA_CARTE_ITEM).stream().filter(data -> data instanceof AlaCarteItem).forEach(data -> ret.add(((AlaCarteItem) data).getCategory()));
        return ret;
    }

    private List<String> getAlaCarteDisplayList(String category) {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.ALA_CARTE_ITEM);
        final List<AlaCarteItem> alaCarteItemList = new ArrayList<>();
        dataList.stream().filter(data -> data instanceof AlaCarteItem).filter(data -> ((AlaCarteItem) data).getCategory().equals(category)).forEach(data -> alaCarteItemList.add(((AlaCarteItem) data)));
        alaCarteItemList.sort(Comparator.comparing(MenuItem::getName));
        return alaCarteItemList.stream().map(AlaCarteItem::toDisplayString).collect(Collectors.toList());
    }

    private List<String> getPromoPackageDisplayList() {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.PROMO_PACKAGE);
        final List<PromotionPackage> promoPackageList = new ArrayList<>();
        dataList.stream().filter(data -> data instanceof PromotionPackage).forEach(data -> promoPackageList.add(((PromotionPackage) data)));
        promoPackageList.sort(Comparator.comparing(MenuItem::getName));
        return promoPackageList.stream().map(PromotionPackage::toDisplayString).collect(Collectors.toList());
    }

    private List<String> getItemNames(DataType dataType) {
        final List<String> ret = new ArrayList<>();

        if (dataType.equals(DataType.ALA_CARTE_ITEM)) {
            getRestaurant().getData(dataType).stream().filter(data -> data instanceof AlaCarteItem).forEach(data -> ret.add(((AlaCarteItem) data).getName()));
        } else {
            getRestaurant().getData(dataType).stream().filter(data -> data instanceof PromotionPackage).forEach(data -> ret.add(((PromotionPackage) data).getName()));
        }

        return ret;
    }

    private boolean ifNameExists(DataType dataType, String name) {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(dataType);
        long count = dataList.stream().filter(data -> data instanceof MenuItem).filter(item -> ((MenuItem) item).getName().equals(name)).count();
        return (count > 0);
    }

    private boolean addNewItem(String name, BigDecimal price, String category) {
        if (ifNameExists(DataType.ALA_CARTE_ITEM, name)) {
            Logger.getGlobal().logp(Level.INFO, "", "", "Failed to add item. Item already exists.");
            return false;
        }

        return addNewItem(new AlaCarteItem(getRestaurant().incrementAndGetCounter(DataType.ALA_CARTE_ITEM), name, price, category));
    }

    private boolean addNewItem(String name, List<Integer> alaCarteItemIndices) {
        if (ifNameExists(DataType.ALA_CARTE_ITEM, name)) {
            Logger.getGlobal().logp(Level.INFO, "", "", "Failed to add item. Item already exists.");
            return false;
        }

        final List<AlaCarteItem> alaCarteItemList = new ArrayList<>();
        BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
        alaCarteItemIndices.stream().map(index -> getRestaurant().getDataFromIndex(DataType.ALA_CARTE_ITEM, index)).filter(item -> item instanceof AlaCarteItem).forEach(item -> alaCarteItemList.add((AlaCarteItem) item));
        for (AlaCarteItem item : alaCarteItemList) {
            price = price.add(item.getPrice());
        }

        return addNewItem(new PromotionPackage(getRestaurant().incrementAndGetCounter(DataType.PROMO_PACKAGE), name, price.multiply(new BigDecimal(0.8)), alaCarteItemList));
    }

    private boolean addNewItem(MenuItem item) {
        return getRestaurant().save(item);
    }

    private boolean updateItem(int index, String name, BigDecimal price, String category) {
        final AlaCarteItem item = (AlaCarteItem) getRestaurant().getData(DataType.ALA_CARTE_ITEM).get(index);
        name = (name.isBlank() ? item.getName() : name);
        price = ((price == null) ? item.getPrice() : price);
        category = (category.isBlank() ? item.getCategory() : category);

        final AlaCarteItem tempItem = new AlaCarteItem(item.getId(), name, price, category);
        return updateItem(item, tempItem);
    }

    private boolean updateItem(int index, String name) {
        final PromotionPackage item = (PromotionPackage) getRestaurant().getData(DataType.PROMO_PACKAGE).get(index);
        name = (name.isBlank() ? item.getName() : name);

        final PromotionPackage tempItem = new PromotionPackage(item.getId(), name, item.getPrice(), item.getAlaCarteItems());
        return updateItem(item, tempItem);
    }

    private boolean updateItem(MenuItem item, MenuItem tempItem) {
        boolean result = getRestaurant().update(tempItem);

        if (result) {
            if (item instanceof AlaCarteItem) {
                ((AlaCarteItem) item).update(tempItem.getName(), tempItem.getPrice(), ((AlaCarteItem) tempItem).getCategory());
                final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.PROMO_PACKAGE);
                dataList.stream().filter(data -> data instanceof PromotionPackage).forEach(promo -> ((PromotionPackage) promo).refreshPrice());
            } else {
                ((PromotionPackage) item).update(tempItem.getName());
            }
        }

        return result;
    }

    private boolean removeItem(DataType dataType, int index) {
        final MenuItem item = (MenuItem) getRestaurant().getData(dataType).get(index);

        if (getRestaurant().getData(DataType.ORDER).size() > 0) {
            Logger.getGlobal().logp(Level.INFO, "", "", "Failed to remove item as there are active orders. Clear all orders before removing items.");
            return false;
        }

        if (dataType.equals(DataType.ALA_CARTE_ITEM)) {
            final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.PROMO_PACKAGE);
            long itemInPackageCount = dataList.stream().filter(data -> data instanceof PromotionPackage).map(promo -> ((PromotionPackage) promo).getAlaCarteItems().stream().filter(alaCarteItem -> alaCarteItem.getId() == item.getId()).count()).count();

            if (itemInPackageCount > 0) {
                Logger.getGlobal().logp(Level.INFO, "", "", "This item is part of a promotion package. Please remove the package first.");
                return false;
            }
        }

        return getRestaurant().remove(item);
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

        if (categoryList.size() == 0) {
            displayList.add("There is no item on the menu.");
            getCs().printTable("Ala-Carte Items", "", displayList, true);
        } else {
            for (String category : categoryList) {
                displayList = getAlaCarteDisplayList(category);
                int itemCount = displayList.size();
                int startIndex = (int) Math.ceil(1.0 * displayList.size() / 2);

                for (int index = startIndex; index < displayList.size(); index++) {
                    displayList.set(index - startIndex, displayList.get(index - startIndex) + " // " + displayList.get(index));
                }

                for (int index = displayList.size() - 1; index >= startIndex; index--) {
                    displayList.remove(index);
                }

                final String headers = (itemCount > 1)? "Item Details // Price // Item Details // Price" : "Item Details // Price";
                getCs().printTable(category, "", displayList, false);
            }
        }

        displayList = getPromoPackageDisplayList();
        getCs().printTable("Promotion Packages", "", displayList, false);
        getCs().getInt("Enter 0 to go back", 0, 0);
    }

    private void addMenuItem(DataType dataType) {
        getCs().printInstructions(Collections.singletonList("Enter -back in item name to go back."));
        final String name = getCs().getString("Enter item name");

        if (name.equalsIgnoreCase("-back")) {
            return;
        }

        if (dataType.equals(DataType.ALA_CARTE_ITEM)) {
            final BigDecimal price = new BigDecimal(getCs().getDouble("Enter item price"));
            final String category = getCs().getString("Enter item category");

            if (addNewItem(name, price, category)) {
                System.out.println("Item has been added successfully.");
            }
        } else {
            final List<String> itemList = getItemNames(DataType.ALA_CARTE_ITEM);
            final List<String> choiceList = getCs().formatChoiceList(itemList, Collections.singletonList("Go back"));
            getCs().printTable("", "Command // Ala-Carte Items", choiceList, true);
            int itemIndex = getCs().getInt("Select an item to add to the package", 0, itemList.size());

            if (itemIndex == 0) {
                return;
            }

            itemIndex--;
            final List<Integer> alaCarteItemIndices = new ArrayList<>();
            alaCarteItemIndices.add(itemIndex);
            String cont;
            while (!(cont = getCs().getString("Add another item to package? [Y = YES | N = NO]")).equalsIgnoreCase("N")) {
                if (cont.equalsIgnoreCase("Y")) {
                    itemIndex = getCs().getInt("Enter choice", 0, itemList.size());

                    if (itemIndex == 0) {
                        return;
                    }

                    itemIndex--;
                    alaCarteItemIndices.add(itemIndex);
                }
            }

            if (addNewItem(name, alaCarteItemIndices)) {
                System.out.println("Item has been added successfully.");
            }
        }
    }

    private void manageMenuItems(DataType dataType) {
        final String title = "Manage Menu Items";
        final List<String> itemList = getItemNames(dataType);
        final List<String> footerChoices = Collections.singletonList("Go back");
        final String headers = "Command // " + (dataType.equals(DataType.ALA_CARTE_ITEM)? "Ala-Carte Items" : "Promotional Packages");
        List<String> choiceList = getCs().formatChoiceList(itemList, footerChoices);
        getCs().printTable(title, headers, choiceList, true);
        int itemIndex = getCs().getInt("Select an item to manage", (1 - footerChoices.size()), itemList.size());

        if (itemIndex == 0) {
            return;
        }

        itemIndex--;
        final String[] actions = dataType.equals(DataType.ALA_CARTE_ITEM) ?
                new String[]{"Change name.", "Change price.", "Change category", "Remove item from menu."} :
                new String[]{"Change name.", "Remove package from menu."};
        choiceList = getCs().formatChoiceList(Arrays.asList(actions), footerChoices);
        getCs().printTable(title, "Command // Action", choiceList, true);
        final int action = getCs().getInt("Select an action", (1 - footerChoices.size()), actions.length);

        if (action == 0) {
            return;
        }

        if ((action == 1) || (action == 3 && dataType.equals(DataType.ALA_CARTE_ITEM))) {
            final String what = (action == 1) ? "name" : "category";
            getCs().printInstructions(Collections.singletonList("Enter -back to go back."));
            final String input = getCs().getString("Enter the new " + what);

            if (input.equalsIgnoreCase("-back")) {
                return;
            }

            if (dataType.equals(DataType.ALA_CARTE_ITEM) && updateItem(itemIndex, (action == 1) ? input : "", null, (action == 3) ? input : "")) {
                System.out.println("Item has been updated successfully.");
            } else if (dataType.equals(DataType.PROMO_PACKAGE) && updateItem(itemIndex, input)) {
                System.out.println("Item has been updated successfully.");
            }
        }

        if (action == 2 && dataType.equals(DataType.ALA_CARTE_ITEM)) {
            final BigDecimal price = new BigDecimal(getCs().getDouble("Enter the new price"));

            if (updateItem(itemIndex, "", price, "")) {
                System.out.println("Item has been updated successfully.");
            }
        }

        if ((action == 2 && dataType.equals(DataType.PROMO_PACKAGE)) || (action == 4 && dataType.equals(DataType.ALA_CARTE_ITEM))) {
            getCs().printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));
            if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
                if (removeItem(dataType, itemIndex)) {
                    System.out.println("Item has been removed successfully.");
                }
            } else {
                System.out.println("Remove operation aborted.");
            }
        }
    }
}
