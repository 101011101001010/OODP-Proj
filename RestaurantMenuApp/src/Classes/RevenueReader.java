package Classes;

import core.Restaurant;
import core.RestaurantManager;
import enums.DataType;
import tools.ConsolePrinter;
import tools.FileIO;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class RevenueReader extends RestaurantManager {
    private enum Period {
        LIFETIME,
        ANNUALLY,
        MONTHLY,
        DAILY
    }

    private List<String> stringList;
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private Map<String, Integer>  totalCount;
    private Map<String, BigDecimal> totalPrice;

    public RevenueReader(Restaurant restaurant) throws Exception {
        super(restaurant);
        final FileIO f = new FileIO();
        stringList = f.read(DataType.REVENUE);
        stringList.sort(Collections.reverseOrder());
    }

    @Override
    public void init() {}

    @Override
    public String[] getMainCLIOptions() {
        List<String> tempList = new ArrayList<>();

        for (Period period : Period.values()) {
            tempList.add("Show revenue (" + period.name().toLowerCase() + ")");
        }

        return tempList.toArray(new String[0]);
    }

    @Override
    public Runnable[] getOptionRunnables() {
        List<Runnable> tempList = new ArrayList<>();

        for (Period period : Period.values()) {
            tempList.add(() -> print(period));
        }

        return tempList.toArray(new Runnable[0]);
    }

    private void print(Period period) {
        String suffix = period.name().substring(0, 1).toUpperCase() + period.name().substring(1).toLowerCase();
        try {
            Method method = this.getClass().getDeclaredMethod("print" + suffix);
            method.invoke(this);
        } catch (Exception e) {
            ConsolePrinter.logToFile("Method error", e);
            //System.out.println("No method found." + e.getMessage());
        }
    }

    private void printDaily() {
        List<String> todayList = new ArrayList<>();
        LocalDateTime checkdate;
        this.totalCount = new HashMap<>();
        this.totalPrice = new HashMap<>();

        for ( String s1 : stringList) {
            String[] split = s1.split(" // ");
            String orderId = split[0];
            checkdate = LocalDateTime.parse(orderId, format);
            if(checkdate.toLocalDate().equals(LocalDateTime.now().toLocalDate())){
                String items = split[1];
                String[] itemArray = items.split("--");

                for (String s2 : itemArray) {
                    String[] itemDetails = s2.split(" - ");
                    String orderName = itemDetails[0];
                    int count = Integer.parseInt(itemDetails[1]);
                    BigDecimal price = new BigDecimal(itemDetails[2]);
                    addList(todayList, orderName);
                    increaseCount(totalCount, orderName, count);
                    addPrice(totalPrice, orderName, price);
                }
            }
        }
        printRevenue(totalCount, totalPrice, todayList, Period.DAILY);
    }

    private void printMonthly() {
        List<String> monthlyList = new ArrayList<>();
        LocalDateTime checkdate;
        this.totalCount = new HashMap<>();
        this.totalPrice = new HashMap<>();

        for ( String s1 : stringList) {
            String[] split = s1.split(" // ");
            String orderId = split[0];
            checkdate = LocalDateTime.parse(orderId, format);
            if(checkdate.getMonth().equals(LocalDateTime.now().getMonth()) &&
                    checkdate.getYear() == LocalDateTime.now().getYear()){
                String items = split[1];
                String[] itemArray = items.split("--");

                for (String s2 : itemArray) {
                    String[] itemDetails = s2.split(" - ");
                    String orderName = itemDetails[0];
                    int count = Integer.parseInt(itemDetails[1]);
                    BigDecimal price = new BigDecimal(itemDetails[2]);
                    addList(monthlyList, orderName);
                    increaseCount(totalCount, orderName, count);
                    addPrice(totalPrice, orderName, price);
                }
            }
        }
        printRevenue(totalCount, totalPrice, monthlyList, Period.MONTHLY);
    }

    private void printAnnually() {
        List<String> annuallyList = new ArrayList<>();
        LocalDateTime checkdate;
        this.totalCount = new HashMap<>();
        this.totalPrice = new HashMap<>();

        for ( String s1 : stringList) {
            String[] split = s1.split(" // ");
            String orderId = split[0];
            checkdate = LocalDateTime.parse(orderId, format);
            if(checkdate.getYear() == LocalDateTime.now().getYear()){
                String items = split[1];
                String[] itemArray = items.split("--");

                for (String s2 : itemArray) {
                    String[] itemDetails = s2.split(" - ");
                    String orderName = itemDetails[0];
                    int count = Integer.parseInt(itemDetails[1]);
                    BigDecimal price = new BigDecimal(itemDetails[2]);
                    addList(annuallyList, orderName);
                    increaseCount(totalCount, orderName, count);
                    addPrice(totalPrice, orderName, price);
                }
            }
        }
        printRevenue(totalCount, totalPrice, annuallyList, Period.ANNUALLY);
    }

    private void printLifetime() {
        List<String> allList = new ArrayList<>();
        this.totalCount = new HashMap<>();
        this.totalPrice = new HashMap<>();

        for ( String s1 : stringList) {
            String[] split = s1.split(" // ");
            String items = split[1];
            String[] itemArray = items.split("--");

            for (String s2 : itemArray) {
                String[] itemDetails = s2.split(" - ");
                String orderName = itemDetails[0];
                int count = Integer.parseInt(itemDetails[1]);
                BigDecimal price = new BigDecimal(itemDetails[2]);
                addList(allList, orderName);
                increaseCount(totalCount, orderName, count);
                addPrice(totalPrice, orderName, price);
            }
        }
        printRevenue(totalCount, totalPrice, allList, Period.LIFETIME);
    }

    private void addList (List<String> list, String itemName){
        for (String l :list){
            if (l.equals(itemName))
                return;
        }
        list.add(itemName);
    }
    private void addPrice (Map<String, BigDecimal> map, String key, BigDecimal price){
        BigDecimal itemPrice = map.get(key);
        if (itemPrice == null){
            map.put(key, price);
        }
    }
    private void increaseCount(Map<String, Integer> map, String key, int count){

        Integer value = map.get(key);
        if (value == null){
            map.put(key, count);
        }
        else{
            map.put(key, value + count);
        }
    }
    private void printRevenue(Map<String, Integer> totalCount, Map<String, BigDecimal> totalPrice, List<String> revenueList, Period period){
        List<String> displayList = new ArrayList<>();

        for ( String l : revenueList){
            displayList.add(l + " // " + totalCount.get(l) + " // " + totalPrice.get(l));
        }

        final String title = period + " Revenue Report";

        ConsolePrinter.clearCmd();
        ConsolePrinter.printTable(title, "Item // Amount // Total Price", displayList, true);
        getInputHelper().getInt("Enter 0 to go back", 0, 0);
    }
}