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
import java.util.stream.Collectors;


public class RevenueReader extends RestaurantManager {
    private enum Period {
        LIFETIME,
        ANNUALLY,
        MONTHLY,
        DAILY
    }

    private List<String> stringList;
    private List<LocalDateTime> dateList;
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    private Map<String, Integer>  totalCount;
    private Map<String, BigDecimal> totalPrice;

    public RevenueReader(Restaurant restaurant) {
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
            System.out.println("No method found." + e.getMessage());
        }
    }

    private void printDaily() {
        List<String> todayList = new ArrayList<>();
        this.totalCount = new HashMap<>();
        this.totalPrice = new HashMap<>();


        for ( String s1 : stringList) {
            String[] split = s1.split(" // ");
            String orderId = split[0];
            String items = split[1];
            String[] itemArray = items.split("--");

            for (String s2 : itemArray) {
                String[] itemDetails = s2.split(" - ");
                String orderName = itemDetails[0];
                int count = Integer.parseInt(itemDetails[1]);
                BigDecimal price = new BigDecimal(itemDetails[2]);
            }

            // ordername - count - price--ordername - count - price
            if (LocalDateTime.parse(str.split(" // ")[0]).toLocalDate().equals(LocalDateTime.now())){

            }
        }
        /*
        stringList.stream().filter(str -> LocalDateTime.parse(str.split(" // ")[0], format).toLocalDate().equals(LocalDateTime.now().toLocalDate())).forEach(todayList::add);
        todayList = todayList.stream().map(str -> str.replace("--", "\n")).collect(Collectors.toList());
        ConsolePrinter.printTable("Today's Revenue", "Order ID // Staff ID // Total // Individual Items", todayList, true);
        getInputHelper().getInt("", 0, 0);*/
    }



    public void loadData() {
        /*
        List<String> fileData = FileIO.read(AppConstants.FILE_NAMES[5].toLowerCase());
        String[] lineData;
        if (fileData != null) {
            revenueList = new ArrayList<>();
            itemTypes = new HashSet<>();
            cId = new AtomicInteger(-1);

            for (String line : fileData) {
                lineData = line.split(AppConstants.FILE_SEPARATOR);

                if (lineData.length == 4) {
                    RevenueReader item = new RevenueReader(Integer.parseInt(lineData[0]), Integer.parseInt(lineData[1]), Integer.parseInt(lineData[2]), Float.parseFloat(lineData[3]));
                    revenueList.add(item);
                }
            }
        }
        */
    }

    /*
    public void printAll() {
        for (int i = 0; i < revenueList.size(); i++) {
            System.out.println(revenueList.get(i).toString());
        }
    }

    public void itemTotal() {
        int temp = -1;
        ArrayList<RevenueReader> totalList = new ArrayList<RevenueReader>();

        for (int i = 0; i < revenueList.size(); i++) {
            temp = searchList(revenueList.get(i).itemID, totalList);
            if (temp == -1) {
                totalList.add(revenueList.get(i));
            } else {
                totalList.get(temp).itemCount += revenueList.get(i).itemCount;
                totalList.get(temp).totalPrice += revenueList.get(i).totalPrice;
            }
        }
        print(totalList);
    }

    public int searchList(int id, ArrayList<RevenueReader> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).itemID == id) {
                return i;
            }
        }
        return -1;
    }

    public void print(ArrayList<RevenueReader> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).toString());
        }
    }
    */
}


