package client;

import enums.DataType;
import tools.FileIO;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public <T extends RestaurantData> void registerClass(Class<T> targetClass, DataType dataType) {
        classMap.putIfAbsent(targetClass, dataType);
        dataMap.putIfAbsent(dataType, new ArrayList<T>());
        idCounterMap.putIfAbsent(dataType, -1);
    }

    boolean checkDataTypeExists(DataType dataType) {
        return (dataMap.containsKey(dataType));
    }

    public List<? extends RestaurantData> getData(DataType dataType) {
        List<? extends RestaurantData> dataList = dataMap.getOrDefault(dataType, null);

        if (dataList == null) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "Data list is null for " + dataType + ".");
            return null;
        }

        dataList = new ArrayList<>(List.copyOf(dataList));
        dataList.sort(Comparator.comparingInt(RestaurantData::getId));
        return dataList;
    }

    public void setCounter(DataType dataType, int count) {
        idCounterMap.put(dataType, count);
    }

    public int incrementAndGetCounter(DataType dataType) {
        idCounterMap.put(dataType, idCounterMap.getOrDefault(dataType, -1) + 1);
        return idCounterMap.get(dataType);
    }

    public int getCounter(DataType dataType) {
        return idCounterMap.getOrDefault(dataType, -1);
    }

    public <T extends RestaurantData> boolean save(T data) {
        final DataType dataType = classMap.get(data.getClass());

        try {
            (new FileIO()).writeLine(dataType, data.toFileString());
        } catch (IOException e) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "File IO error: " + e.getMessage());
            return false;
        }

        List<T> dataList = (List<T>) dataMap.getOrDefault(dataType, null);

        if (dataList == null) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "Data list is null for " + dataType + ".");
            return false;
        }

        dataList.add(data);
        return true;
    }

    public <T extends RestaurantData> boolean load(T data) {
        final DataType dataType = classMap.get(data.getClass());
        List<T> dataList = (List<T>) dataMap.getOrDefault(dataType, null);

        if (dataList == null) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "Data list is null for " + dataType + ".");
            return false;
        }

        dataList.add(data);
        return true;
    }

    public <T extends RestaurantData> boolean bulkSave(DataType dataType) {
        List<T> dataList = (List<T>) dataMap.getOrDefault(dataType, null);

        if (dataList == null) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "Data list is null for " + dataType + ".");
            return false;
        }

        try {
            final FileIO f = new FileIO();
            f.clearFile(dataType);

            for (RestaurantData data : dataList) {
                f.writeLine(dataType, data.toFileString());
            }

            return true;
        } catch (IOException e) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "File IO error: " + e.getMessage());
            return false;
        }
    }

    public <T extends RestaurantData> boolean update(T data) {
        final DataType dataType = classMap.get(data.getClass());
        final FileIO f = new FileIO();
        final int index = getIndexFromId(dataType, data.getId());

        try {
            if (index == -1) {
                throw (new IndexOutOfBoundsException("Failed to get data from file."));
            }

            int fileId = Integer.parseInt(f.read(dataType).get(index).split(" // ")[0]);

            if (fileId != data.getId()) {
                throw (new FileIDMismatchException(fileId, data.getId()));
            }

            f.updateLine(dataType, index, data.toFileString());
            return true;
        } catch (IOException e) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "File IO error: " + e.getMessage());
            return false;
        } catch (FileIDMismatchException | IndexOutOfBoundsException e) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", e.getMessage());
            return false;
        }
    }

    public <T extends RestaurantData> boolean remove(T data) {
        final DataType dataType = classMap.get(data.getClass());
        final FileIO f = new FileIO();
        final int index = getIndexFromId(dataType, data.getId());

        try {
            if (index == -1) {
                throw (new IndexOutOfBoundsException("Failed to get data from file."));
            }

            int fileId = Integer.parseInt(f.read(dataType).get(index).split(" // ")[0]);

            if (fileId != data.getId()) {
                throw (new FileIDMismatchException(fileId, data.getId()));
            }

            f.removeLine(dataType, index);
            Optional.ofNullable(dataMap.get(dataType)).ifPresent(list -> list.remove(data));
            return true;
        } catch (IOException e) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", "File IO error: " + e.getMessage());
            return false;
        } catch (FileIDMismatchException | IndexOutOfBoundsException e) {
            Logger.getGlobal().logp(Level.SEVERE, "", "", e.getMessage());
            return false;
        }
    }

    public RestaurantData getDataFromId(DataType dataType, int id) {
        for (RestaurantData o : getData(dataType)) {
            if (o.getId() == id) {
                return o;
            }
        }

        Logger.getGlobal().logp(Level.SEVERE, "", "", "Item ID is invalid: " + id + " for data type '" + dataType + "'");
        return null;
    }

    public RestaurantData getDataFromIndex(DataType dataType, int index) {
        final List<? extends RestaurantData> dataList = getData(dataType);

        if (index < dataList.size()) {
            return dataList.get(index);
        }

        Logger.getGlobal().logp(Level.SEVERE, "", "", "Index is invalid: " + index + " for data type '" + dataType + "'");
        return null;
    }

    public int getIndexFromId(DataType dataType, int id) {
        int index = 0;
        for (RestaurantData o : getData(dataType)) {
            if (o.getId() == id) {
                return index;
            }

            index++;
        }

        Logger.getGlobal().logp(Level.SEVERE, "", "", "Failed to get index from ID: " + id + " for data type '" + dataType + "'");
        return -1;
    }

    public class FileIDMismatchException extends RuntimeException {
        FileIDMismatchException(int fileId, int updateId) {
            super("ID mismatch when attempting to update file: " + fileId + " VS " + updateId + "");
        }
    }
}
