package staff;

import client.DataManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import tools.FileIO;
import tools.Log;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StaffManager extends DataManager {

    public StaffManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() {
        getRestaurant().registerClassToDataType(Staff.class, DataType.STAFF);

        Comparator<Staff> comparator = Comparator.comparing(Staff::getName);
        getRestaurant().setDefaultComparator(DataType.STAFF, comparator);

        final List<String[]> fileData = (new FileIO()).read(DataType.STAFF).stream().map(data -> data.split(" // ")).filter(data -> data.length == 3).collect(Collectors.toList());

        for (String[] data : fileData) {
            try {
                final int id = Integer.parseInt(data[0]);
                final String name = data[1];
                final String title = data[2];
                getRestaurant().load(new Staff(id, name, title));
            } catch (NumberFormatException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Invalid file data: " + e.getMessage());
            }
        }

        getRestaurant().bulkSave(DataType.STAFF);
    }

    private List<String> getDisplayList(int sortOption) {
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

        final Optional<List<Staff>> dataList = getRestaurant().getDataList(DataType.STAFF);
        return dataList.map(data -> data.stream().sorted(comparator).map(Staff::toDisplayString).collect(Collectors.toList())).get();
    }

    public List<String> getStaffNames() {
        final Optional<List<Staff>> dataList = getRestaurant().getDataList(DataType.STAFF);
        return dataList.map(data -> data.stream().sorted(Comparator.comparing(Staff::getName)).map(Staff::getName).collect(Collectors.toList())).get();
    }

    private boolean ifNameExists(String name) {
        final Optional<List<Staff>> dataList = getRestaurant().getDataList(DataType.STAFF);
        return dataList.map(data -> data.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name))).get();
    }

    private boolean isStaffLoggedIn(Staff staff) {
        return (staff.getId() == getRestaurant().getSessionStaffId());
    }

    @Override
    public String[] getMainCLIOptions() {
        return new String[] {
                "View staff roster",
                "Add new staff",
                "Manage staff"
        };
    }

    @Override
    public Runnable[] getOptionRunnables() {
        return new Runnable[] {
                this::viewStaff,
                this::addStaff,
                this::manageStaff
        };
    }

    private void viewStaff() {
        int sortOption = 2;
        final String title = "Staff Roster";
        final List<String> sortOptions = Arrays.asList("Sort by ID", "Sort by name", "Sort by title");
        final List<String> footerOptions = Collections.singletonList("Go back");
        final List<String> options = getCm().formatChoiceList(sortOptions, footerOptions);
        List<String> displayList;

        do {
            getCm().clearCmd();
            displayList = getDisplayList(sortOption);
            getCm().printTable(title, "ID // Name // Title", displayList, true);
            getCm().printTable("Command // Sort Option", options, true);
        } while ((sortOption = getCm().getInt("Select a sort option", (1 - footerOptions.size()), sortOptions.size())) != 0);
        getCm().clearCmd();
    }

    private void addStaff() {
        getCm().printInstructions(Collections.singletonList("Enter -back in staff name to go back."));
        final String name = getCm().getString("Enter staff name");

        if (name.equalsIgnoreCase("-back")) {
            return;
        }

        final String title = getCm().getString("Enter staff title");

        if (ifNameExists(name)) {
            getCm().clearCmd();
            Log.notice("Failed to add staff as the staff already exists on the roster.");
            return;
        }

        final int id = getRestaurant().generateUniqueId(DataType.STAFF);

        if (getRestaurant().save(new Staff(id, name, title))) {
            getCm().clearCmd();
            System.out.println("Staff has been added successfully.");
        }
    }

    private void manageStaff() {
        final List<String> nameList = getStaffNames();
        List<String> choiceList = getCm().formatChoiceList(nameList, null);
        getCm().printTable("Manage Staff", "Command // Staff", choiceList, true);
        int staffIndex = getCm().getInt("Select a staff to manage", 0, nameList.size()) - 1;

        if (staffIndex == -1) {
            return;
        }

        Optional<Staff> oStaff = getRestaurant().getDataFromIndex(DataType.STAFF, staffIndex);

        if (oStaff.isEmpty()) {
            return;
        }

        final List<String> actions = Arrays.asList("Change name", "Change title", "Remove staff from roster");
        choiceList = getCm().formatChoiceList(actions, null);
        getCm().printTable("Command // Action", choiceList, true);
        final int action = getCm().getInt("Select an action", 0, actions.size());

        if (action == 0) {
            return;
        }

        Staff staff = oStaff.get();

        if (action == 3) {
            if (removeStaff(staff)) {
                getCm().clearCmd();
                Log.notice("Staff has been removed successfully.");
            }
        } else {
            if (updateStaffInfo(staff, action)) {
                getCm().clearCmd();
                Log.notice("Staff information has been updated successfully.");
            }
        }
    }

    private boolean updateStaffInfo(Staff staff, int action) {
        getCm().printInstructions(Collections.singletonList("Enter -back to go back."));
        final String input = getCm().getString("Enter the new " + ((action == 1)? "name" : "title"));

        if (input.equalsIgnoreCase("-back")) {
            return false;
        }

        if (action == 1) {
            staff.setName(input);
        } else {
            staff.setTitle(input);
        }

        return getRestaurant().save(staff);
    }

    private boolean removeStaff(Staff staff) {
        getCm().printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

        if (getCm().getString("Confirm remove?").equalsIgnoreCase("Y")) {
            if (isStaffLoggedIn(staff)) {
                getCm().clearCmd();
                Log.notice("Failed to remove staff as the staff is currently logged into the system.");
                return false;
            }

            return getRestaurant().remove(staff);
        }

        return false;
    }
}
