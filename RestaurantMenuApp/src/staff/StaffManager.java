package staff;

import client.DataManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import tools.FileIO;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StaffManager extends DataManager {

    public StaffManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws IOException {
        getRestaurant().registerClass(Staff.class, DataType.STAFF);
        final List<String[]> fileData = (new FileIO()).read(DataType.STAFF).stream().map(data -> data.split(" // ")).filter(data -> data.length == 3).collect(Collectors.toList());

        for (String[] data : fileData) {
            try {
                final int id = Integer.parseInt(data[0]);
                final String name = data[1];
                final String title = data[2];
                getRestaurant().load(new Staff(id, name, title));

                if (id > getRestaurant().getCounter(DataType.STAFF)) {
                    getRestaurant().setCounter(DataType.STAFF, id);
                }
            } catch (NumberFormatException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Invalid file data: " + e.getMessage());
            }
        }
    }

    private List<String> getDisplayList(int sortOption) {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.STAFF);
        final List<Staff> staffList = new ArrayList<>();
        dataList.stream().filter(staff -> staff instanceof Staff).forEach(staff -> staffList.add((Staff) staff));

        if (sortOption > 1) {
            staffList.sort(Comparator.comparing((sortOption == 3) ? Staff::getTitle : Staff::getName));
        } else {
            staffList.sort(Comparator.comparingInt(RestaurantData::getId));
        }

        return staffList.stream().map(Staff::toDisplayString).collect(Collectors.toList());
    }

    public List<String> getStaffNames() {
        final List<String> nameList = new ArrayList<>();

        for (RestaurantData o : getRestaurant().getData(DataType.STAFF)) {
            nameList.add(((Staff) o).getName());
        }

        return nameList;
    }

    private boolean addNewStaff(String name, String title) {
        final List<? extends RestaurantData> dataList = getRestaurant().getData(DataType.STAFF);
        long count = dataList.stream().filter(data -> data instanceof Staff).filter(item -> ((Staff) item).getName().equals(name)).count();

        if (count > 0) {
            Logger.getGlobal().logp(Level.INFO, "", "", "Failed to add item. Item already exists.");
            return false;
        }

        return getRestaurant().save(new Staff(getRestaurant().incrementAndGetCounter(DataType.STAFF), name, title));
    }

    private boolean updateStaffInfo(int index, String name, String title) {
        final Staff staff = (Staff) getRestaurant().getData(DataType.STAFF).get(index);
        name = (name.isBlank()? staff.getName() : name);
        title = (title.isBlank()? staff.getTitle() : title);
        boolean result = getRestaurant().update(staff);

        if (result) {
            staff.update(name, title);
        }

        return result;
    }

    private boolean removeStaff(int index) {
        if (getRestaurant().getDataFromIndex(DataType.STAFF, index).getId() != getRestaurant().getSessionStaffId()) {
            getRestaurant().remove(getRestaurant().getData(DataType.STAFF).get(index));
            return true;
        }

        Logger.getGlobal().logp(Level.INFO, "", "", "The staff is currently logged in. Please try again later.");
        return false;
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
        int sortOption = 1;
        final String title = "Staff Roster";
        final List<String> sortOptions = Arrays.asList("Sort by ID", "Sort by name", "Sort by title");
        final List<String> footerOptions = Collections.singletonList("Go back");
        final List<String> options = getCs().formatChoiceList(sortOptions, footerOptions);
        List<String> displayList;

        do {
            displayList = getDisplayList(sortOption);
            getCs().printTable(title, "ID // Name // Title", displayList, true);
            getCs().printTable(title, "Command / Sort Option", options, true);
        } while ((sortOption = getCs().getInt("Select a sort option", (1 - footerOptions.size()), sortOptions.size())) != 0);
    }

    private void addStaff() {
        getCs().printInstructions(Collections.singletonList("Enter -back in staff name to go back."));
        final String name = getCs().getString("Enter staff name");

        if (name.equalsIgnoreCase("-back")) {
            return;
        }

        final String title = getCs().getString("Enter staff title");

        if (addNewStaff(name, title)) {
            System.out.println("Staff has been added successfully.");
        }
    }

    private void manageStaff() {

        final String title = "Manage Staff";
        final List<String> nameList = getStaffNames();
        final List<String> footerChoices = Collections.singletonList("Go back");
        final String headers = "Command // Staff";
        List<String> choiceList = getCs().formatChoiceList(nameList, footerChoices);
        getCs().printTable(title, headers, choiceList, true);

        int staffIndex = getCs().getInt("Select a staff to manage", (1 - footerChoices.size()), nameList.size());

        if (staffIndex == 0) {
            return;
        }

        staffIndex--;
        final List<String> actions = Arrays.asList("Change name", "Change title", "Remove staff from roster");
        choiceList = getCs().formatChoiceList(actions, footerChoices);
        getCs().printTable(title, "Command // Action", choiceList, true);
        final int action = getCs().getInt("Select an action", (1 - footerChoices.size()), actions.size());

        if (action == 0) {
            return;
        }

        if (action == 3) {
            getCs().printInstructions(Collections.singletonList("Y = YES | Any other key = NO"));
            if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
                if (removeStaff(staffIndex)) {
                    System.out.println("Staff has been removed successfully.");
                }
            } else {
                System.out.println("Remove operation aborted.");
            }
        } else {
            final String what = (action == 1)? "name" : "title";
            getCs().printInstructions(Collections.singletonList("Enter -back to go back."));
            final String input = getCs().getString("Enter the new " + what);

            if (input.equalsIgnoreCase("/quit")) {
                return;
            }

            if (updateStaffInfo(staffIndex, (action == 1) ? input : "", (action == 2) ? input : "")) {
                System.out.println("Staff information has been updated successfully.");
            }
        }
    }
}
