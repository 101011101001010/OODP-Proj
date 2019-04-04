package client;

import enums.AssetType;
import enums.FileName;
import tools.FileIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Restaurant {
    private HashMap<AssetType, List<? extends RestaurantAsset>> assetListMap;
    private HashMap<AssetType, Integer> idCounterMap;
    private HashMap<Class<?>, AssetType> classMap;

    /**
     * Initialises the restaurant's variables.
     */
    Restaurant() {
        assetListMap = new HashMap<>();
        idCounterMap = new HashMap<>();
        classMap = new HashMap<>();
    }

    /**
     * Maps a RestaurantAsset sub-class to an asset type.
     * @param newClass name of sub-class to map
     * @param assetType asset type to map to
     * @param <T> sub-class of RestaurantAsset
     * @return true if class has been mapped successfully or already mapped
     */
    public <T extends RestaurantAsset> boolean mapClassToAssetType(Class<T> newClass, AssetType assetType) {
        if (classMap.containsKey(newClass) && assetListMap.containsKey(assetType) && idCounterMap.containsKey(assetType)) {
            return true;
        }

        List<T> newList = new ArrayList<>();
        assetListMap.put(assetType, newList);
        idCounterMap.put(assetType, -1);
        classMap.put(newClass, assetType);
        return (classMap.containsKey(newClass) && assetListMap.containsKey(assetType) && idCounterMap.containsKey(assetType));
    }

    /**
     * Gets assets stored in the restaurant's inventory
     * @param assetType the asset type to retrieve
     * @return list of assets of the supplied asset type
     * @throws AssetNotRegisteredException if no sub-class is mapped to the supplied asset type
     */
    public List<? extends RestaurantAsset> getAsset(AssetType assetType) throws AssetNotRegisteredException {
        if (assetListMap.containsKey(assetType)) {
            return assetListMap.get(assetType);
        }

        throw (new AssetNotRegisteredException());
    }

    public void setCounter(AssetType assetType, int count) {
        if (idCounterMap.containsKey(assetType)) {
            idCounterMap.put(assetType, count);
        }
    }

    public int incrementAndGetCounter(AssetType assetType) {
        if (idCounterMap.containsKey(assetType)) {
            idCounterMap.put(assetType, idCounterMap.get(assetType) + 1);
            return idCounterMap.get(assetType);
        }

        return -1;
    }

    public int getCounter(AssetType assetType) {
        if (idCounterMap.containsKey(assetType)) {
            return idCounterMap.get(assetType);
        }

        return -1;
    }

    private <T extends RestaurantAsset> void add(T o, boolean isNew) throws AssetNotRegisteredException, IOException {
        if (o == null) {
            return;
        }

        if (!classMap.containsKey(o.getClass())) {
            throw (new AssetNotRegisteredException());
        }

        AssetType assetType = classMap.get(o.getClass());
        FileName fileName = FileName.valueOf(assetType.name());

        if (isNew) {
            (new FileIO()).writeLine(fileName, o.toPrintString());
        }

        ((List<T>) assetListMap.get(assetType)).add(o);
    }

    public <T extends RestaurantAsset> void addNew(T toAdd) throws AssetNotRegisteredException, IOException {
        add(toAdd, true);
    }

    public <T extends RestaurantAsset> void addFromFile(T toAdd) throws AssetNotRegisteredException, IOException {
        add(toAdd, false);
    }

    private <T extends RestaurantAsset> void updateOrRemove(T o, boolean update) throws AssetNotRegisteredException, IOException, FileIDMismatchException {
        if (o == null) {
            return;
        }

        if (!classMap.containsKey(o.getClass())) {
            throw (new AssetNotRegisteredException());
        }

        AssetType assetType = classMap.get(o.getClass());
        FileIO f = new FileIO();
        int index = 0;

        for (RestaurantAsset asset : getAsset(assetType)) {
            if (asset.getId() == o.getId()) {
                break;
            }

            index++;
        }

        FileName fileName = FileName.valueOf(assetType.name());
        int fileId = Integer.parseInt(f.read(fileName).get(index).split(" // ")[0]);
        if (fileId != o.getId()) {
            throw (new FileIDMismatchException(fileId, o.getId()));
        }

        if (update) {
            f.updateLine(fileName, index, o.toPrintString());
        } else {
            f.removeLine(fileName, index);
            assetListMap.get(assetType).remove(o);
        }
    }

    public <T extends RestaurantAsset> void update(T toUpdate) throws AssetNotRegisteredException, IOException, FileIDMismatchException {
        updateOrRemove(toUpdate, true);
    }

    public <T extends RestaurantAsset> void remove(T toRemove) throws AssetNotRegisteredException, IOException, FileIDMismatchException {
        updateOrRemove(toRemove, false);
    }

    public RestaurantAsset getAssetFromId(AssetType assetType, int id) throws AssetNotRegisteredException, IndexOutOfBoundsException {
        for (RestaurantAsset o : getAsset(assetType)) {
            if (o.getId() == id) {
                return o;
            }
        }

        throw (new IndexOutOfBoundsException("Item ID is invalid."));
    }

    public RestaurantAsset getAssetFromIndex(AssetType assetType, int index) throws AssetNotRegisteredException, IndexOutOfBoundsException {
        List<? extends RestaurantAsset> assetList = getAsset(assetType);

        if (index < assetList.size()) {
            return assetList.get(index);
        }

        throw (new IndexOutOfBoundsException("Index is invalid."));
    }

    public class AssetNotRegisteredException extends Exception {
        AssetNotRegisteredException() {
            super("Asset is not registered to restaurant.");
        }
    }

    public class FileIDMismatchException extends Exception {
        FileIDMismatchException(int fileId, int updateId) {
            super("ID mismatch when attempting to update file. (" + fileId + " VS " + updateId + ")");
        }
    }
}
