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

    /**
     * Checks if the given data type has a list dedicated to it.
     * @param dataType Data type enum value to check
     * @return True / False
     */
    public boolean isDataTypeExists(DataType dataType) {
        return dataListMap.getOrDefault(dataType, null) != null;
    }

    /**
     * Overwrites the default comparator for list sorting when retrieving data. Data is sorted whenever it is retrieved.
     * @param dataType data type set the default comparator to
     * @param comparator comparator to overwrite
     * @param <X> classes should be sub-classes of RestaurantData
     */
    public <X extends RestaurantData> void setDefaultComparator(DataType dataType, Comparator<X> comparator) {
        defaultComparatorMap.put(dataType, comparator);
    }

    /**
     * Gets the default comparator for sorting.
     * @param dataType data type to retrieve comparator for
     * @param <X> classes should be sub-classes of RestaurantData
     * @return the default comparator for the specified data type
     */
    private <X extends RestaurantData> Comparator<X> getDefaultComparator(DataType dataType) {
        return (Comparator<X>) defaultComparatorMap.get(dataType);
    }

    /**
     * Sorts the list of the given data type
     * @param dataType data type of the list to sort
     * @param <X> classes should be sub-classes of RestaurantData
     * @throws Exception contains error messages as to why sorting failed
     */
    private <X extends RestaurantData> void sortList(DataType dataType) throws Exception {
        List<X> dataList = getOriginalDataList(dataType);
        Comparator<X> comparator = getDefaultComparator(dataType);
        dataList = dataList.stream().sorted(comparator).collect(Collectors.toList());
        dataListMap.put(dataType, dataList);
    }

    /**
     * Retrieves a copy of the list for a given data type
     * @param dataType data type of the list to retrieve
     * @param <X> classes should be sub-classes of RestaurantData
     * @return list of the given data type, sorted by its default comparator
     * @throws Exception contains error messages as to why list retrieval failed
     */
    public <X extends RestaurantData> List<X> getDataList(DataType dataType) throws Exception {
        sortList(dataType);
        return List.copyOf(getOriginalDataList(dataType));
    }

    /**
     * Retrieves the original list for a given data type for manipulation, without any sorting
     * @param dataType data type of the list to retrieve
     * @param <X> classes should be sub-classes of RestaurantData
     * @return list of the given data type without any sorting
     * @throws Exception contains error messages as to why list retrieval failed
     */
    private <X extends RestaurantData> List<X> getOriginalDataList(DataType dataType) throws Exception {
        final List<X> dataList = (List<X>) dataListMap.getOrDefault(dataType, null);

        if (dataList == null) {
            throw (new Exception("Failed to get data list from restaurant."));
        }

        return dataList;
    }

    /**
     * Saves a RestaurantData object into the restaurant's list database, then write to its respective text file.
     * @param data an RestaurantData object, or its sub-classes' equivalents
     * @param <X> classes should be sub-classes of RestaurantData
     * @throws Exception contains error messages as to why object could not be saved to restaurant
     */
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

    /**
     * Saves a RestaurantData object into the restaurant's list database without writing to text file.
     * @param data an RestaurantData object, or its sub-classes' equivalents, to be saved
     * @param <X> classes should be sub-classes of RestaurantData
     * @throws Exception contains error messages as to why object could not be saved to restaurant
     */
    public <X extends RestaurantData> void load(X data) throws Exception {
        if (data == null) {
            throw (new RuntimeException("Passing a null object to load into restaurant."));
        }

        final DataType dataType = getDataTypeFromClass(data.getClass());
        final List<X> dataList = getOriginalDataList(dataType);
        dataList.add(data);
    }

    /**
     * Writes data of all objects of a given data type in the restaurant list database into their respective text files.
     * @param dataType data type of objects to write data from
     * @param <X> classes should be sub-classes of RestaurantData
     * @throws Exception contains error messages as to why file writing failed
     */
    public <X extends RestaurantData> void bulkSave(DataType dataType) throws Exception {
        final List<X> dataList = getDataList(dataType);
        final FileIO fileIO = new FileIO();
        fileIO.clearFile(dataType.name());

        for (X data : dataList) {
            fileIO.writeLine(dataType.name(), data.toFileString());
        }
    }

    /**
     * Removes a RestaurantData object from the restaurant's list database, then remove its data from its text file.
     * @param data an RestaurantData object, or its sub-classes' equivalents, to be removed
     * @param <X> classes should be sub-classes of RestaurantData
     * @throws Exception contains error messages as to why object could not be removed from restaurant
     */
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

    /**
     * Retrieves a RestaurantData object of a given data type by its unique ID.
     * @param dataType data type to retrieve object of
     * @param id ID of the object to retrieve
     * @param <X> classes should be sub-classes of RestaurantData
     * @return Returns the respective RestaurantData object of the given ID
     * @throws Exception contains error messages as to why object could not be removed from restaurant
     */
    public <X extends RestaurantData> X getDataFromId(DataType dataType, int id) throws Exception {
        List<X> dataList = getDataList(dataType);
        Optional<X> data = dataList.stream().filter(x -> x.matchId(id)).findFirst();

        if (data.isPresent()) {
            return data.get();
        }

        throw (new Exception("Failed to find item for the given ID '" + id + "'."));
    }

    /**
     * Retrieves a RestaurantData object of a given data type by its index on the text file.
     * @param dataType data type to retrieve object of
     * @param index index of the object to retrieve
     * @param <X> classes should be sub-classes of RestaurantData
     * @return Returns the respective RestaurantData object of the given index
     * @throws Exception contains error messages as to why object could not be retrieved
     */
    public <X extends RestaurantData> X getDataFromIndex(DataType dataType, int index) throws Exception {
        List<X> dataList = getDataList(dataType);

        try {
            return dataList.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw (new Exception("Index out of bounds: " + e.getMessage()));
        }
    }

    /**
     * Retrieves the file index of an object of a given dataType by its ID.
     * @param dataType data type to retrieve index of
     * @param id ID of the object to retrieve
     * @return Returns the respective file index of the object of the given ID
     * @throws Exception contains error messages as to why object could not be retrieved
     */
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

    /**
     * Sets the initial value for the unique ID generator for the given dataType.
     * @param dataType data type to modify the ID generator of
     * @param id initial value of the generator
     */
    public void setUniqueId(DataType dataType, int id) {
        if (id > uniqueIdMap.get(dataType)) {
            uniqueIdMap.put(dataType, id);
        }
    }

    /**
     * Generates a unique ID for the given dataType
     * @param dataType data type to generate ID for
     * @return the generated ID
     */
    public int generateUniqueId(DataType dataType) {
        uniqueIdMap.put(dataType, uniqueIdMap.getOrDefault(dataType, -1) + 1);
        return uniqueIdMap.get(dataType);
    }

    /**
     * Sets the staff ID for the current restaurant session.
     * @param sessionStaffId the staff ID to set
     */
    void setSessionStaffId(int sessionStaffId) {
        this.sessionStaffId = sessionStaffId;
    }

    /**
     * Retrieves the staff ID for the current restaurant session.
     * @return the staff ID
     */
    public int getSessionStaffId() {
        return sessionStaffId;
    }

    /**
     * Gets the data type associated with a class (defined in DataType enum)
     * @param xClass the class to retrieve the data type of
     * @param <X> classes should be sub-classes of RestaurantData
     * @return the data type of the given class
     * @throws Exception contains error messages as to why the data type could not be obtained
     */
    private <X extends RestaurantData> DataType getDataTypeFromClass(Class<X> xClass) throws Exception {
        Optional<DataType> dataType = Arrays.stream(DataType.values()).filter(type -> type.getC().equals(xClass)).findFirst();

        if (dataType.isPresent()) {
            return dataType.get();
        }

        throw (new Exception("Failed to get data type from class for class: " + xClass.getSimpleName()));
    }
}
