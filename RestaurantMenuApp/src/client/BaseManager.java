package client;

import enums.DataType;
import tools.ConsoleHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class BaseManager {
    private Restaurant restaurant;
    private ConsoleHelper cs;

    public BaseManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.cs = new ConsoleHelper();
    }

    protected Restaurant getRestaurant() {
        return restaurant;
    }

    protected ConsoleHelper getCs() {
        return cs;
    }

    protected List<String> getDisplayData(DataType dataType) {
        List<? extends RestaurantData> masterList = new ArrayList<>(getRestaurant().getData(dataType));
        List<String> ret = new ArrayList<>();

        if (masterList.size() == 0) {
            ret.add("Database is empty for " + dataType + ".");
            return ret;
        }

        masterList.sort(Comparator.comparingInt(RestaurantData::getId));
        for (RestaurantData o : masterList) {
            ret.add(o.toTableString());
        } return ret;
    }

    public abstract void init() throws ManagerInitFailedException;
    public abstract String[] getMainCLIOptions();
    public abstract Runnable[] getOptionRunnables();

    public class ManagerInitFailedException extends Exception {
        public ManagerInitFailedException(Object o, String rootMessage) {
            super(o.getClass().getCanonicalName() + " initialisation failed: " + rootMessage);
        }
    }
}