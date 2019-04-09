package core;

import enums.DataType;
import tools.FileIO;

import java.util.*;
import java.util.stream.Collectors;

public class Restaurant {
    private Map<DataType, List<? extends RestaurantData>> dataListMap;
    private Map<DataType, Comparator<? extends RestaurantData>> defaultComparatorMap;
    private Map<DataType, Integer> uniqueIdMap;
    private int sessionStaffId = -1;

    <X extends RestaurantData> Restaurant() {
        dataListMap = new HashMap<>();
        defaultComparatorMap = new HashMap<>();
        uniqueIdMap = new HashMap<>();

        for (DataType dataType : DataType.values()) {
            final List<X> newList = new ArrayList<>();
            dataListMap.putIfAbsent(dataType, newList);
            defaultComparatorMap.putIfAbsent(dataType, Comparator.comparing(X::getId));
            uniqueIdMap.putIfAbsent(dataType, -1);
        }
    }

    public boolean isDataTypeExists(DataType dataType) {
        return dataListMap.getOrDefault(dataType, null) != null;
    }

    public <X extends RestaurantData> void setDefaultComparator(DataType dataType, Comparator<X> comparator) {
        defaultComparatorMap.put(dataType, comparator);
    }

    private <X extends RestaurantData> Comparator<X> getDefaultComparator(DataType dataType) {
        return (Comparator<X>) defaultComparatorMap.get(dataType);
    }

    private <X extends RestaurantData> void sortList(DataType dataType) throws Exception {
        List<X> dataList = getOriginalDataList(dataType);
        Comparator<X> comparator = getDefaultComparator(dataType);
        dataList = dataList.stream().sorted(comparator).collect(Collectors.toList());
        dataListMap.put(dataType, dataList);
    }

    public <X extends RestaurantData> List<X> getDataList(DataType dataType) throws Exception {
        sortList(dataType);
        return List.copyOf(getOriginalDataList(dataType));
    }

    private <X extends RestaurantData> List<X> getOriginalDataList(DataType dataType) throws Exception {
        final List<X> dataList = (List<X>) dataListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            throw (new Exception("Failed to get data list from restaurant."));
        }

        return dataList;
    }

    public <X extends RestaurantData> void save(X data) throws Exception {
        if (data == null) {
            throw (new RuntimeException("Passing a null object to save to restaurant."));
        }

        try {
            final DataType dataType = getDataTypeFromClass(data.getClass());
            final List<X> dataList = getOriginalDataList(dataType);
            final int index;
            final FileIO fileIO = new FileIO();

            if (dataList.stream().anyMatch(search -> search.getId() == data.getId())) {
                index = getFileLineFromId(dataType, data.getId());
                if (index == -1) {
                    throw (new Exception("Failed to get index for " + dataType + " of ID " + data.getId() + "."));
                }

                int fileId = Integer.parseInt(fileIO.read(dataType.name()).get(index).split(" // ")[0]);
                if (fileId != data.getId()) {
                    throw (new Exception("File ID mismatch for " + dataType + " at index " + index + ". (" + fileId + " VS " + data.getId() + ")"));
                }

                fileIO.updateLine(dataType.name(), index, data.toFileString());
            } else {
                fileIO.writeLine(dataType.name(), data.toFileString());
                dataList.add(data);
            }
        } catch (NumberFormatException e) {
            throw (new Exception("Invalid file data to save: " + e.getMessage()));
        }
    }

    public <X extends RestaurantData> void load(X data) throws Exception {
        if (data == null) {
            throw (new RuntimeException("Passing a null object to load into restaurant."));
        }

        final DataType dataType = getDataTypeFromClass(data.getClass());
        final List<X> dataList = getOriginalDataList(dataType);
        dataList.add(data);
    }

    public <X extends RestaurantData> void bulkSave(DataType dataType) throws Exception {
        final List<X> dataList = getDataList(dataType);
        final FileIO fileIO = new FileIO();
        fileIO.clearFile(dataType.name());

        for (X data : dataList) {
            fileIO.writeLine(dataType.name(), data.toFileString());
        }
    }

    public <X extends RestaurantData> void remove(X data) throws Exception {
        if (data == null) {
            throw (new RuntimeException("Passing a null object to remove from restaurant."));
        }

        try {
            final DataType dataType = getDataTypeFromClass(data.getClass());
            final List<X> dataList = getOriginalDataList(dataType);
            final int index;
            final FileIO fileIO = new FileIO();

            if (dataList.stream().anyMatch(search -> search.getId() == data.getId())) {
                index = getFileLineFromId(dataType, data.getId());
                if (index == -1) {
                    throw (new Exception("Failed to get index for " + dataType + " of ID " + data.getId() + "."));
                }

                int fileId = Integer.parseInt(fileIO.read(dataType.name()).get(index).split(" // ")[0]);
                if (fileId != data.getId()) {
                    throw (new Exception("File ID mismatch for " + dataType + " at index " + index + ". (" + fileId + " VS " + data.getId() + ")"));
                }

                fileIO.removeLine(dataType.name(), index);
                dataList.remove(data);
            } else {
                throw (new Exception("Object does not exist in restaurant."));
            }
        } catch (NumberFormatException e) {
            throw (new Exception("Invalid file data to save: " + e.getMessage()));
        }
    }

    public <X extends RestaurantData> X getDataFromId(DataType dataType, int id) throws Exception {
        List<X> dataList = getDataList(dataType);
        Optional<X> data = dataList.stream().filter(x -> x.matchId(id)).findFirst();

        if (data.isPresent()) {
            return data.get();
        }

        throw (new Exception("Failed to find item for the given ID '" + id + "'."));
    }

    public <X extends RestaurantData> X getDataFromIndex(DataType dataType, int index) throws Exception {
        List<X> dataList = getDataList(dataType);

        try {
            return dataList.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw (new Exception("Index out of bounds: " + e.getMessage()));
        }
    }

    private int getFileLineFromId(DataType dataType, int id) throws Exception {
        final List<String> fileData = (new FileIO()).read(dataType.name());
        int index = 0;

        try {
            for (String data : fileData) {
                final int fileId = Integer.parseInt(data.split(" // ")[0]);

                if (fileId == id) {
                    return index;
                }

                index++;
            }
        } catch (NumberFormatException e) {
            throw (new Exception("Invalid file data: " + e.getMessage()));
        }

        throw (new Exception("Failed to find index for the given ID '" + id + "'."));
    }

    public void setUniqueId(DataType dataType, int id) {
        if (id > uniqueIdMap.get(dataType)) {
            uniqueIdMap.put(dataType, id);
        }
    }

    public int generateUniqueId(DataType dataType) {
        uniqueIdMap.put(dataType, uniqueIdMap.getOrDefault(dataType, -1) + 1);
        return uniqueIdMap.get(dataType);
    }

    void setSessionStaffId(int sessionStaffId) {
        this.sessionStaffId = sessionStaffId;
    }

    public int getSessionStaffId() {
        return sessionStaffId;
    }

    private <X extends RestaurantData> DataType getDataTypeFromClass(Class<X> xClass) throws Exception {
        Optional<DataType> dataType = Arrays.stream(DataType.values()).filter(type -> type.getC().equals(xClass)).findFirst();

        if (dataType.isPresent()) {
            return dataType.get();
        }

        throw (new Exception("Failed to get data type from class for class: " + xClass.getSimpleName()));
    }
}
