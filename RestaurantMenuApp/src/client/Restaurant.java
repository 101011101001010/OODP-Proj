package client;

import Classes.TableManager;
import client.enums.AssetType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Restaurant {
    private HashMap<AssetType, List<? extends RestaurantAsset>> listMap;
    private HashMap<AssetType, Integer> counterMap;
    private HashMap<Class<?>, AssetType> classMap;
    private TableManager tableManager;

    public Restaurant() {
        listMap = new HashMap<>();
        counterMap = new HashMap<>();
        classMap = new HashMap<>();

        for (AssetType type : AssetType.values()) {
            counterMap.put(type, type.equals(AssetType.PROMO)? 99999 : -1);
        }

        tableManager = new TableManager(this);
    }

    public TableManager getTableManager() {
        return tableManager;
    }

    public <T extends RestaurantAsset> boolean registerClassToAsset(Class<T> c, AssetType assetType) {
        if (classMap.containsKey(c) && listMap.containsKey(assetType)) {
            return true;
        }

        classMap.put(c, assetType);
        List<T> newList = new ArrayList<>();
        listMap.put(assetType, newList);
        return classMap.containsKey(c) && listMap.containsKey(assetType);
    }

    public List<? extends RestaurantAsset> getAsset(AssetType assetType) {
        if (listMap.containsKey(assetType)) {
            return listMap.get(assetType);
        }

        return null;
    }

    public void setCounter(AssetType assetType, int count) {
        if (counterMap.containsKey(assetType)) {
            counterMap.put(assetType, count);
        }
    }

    public int incrementAndGetCounter(AssetType assetType) {
        if (counterMap.containsKey(assetType)) {
            counterMap.put(assetType, counterMap.get(assetType) + 1);
            return counterMap.get(assetType);
        }

        return -1;
    }

    public int getCounter(AssetType assetType) {
        if (counterMap.containsKey(assetType)) {
            return counterMap.get(assetType);
        }

        return -1;
    }

    public void add(Object o) {
        update(o, false);
    }

    public void remove(Object o) {
        update(o, true);
    }

    private <T extends RestaurantAsset> void update(Object o, boolean remove) {
        if (!(o instanceof RestaurantAsset)) {
            return;
        }

        if (!classMap.containsKey(o.getClass())) {
            return;
        }

        AssetType assetType = classMap.get(o.getClass());
        if (listMap.containsKey(assetType)) {
            if (remove) {
                ((List<T>) listMap.get(classMap.get(o.getClass()))).remove((T) o);
                return;
            }

            ((List<T>) listMap.get(classMap.get(o.getClass()))).add((T) o);
        }
    }

    public RestaurantAsset getItemFromId(AssetType assetType, int id) {
        for (RestaurantAsset o : getAsset(assetType)) {
            if (o.getId() == id) {
                return o;
            }
        }

        return null;
    }

    public RestaurantAsset getItemFromIndex(AssetType assetType, int index) {
        List<? extends RestaurantAsset> assetList = getAsset(assetType);

        if (index >= assetList.size()) {
            return null;
        }

        return assetList.get(index);
    }
}
