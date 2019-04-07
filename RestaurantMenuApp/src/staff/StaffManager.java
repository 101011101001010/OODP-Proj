package staff;

import core.RestaurantDataManager;
import core.Restaurant;
import core.RestaurantData;
import enums.DataType;
import tools.ConsolePrinter;
import tools.FileIO;

import java.util.*;
import java.util.stream.Collectors;

public class StaffManager extends RestaurantDataManager {

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
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.WARNING, "Invalid file data: " + e.getMessage());
            }
        }

        getRestaurant().bulkSave(DataType.STAFF);
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
        final List<String> options = ConsolePrinter.formatChoiceList(sortOptions, footerOptions);
        List<String> displayList;

        do {
            ConsolePrinter.clearCmd();
            displayList = getDisplayList(sortOption);
            ConsolePrinter.printTable(title, "ID // Name // Title", displayList, true);
            ConsolePrinter.printTable("Command // Sort Option", options, true);
        } while ((sortOption = getInputHelper().getInt("Select a sort option", (1 - footerOptions.size()), sortOptions.size())) != 0);
        ConsolePrinter.clearCmd();
    }

    private void addStaff() {
        ConsolePrinter.printInstructions(Collections.singletonList("Enter -back in staff name to go back."));
        final String name = getInputHelper().getString("Enter staff name");

        if (name.equalsIgnoreCase("-back")) {
            ConsolePrinter.clearCmd();
            return;
        }

        final String title = getInputHelper().getString("Enter staff title");
        final Optional<List<Staff>> dataList = getRestaurant().getDataList(DataType.STAFF);
        boolean isNameExists = dataList.map(data -> data.stream().anyMatch(x -> x.getName().equalsIgnoreCase(name))).get();

        if (isNameExists) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to add staff as the staff already exists on the roster.");
            return;
        }

        final int id = getRestaurant().generateUniqueId(DataType.STAFF);

        if (getRestaurant().save(new Staff(id, name, title))) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Staff has been added successfully.");
        }
    }

    private void manageStaff() {
        final List<String> nameList = getStaffNames();
        List<String> choiceList = ConsolePrinter.formatChoiceList(nameList, null);
        ConsolePrinter.printTable("Manage Staff", "Command // Staff", choiceList, true);
        int staffIndex = getInputHelper().getInt("Select a staff to manage", 0, nameList.size()) - 1;

        if (staffIndex == -1) {
            ConsolePrinter.clearCmd();
            return;
        }

        Optional<Staff> oStaff = getRestaurant().getDataFromIndex(DataType.STAFF, staffIndex);

        if (oStaff.isEmpty()) {
            return;
        }

        final List<String> actions = Arrays.asList("Change name", "Change title", "Remove staff from roster");
        choiceList = ConsolePrinter.formatChoiceList(actions, null);
        ConsolePrinter.printTable("Command // Action", choiceList, true);
        final int action = getInputHelper().getInt("Select an action", 0, actions.size());

        if (action == 0) {
            ConsolePrinter.clearCmd();
            return;
        }

        Staff staff = oStaff.get();

        if (action == 3) {
            if (removeStaff(staff)) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Staff has been removed successfully.");
            }
        } else {
            if (updateStaffInfo(staff, action)) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.SUCCESS, "Staff information has been updated successfully.");
            }
        }
    }

    private boolean updateStaffInfo(Staff staff, int action) {
        ConsolePrinter.printInstructions(Collections.singletonList("Enter -back to go back."));
        final String input = getInputHelper().getString("Enter the new " + ((action == 1)? "name" : "title"));

        if (input.equalsIgnoreCase("-back")) {
            ConsolePrinter.clearCmd();
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
        ConsolePrinter.printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));

        if (getInputHelper().getString("Confirm remove?").equalsIgnoreCase("Y")) {
            if (staff.getId() == getRestaurant().getSessionStaffId()) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to remove staff as the staff is currently logged into the system.");
                return false;
            }

            return getRestaurant().remove(staff);
        }

        return false;
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
        return dataList.map(data -> data.stream().map(Staff::getName).collect(Collectors.toList())).get();
    }
}
