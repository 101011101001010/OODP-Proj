package menu;

import core.RestaurantManager;
import core.Restaurant;
import enums.DataType;
import tools.ConsolePrinter;
import tools.FileIO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class MenuManager extends RestaurantManager {

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
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data for detected for " + DataType.ALA_CARTE_ITEM.name() + ": " + e.getMessage());
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
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data for detected for " + DataType.PROMO_PACKAGE.name() + ": " + e.getMessage());
            }
        }

        getRestaurant().bulkSave(DataType.ALA_CARTE_ITEM);
        getRestaurant().bulkSave(DataType.PROMO_PACKAGE);
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
        final List<String> displayList = new ArrayList<>();

        for (String category : categoryList) {
            displayList.add("\\SUB" + category);
            final Optional<List<AlaCarteItem>> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
            final List<String> tempList = dataList.map(data -> data.stream().filter(x -> x.getCategory().equals(category)).map(AlaCarteItem::toDisplayString).collect(Collectors.toList())).get();
            displayList.addAll(tempList);
        }

        displayList.add("\\SUB" + "Promotion Packages");
        final Optional<List<PromotionPackage>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        final List<String> tempList = dataList.map(data -> data.stream().map(PromotionPackage::toDisplayString).collect(Collectors.toList())).get();
        displayList.addAll(tempList);

        ConsolePrinter.clearCmd();
        ConsolePrinter.printTable("View Menu Items", "", displayList, false);
        getInputHelper().getInt("Enter 0 to go back", 0, 0);
        ConsolePrinter.clearCmd();
    }

    private void addMenuItem(DataType dataType) {
        ConsolePrinter.printInstructions(Collections.singletonList("Enter -back in item name to go back."));
        final String name = getInputHelper().getString("Enter item name");

        if (name.equalsIgnoreCase("-back")) {
            ConsolePrinter.clearCmd();
            return;
        }

        if (dataType.equals(DataType.ALA_CARTE_ITEM) && (addAlaCarteItem(name)) || dataType.equals(DataType.PROMO_PACKAGE) && (addPromoPackage(name))) {
            ConsolePrinter.clearCmd();
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Item has been added successfully.");
        }
    }

    private boolean addAlaCarteItem(String name) {
        final BigDecimal price = new BigDecimal(getInputHelper().getDouble("Enter item price"));
        final String category = getInputHelper().getString("Enter item category");
        final Optional<List<MenuItem>> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        boolean isNameExists = dataList.map(data -> data.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name))).get();

        if (isNameExists) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to add item as it already exists on the menu.");
            return false;
        }

        final int id = getRestaurant().generateUniqueId(DataType.ALA_CARTE_ITEM);
        AlaCarteItem item = new AlaCarteItem(id, name, price, category);
        return getRestaurant().save(item);
    }

    private boolean addPromoPackage(String name) {
        final List<String> nameList = getItemNames(DataType.ALA_CARTE_ITEM);
        final List<String> choiceList = ConsolePrinter.formatChoiceList(nameList, Collections.singletonList("Go back"));
        ConsolePrinter.printTable("Command // Ala-Carte Items", choiceList, true);

        final List<AlaCarteItem> itemList = new ArrayList<>();
        BigDecimal price = new BigDecimal(0).setScale(2, RoundingMode.FLOOR);
        int itemIndex;
        String cont = "Y";

        do {
            if (cont.equalsIgnoreCase("Y")) {
                itemIndex = getInputHelper().getInt("Select an item to add to the package", 0, nameList.size()) - 1;
                Optional<AlaCarteItem> item = getRestaurant().getDataFromIndex(DataType.ALA_CARTE_ITEM, itemIndex);
                if (item.isPresent()) {
                    itemList.add(item.get());
                    price = price.add(item.get().getPrice());
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, item.get().getName() + " added to package successfully.");
                } else {
                    return false;
                }
            }

            cont = getInputHelper().getString("Add another item to package? [Y = YES | N = NO]");
        } while (!cont.equalsIgnoreCase("N"));

        final Optional<List<MenuItem>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
        boolean isNameExists = dataList.map(data -> data.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name))).get();

        if (isNameExists) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to add item as it already exists on the menu.");
            return false;
        }

        final int id = getRestaurant().generateUniqueId(DataType.PROMO_PACKAGE);
        PromotionPackage item = new PromotionPackage(id, name, price.multiply(new BigDecimal(0.8)), itemList);
        return getRestaurant().save(item);
    }

    private void manageMenuItems(DataType dataType) {
        final List<String> itemList = getItemNames(dataType);
        List<String> choiceList = ConsolePrinter.formatChoiceList(itemList, null);
        ConsolePrinter.printTable("Manage Menu Items", "Command // Menu Item", choiceList, true);
        int itemIndex = getInputHelper().getInt("Select an item to manage", 0, itemList.size()) - 1;

        if (itemIndex == -1) {
            ConsolePrinter.clearCmd();
            return;
        }

        Optional<MenuItem> oItem = getRestaurant().getDataFromIndex(dataType, itemIndex);

        if (oItem.isEmpty()) {
            return;
        }

        final String[] actions = dataType.equals(DataType.ALA_CARTE_ITEM) ? new String[]{"Change name.", "Change price.", "Change category", "Remove item from menu."} : new String[]{"Change name.", "Remove package from menu."};
        choiceList = ConsolePrinter.formatChoiceList(Arrays.asList(actions), null);
        ConsolePrinter.printTable("Command // Action", choiceList, true);
        final int action = getInputHelper().getInt("Select an action", 0, actions.length);

        if (action == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        MenuItem item = oItem.get();

        if ((action == 2 && dataType.equals(DataType.PROMO_PACKAGE)) || (action == 4 && dataType.equals(DataType.ALA_CARTE_ITEM))) {
            ConsolePrinter.printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

            if (getInputHelper().getString("Confirm remove?").equalsIgnoreCase("Y") && (removeItem(item))) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been removed successfully.");
            }
        } else {
            if (updateItem(item, action)) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Item has been updated successfully.");
            }
        }
    }

    private boolean updateItem(MenuItem item, int action) {
        switch (action) {
            case 1:
            case 3:
                ConsolePrinter.printInstructions(Collections.singletonList("Enter -back to go back."));
                final String input = getInputHelper().getString("Enter the new " + ((action == 1) ? "name" : "category"));

                if (!input.equalsIgnoreCase("-back")) {
                    if (action == 1) {
                        item.setName(input);
                    } else {
                        ((AlaCarteItem) item).setCategory(input);
                    }

                    if (getRestaurant().save(item)) {
                        final Optional<List<PromotionPackage>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
                        dataList.ifPresent(data -> data.forEach(PromotionPackage::refreshPrice));
                        return true;
                    }
                } else {
                    ConsolePrinter.clearCmd();
                    return false;
                }
                break;

            case 2:
                final BigDecimal price = new BigDecimal(getInputHelper().getDouble("Enter the new price")).setScale(2, RoundingMode.FLOOR);
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
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to remove item as there are active orders. Clear all orders before removing items.");
            return false;
        }

        if (item instanceof AlaCarteItem) {
            final Optional<List<PromotionPackage>> dataList = getRestaurant().getDataList(DataType.PROMO_PACKAGE);
            boolean isItemInPackages = dataList.map(data -> data.stream().anyMatch(x -> x.getAlaCarteItems().stream().anyMatch(y -> y.getId() == item.getId()))).get();

            if (isItemInPackages) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "This item is part of a promotion package. Please remove the package first.");
                return false;
            }
        }

        return getRestaurant().remove(item);
    }

    public Set<String> getAlaCarteCategories() {
        final Set<String> ret = new TreeSet<>();
        final Optional<List<AlaCarteItem>> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        dataList.ifPresent(data -> data.forEach(x -> ret.add(x.getCategory())));
        return ret;
    }

    public List<String> getAlaCarteItemNamesForCategory(String category) {
        final Optional<List<AlaCarteItem>> dataList = getRestaurant().getDataList(DataType.ALA_CARTE_ITEM);
        return dataList.map(data -> data.stream().filter(item -> item.getCategory().equals(category)).map(MenuItem::getName).collect(Collectors.toList())).get();
    }

    public List<String> getItemNames(DataType dataType) {
        final Optional<List<MenuItem>> dataList = getRestaurant().getDataList(dataType);
        return dataList.map(data -> data.stream().map(MenuItem::getName).collect(Collectors.toList())).get();
    }
}
