package core;

import enums.DataType;
import tools.ConsolePrinter;
import tools.FileIO;

import java.util.*;
import java.util.stream.Collectors;

public class Restaurant {
    private Map<Class<? extends RestaurantData>, DataType> classDataTypeMap;
    private Map<DataType, List<? extends RestaurantData>> dataTypeListMap;
    private Map<DataType, Comparator<? extends RestaurantData>> dataTypeDefaultComparatorMap;
    private Map<DataType, Integer> uniqueIdMap;
    private int sessionStaffId = -1;

    Restaurant() {
        classDataTypeMap = new HashMap<>();
        dataTypeListMap = new HashMap<>();
        dataTypeDefaultComparatorMap = new HashMap<>();
        uniqueIdMap = new HashMap<>();
    }

    public <X extends RestaurantData> void registerClassToDataType(Class<X> targetClass, DataType dataType) {
        classDataTypeMap.putIfAbsent(targetClass, dataType);
        final List<X> newList = new ArrayList<>();
        dataTypeListMap.putIfAbsent(dataType, newList);
        dataTypeDefaultComparatorMap.putIfAbsent(dataType, Comparator.comparing(X::getId));
        uniqueIdMap.putIfAbsent(dataType, -1);
    }

    public <X extends RestaurantData> void setDefaultComparator(DataType dataType, Comparator<X> comparator) {
        dataTypeDefaultComparatorMap.put(dataType, comparator);
        sortList(dataType);
    }

    public <X extends RestaurantData> Comparator<X> getDefaultComparator(DataType dataType) {
        return (Comparator<X>) dataTypeDefaultComparatorMap.get(dataType);
    }

    private <X extends RestaurantData> void sortList(DataType dataType) {
        List<X> dataList = (List<X>) dataTypeListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Data list is null for " + dataType + "!");
            return;
        }

        Comparator<X> comparator = getDefaultComparator(dataType);
        dataList = dataList.stream().sorted(comparator).collect(Collectors.toList());
        dataTypeListMap.put(dataType, dataList);
    }

    public <X extends RestaurantData> Optional<List<X>> getDataList(DataType dataType) {
        final List<X> dataList = (List<X>) dataTypeListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Data list is null for " + dataType + "!");
            return Optional.empty();
        }

        Comparator<X> comparator = getDefaultComparator(dataType);
        return Optional.of(dataList.stream().sorted(comparator).collect(Collectors.toList()));
    }

    public <X extends RestaurantData> boolean save(X data) {
        if (data == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Passing a null object to save to restaurant.");
            return false;
        }

        final DataType dataType = classDataTypeMap.get(data.getClass());
        final List<X> dataList = (List<X>) dataTypeListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            return false;
        }

        final FileIO fileIO = new FileIO();

        if (dataList.stream().anyMatch(search -> search.getId() == data.getId())) {
            final int index = getFileLineFromId(dataType, data.getId());

            if (index == -1) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Failed to get index for " + dataType + " of ID " + data.getId() + ".");
                return false;
            }

            try {
                int fileId = Integer.parseInt(fileIO.read(dataType).get(index).split(" // ")[0]);

                if (fileId != data.getId()) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "File ID mismatch for " + dataType + " at index " + index + ". (" + fileId + " VS " + data.getId() + ")");
                    return false;
                }

                return fileIO.updateLine(dataType, index, data.toFileString());
            } catch (NumberFormatException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Invalid file data for " + dataType + " at index " + index + ".");
                return false;
            }
        }

        if (fileIO.writeLine(dataType, data.toFileString())) {
            dataList.add(data);
            sortList(dataType);
            return true;
        }

        return false;
    }

    public <X extends RestaurantData> boolean load(X data) {
        if (data == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Passing a null object to save to restaurant.");
            return false;
        }

        final DataType dataType = classDataTypeMap.get(data.getClass());
        final List<X> dataList = (List<X>) dataTypeListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Data list is null for " + dataType + "!");
            return false;
        }

        dataList.add(data);
        sortList(dataType);
        return true;
    }

    public <X extends RestaurantData> boolean bulkSave(DataType dataType) {
        final List<X> dataList = (List<X>) dataTypeListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Data list is null for " + dataType + "!");
            return false;
        }

        final FileIO fileIO = new FileIO();

        if (!fileIO.clearFile(dataType)) {
            return false;
        }

        dataList.stream().filter(Objects::nonNull).forEach(data -> fileIO.writeLine(dataType, data.toFileString()));
        sortList(dataType);
        return true;
    }

    public <X extends RestaurantData> boolean remove(X data) {
        if (data == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Passing a null object to remove from restaurant.");
            return false;
        }

        final DataType dataType = classDataTypeMap.get(data.getClass());
        final List<X> dataList = (List<X>) dataTypeListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Data list is null for " + dataType + "!");
            return false;
        }

        final FileIO fileIO = new FileIO();

        if (dataList.stream().anyMatch(search -> search.getId() == data.getId())) {
            final int index = getFileLineFromId(dataType, data.getId());

            if (index == -1) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Failed to get index for " + dataType + " of ID " + data.getId() + ".");
                return false;
            }

            try {
                int fileId = Integer.parseInt(fileIO.read(dataType).get(index).split(" // ")[0]);

                if (fileId != data.getId()) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "File ID mismatch for " + dataType + " at index " + index + ". (" + fileId + " VS " + data.getId() + ")");
                    return false;
                }

                if (fileIO.removeLine(dataType, index)) {
                    dataList.remove(data);
                    return true;
                }

                return false;
            } catch (NumberFormatException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Invalid file data for " + dataType + " at index " + index + ".");
                return false;
            }
        } else {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Item ID of " + dataType + " does not exist in restaurant.");
            return false;
        }
    }

    public <X extends RestaurantData> Optional<X> getDataFromId(DataType dataType, int id) {
        final Optional<List<X>> dataList = getDataList(dataType);
        return dataList.flatMap(data -> data.stream().filter(x -> x.getId() == id).findFirst());
    }

    public <X extends RestaurantData> Optional<X> getDataFromIndex(DataType dataType, int index) {
        final Optional<List<X>> dataList = getDataList(dataType);

        if (dataList.isPresent() && (index < dataList.get().size())) {
            return Optional.of(dataList.get().get(index));
        }

        ConsolePrinter.printMessage(ConsolePrinter.MessageType.ERROR, "Invalid index or data type for " + dataType + ": " + index + ".");
        return Optional.empty();
    }

    private <X extends RestaurantData> int getFileLineFromId(DataType dataType, int id) {
        final List<String> fileData = (new FileIO()).read(dataType);
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
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data for " + DataType.ALA_CARTE_ITEM.name() + ": " + e.getMessage());
        }

        return -1;
    }

    public void setUniqueId(DataType dataType, int id) {
        uniqueIdMap.put(dataType, id);
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
}
