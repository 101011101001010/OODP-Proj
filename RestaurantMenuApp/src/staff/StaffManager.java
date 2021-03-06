package staff;

import core.RestaurantManager;
import core.Restaurant;
import core.RestaurantData;
import enums.DataType;
import tools.ConsolePrinter;
import tools.FileIO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages staff information in the restaurant list database through a CLI.
 */
public class StaffManager extends RestaurantManager {
    /**
     * Initialises the manager with a restaurant object for data storage and manipulation.
     * @param restaurant Restaurant instance from main.
     */
    public StaffManager(Restaurant restaurant) {
        super(restaurant);
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public void init() throws Exception {
        Comparator<Staff> comparator = Comparator.comparing(Staff::getName);
        getRestaurant().setDefaultComparator(DataType.STAFF, comparator);
        final List<String[]> fileData = (new FileIO()).read(DataType.STAFF).stream().map(data -> data.split(" // ")).filter(data -> data.length == 3).collect(Collectors.toList());

        for (String[] data : fileData) {
            try {
                final int id = Integer.parseInt(data[0]);
                final String name = data[1];
                final String title = data[2];
                getRestaurant().setUniqueId(DataType.STAFF, id);
                getRestaurant().load(new Staff(id, name, title));
            } catch (NumberFormatException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data: " + e.getMessage());
            }
        }

        if (getRestaurant().getDataList(DataType.STAFF).size() == 0) {
            final String name = "admin";
            final String title = "Default admin account";
            getRestaurant().load(new Staff(0, name, title));
        }

        getRestaurant().bulkSave(DataType.STAFF);
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public String[] getMainCLIOptions() {
        return new String[] {
                "View staff roster",
                "Add new staff",
                "Manage staff"
        };
    }

    /**
     * Please see the method description in RestaurantManager.
     * @see RestaurantManager
     */
    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[] {
                () -> display(1),
                () -> display(2),
                () -> display(3)
        };
    }

    /**
     * Maps to the various methods to run. Used as some methods throw exceptions, which will be caught by this method for logging purposes.
     * @param which Which method to run.
     */
    private void display(int which) {
        try {
            switch (which) {
                case 1:
                    viewStaff();
                    break;

                case 2:
                    addStaff();
                    break;

                case 3:
                    manageStaff();
                    break;
            }
        } catch (Exception e) {
            ConsolePrinter.logToFile(e.getMessage(), e);
        }
    }

    /**
     * Displays all staff information.
     * @throws Exception Errors that occurred while displaying the staff information.
     */
    private void viewStaff() throws Exception {
        final String title = "Staff Roster";
        final List<String> sortOptions = Arrays.asList("Sort by ID", "Sort by name", "Sort by title");
        final List<String> footerOptions = Collections.singletonList("Go back");
        final List<String> options = ConsolePrinter.formatChoiceList(sortOptions, footerOptions);
        int sortOption = 2;
        List<String> displayList;

        do {
            ConsolePrinter.clearCmd();
            displayList = getDisplayList(sortOption);
            ConsolePrinter.printTable(title, "ID // Name // Title", displayList, true);
            ConsolePrinter.printTable("Command // Sort Option", options, true);
        } while ((sortOption = getInputHelper().getInt("Select a sort option", (1 - footerOptions.size()), sortOptions.size())) != 0);

        ConsolePrinter.clearCmd();
    }

    /**
     * Adds a staff into the restaurant list database.
     * @throws Exception Errors that occurred while adding the item.
     */
    private void addStaff() throws Exception {
        ConsolePrinter.printInstructions(Collections.singletonList("Enter -back in staff name to go back."));
        final String name = getInputHelper().getString("Enter staff name");

        if (name.equalsIgnoreCase("-back")) {
            ConsolePrinter.clearCmd();
            return;
        }

        final String title = getInputHelper().getString("Enter staff title");
        final List<Staff> dataList = getRestaurant().getDataList(DataType.STAFF);
        final boolean isNameExists = dataList.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name));

        if (isNameExists) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to add staff as the staff already exists on the roster.");
            return;
        }

        final int id = getRestaurant().generateUniqueId(DataType.STAFF);
        getRestaurant().save(new Staff(id, name, title));
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Staff has been added successfully.");
    }


    /**
     * Manages staff information in the restaurant list database. Calls updateStaffInfo and removeStaff as needed.
     * @throws Exception Errors that occurred while managing the staff information.
     */
    private void manageStaff() throws Exception {
        final List<Staff> dataList = getRestaurant().getDataList(DataType.STAFF);
        final List<String> nameList = dataList.stream().map(Staff::getName).collect(Collectors.toList());
        List<String> choiceList = ConsolePrinter.formatChoiceList(nameList, null);
        ConsolePrinter.printTable("Manage Staff", "Command // Staff", choiceList, true);
        int staffIndex = getInputHelper().getInt("Select a staff to manage", 0, nameList.size()) - 1;

        if (staffIndex == -1) {
            ConsolePrinter.clearCmd();
            return;
        }

        Staff staff = getRestaurant().getDataFromIndex(DataType.STAFF, staffIndex);
        final List<String> actions = Arrays.asList("Change name", "Change title", "Remove staff from roster");
        choiceList = ConsolePrinter.formatChoiceList(actions, null);
        ConsolePrinter.printTable("Command // Action", choiceList, true);
        final int action = getInputHelper().getInt("Select an action", 0, actions.size());

        if (action == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        if (action == 3) {
            removeStaff(staff);
        } else {
            updateStaffInfo(staff, action);
        }
    }

    /**
     * Updates a staff's information in the restaurant list database.
     * @param staff Staff obtained from the calling function.
     * @param action Action the user chose in the calling function
     * @throws Exception Errors that occurred while updating staff information
     */
    private void updateStaffInfo(Staff staff, int action) throws Exception {
        ConsolePrinter.printInstructions(Collections.singletonList("Enter -back to go back."));
        final String input = getInputHelper().getString("Enter the new " + ((action == 1)? "name" : "title"));

        if (input.equalsIgnoreCase("-back")) {
            ConsolePrinter.clearCmd();
            return;
        }

        if (action == 1) {
            staff.setName(input);
        } else {
            staff.setTitle(input);
        }

        getRestaurant().save(staff);
        ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Staff information has been updated successfully.");
    }

    /**
     * Removes a staff's information in the restaurant list database.
     * @param staff Staff obtained from the calling function.
     * @throws Exception Errors that occurred while removing staff information
     */
    private void removeStaff(Staff staff) throws Exception {
        ConsolePrinter.printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

        if (getInputHelper().getString("Confirm remove?").equalsIgnoreCase("Y")) {
            if (staff.getId() == getRestaurant().getSessionStaffId()) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to remove staff as the staff is currently logged into the system.");
                return;
            }

            getRestaurant().remove(staff);
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Staff has been removed successfully.");
        }
    }

    /**
     * Formats the data objects obtained from the restaurant list database into table-friendly format.
     * @param sortOption Determines which comparator to sort the formatted list by.
     * @return List of formatted object data. One object per entry.
     * @throws Exception Errors that occurred while obtaining data objects.
     */
    private List<String> getDisplayList(int sortOption) throws Exception {
        Comparator<Staff> comparator;

        switch (sortOption) {
            case 2:
                comparator = Comparator.comparing(Staff::getName);
                break;
            case 3:
                comparator = Comparator.comparing(Staff::getTitle);
                break;
            default:
                comparator = Comparator.comparingInt(RestaurantData::getId);
        }

        final List<Staff> dataList = getRestaurant().getDataList(DataType.STAFF);
        return dataList.stream().sorted(comparator).map(Staff::toDisplayString).collect(Collectors.toList());
    }
}
