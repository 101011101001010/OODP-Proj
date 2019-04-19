package revenue;

import core.Restaurant;
import core.RestaurantManager;
import enums.DataType;
import tools.ConsolePrinter;
import tools.FileIO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Displays revenue information of the restaurant by periods.
 */
public class RevenueManager extends RestaurantManager {
    /**
     * Period enumerator for the revenue display periods.
     */
    private enum Period {
        LIFETIME,
        ANNUALLY,
        MONTHLY,
        DAILY
    }

    /**
     * List of all revenue information read from its text file.
     */
    private List<String[]> revenueList;

    /**
     * Initialises the manager with a restaurant object for data storage and manipulation.
     * Also reads in data from the revenue text file for local storage.
     * @param restaurant Restaurant instance from main
     * @throws Exception Errors that occurred while reading in revenue data from text file.
     */
    public RevenueManager(Restaurant restaurant) throws Exception {
        super(restaurant);
        final FileIO f = new FileIO();
        revenueList = f.read(DataType.REVENUE).stream().map(data -> data.split(" // ")).filter(data -> data.length == 2).collect(Collectors.toList());
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public String[] getMainCLIOptions() {
        List<String> tempList = new ArrayList<>();

        for (Period period : Period.values()) {
            tempList.add("Show revenue (" + period.name().toLowerCase() + ")");
        }

        return tempList.toArray(new String[0]);
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public Runnable[] getOptionRunnables() {
        List<Runnable> tempList = new ArrayList<>();

        for (Period period : Period.values()) {
            tempList.add(() -> print(period));
        }

        return tempList.toArray(new Runnable[0]);
    }

    /**
     * Prints revenue information by period.
     * @param period Revenue period to print based on the enumerator.
     */
    private void print(Period period) {
        try {
            LocalDate compareDate = LocalDate.now();
            Set<String> keySet = new TreeSet<>();
            Map<String, Integer> countMap = new HashMap<>();
            Map<String, BigDecimal> priceMap = new HashMap<>();

            for (String[] datas : revenueList) {
                String orderId = datas[0];
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
                LocalDate orderDate = LocalDate.parse(orderId, format);

                if (period.equals(Period.DAILY) && !orderDate.equals(compareDate)) {
                    continue;
                } else if (period.equals(Period.MONTHLY) && (!orderDate.getMonth().equals(compareDate.getMonth()) || orderDate.getYear() != compareDate.getYear())) {
                    continue;
                } else if (period.equals(Period.ANNUALLY) && orderDate.getYear() != compareDate.getYear()) {
                    continue;
                }

                for (String items : datas[1].split("--")) {
                    String[] itemDetails = items.split(" - ");
                    String itemName = itemDetails[0];
                    int itemCount = Integer.parseInt(itemDetails[1]);
                    BigDecimal itemPrice = new BigDecimal(itemDetails[2]);

                    if (countMap.containsKey(itemName)) {
                        itemCount += countMap.get(itemName);
                    }

                    if (priceMap.containsKey(itemName)) {
                        itemPrice = priceMap.get(itemName).add(itemPrice);
                    }

                    keySet.add(itemName);
                    countMap.put(itemName, itemCount);
                    priceMap.put(itemName, itemPrice);
                }
            }

            if (keySet.size() == 0) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "No revenue information found for this period.");
                return;
            }

            List<String> displayList = new ArrayList<>();

            for (String key : keySet) {
                displayList.add(key + " // " + countMap.get(key) + " // " + priceMap.get(key));
            }

            final String title = period + " Revenue Report";
            ConsolePrinter.clearCmd();
            ConsolePrinter.printTable(title, "Item // Amount // Total Price", displayList, true);
            getInputHelper().getInt("Enter 0 to go back", 0, 0);
        } catch (RuntimeException e) {
            ConsolePrinter.logToFile(e.getMessage(), e);
        }
    }
}