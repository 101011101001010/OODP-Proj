package client;

import enums.DataType;
import tools.FileIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Restaurant {
    private HashMap<DataType, List<? extends RestaurantData>> dataMap;
    private HashMap<DataType, Integer> idCounterMap;
    private HashMap<Class<?>, DataType> classMap;
    private int sessionStaffId;

    Restaurant() {
        dataMap = new HashMap<>();
        idCounterMap = new HashMap<>();
        classMap = new HashMap<>();
    }

    void setSessionStaffId(int sessionStaffId) {
        this.sessionStaffId = sessionStaffId;
    }

    public int getSessionStaffId() {
        return sessionStaffId;
    }

    public <T extends RestaurantData> void registerClass(Class<T> targetClass, DataType dataType) throws ClassNotRegisteredException {
        if (checkDataTypeExists(dataType)) {
            return;
        }

        classMap.put(targetClass, dataType);
        dataMap.put(dataType, new ArrayList<T>());
        idCounterMap.put(dataType, -1);

        if (!checkDataTypeExists(dataType)) {
            throw (new ClassNotRegisteredException(targetClass));
        }
    }

    public <T extends RestaurantData> boolean checkDataTypeExists(DataType dataType) {
        return (dataMap.containsKey(dataType));
    }

    public List<? extends RestaurantData> getData(DataType dataType) {
        return dataMap.getOrDefault(dataType, null);
    }

    public void setCounter(DataType dataType, int count) {
        idCounterMap.put(dataType, count);
    }

    public int incrementAndGetCounter(DataType dataType) {
        idCounterMap.put(dataType, idCounterMap.get(dataType) + 1);
        return idCounterMap.get(dataType);
    }

    public int getCounter(DataType dataType) {
        return idCounterMap.get(dataType);
    }

    public <T extends RestaurantData> void save(T data) throws IOException {
        DataType dataType = classMap.get(data.getClass());
        (new FileIO()).writeLine(dataType, data.toPrintString());
        ((List<T>) dataMap.get(dataType)).add(data);
    }

    public <T extends RestaurantData> void load(T data) {
        DataType dataType = classMap.get(data.getClass());
        ((List<T>) dataMap.get(dataType)).add(data);
    }

    public <T extends RestaurantData> void update(T data) throws IOException, FileIDMismatchException {
        DataType dataType = classMap.get(data.getClass());
        FileIO f = new FileIO();
        int index = 0;
        for (RestaurantData asset : getData(dataType)) {
            if (asset.getId() == data.getId()) {
                break;
            } index++;
        }

        int fileId = Integer.parseInt(f.read(dataType).get(index).split(" // ")[0]);
        if (fileId != data.getId()) {
            throw (new FileIDMismatchException(fileId, data.getId()));
        }

        f.updateLine(dataType, index, data.toPrintString());
    }

    public <T extends RestaurantData> void remove(T data) throws IOException, FileIDMismatchException {
        DataType dataType = classMap.get(data.getClass());
        FileIO f = new FileIO();
        int index = 0;
        for (RestaurantData asset : getData(dataType)) {
            if (asset.getId() == data.getId()) {
                break;
            } index++;
        }

        int fileId = Integer.parseInt(f.read(dataType).get(index).split(" // ")[0]);
        if (fileId != data.getId()) {
            throw (new FileIDMismatchException(fileId, data.getId()));
        }

        f.removeLine(dataType, index);
        dataMap.get(dataType).remove(data);
    }

    public RestaurantData getDataFromId(DataType assetType, int id) throws IndexOutOfBoundsException {
        for (RestaurantData o : getData(assetType)) {
            if (o.getId() == id) {
                return o;
            }
        }

        throw (new IndexOutOfBoundsException("Item ID is invalid: " + id));
    }

    public RestaurantData getDataFromIndex(DataType assetType, int index) throws IndexOutOfBoundsException {
        List<? extends RestaurantData> assetList = getData(assetType);

        if (index < assetList.size()) {
            return assetList.get(index);
        }

        throw (new IndexOutOfBoundsException("Index is invalid: " + index));
    }

    public class FileIDMismatchException extends Exception {
        FileIDMismatchException(int fileId, int updateId) {
            super("ID mismatch when attempting to update file: " + fileId + " VS " + updateId + "");
        }
    }

    public class ClassNotRegisteredException extends Exception {
        public ClassNotRegisteredException(Class c) {
            super("Class is not registered to restaurant: " + c.getSimpleName());
        }
    }
}
