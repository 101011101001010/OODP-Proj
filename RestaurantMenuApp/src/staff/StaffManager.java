package staff;

import client.BaseManager;
import client.Restaurant;
import client.RestaurantData;
import enums.DataType;
import tools.FileIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;

public class StaffManager extends BaseManager {

    public StaffManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws ManagerInitFailedException {
        try {
            getRestaurant().registerClass(Staff.class, DataType.STAFF);
        } catch (Restaurant.ClassNotRegisteredException e) {
            throw (new ManagerInitFailedException(this, "Class registration failed: " + e.getMessage()));
        }

        FileIO f = new FileIO();
        List<String> fileData;

        try {
            fileData = f.read(DataType.STAFF);
        } catch (IOException e) {
            throw (new ManagerInitFailedException(this, "Unable to load staff from file: " + e.getMessage()));
        }

        String splitStr = " // ";
        for (String data : fileData) {
            String[] datas = data.split(splitStr);

            if (datas.length != 4) {
                continue;
            }

            int id;
            try {
                id = Integer.parseInt(datas[0]);
            } catch (InputMismatchException e) {
                throw (new ManagerInitFailedException(this, "Invalid file data: " + e.getMessage()));
            }

            getRestaurant().load(new Staff(id, datas[1], datas[2], datas[3]));
            if (id > getRestaurant().getCounter(DataType.STAFF)) {
                getRestaurant().setCounter(DataType.STAFF, id);
            }
        }
    }

    public List<String> getDisplay(int sortOption) {
        List<? extends RestaurantData> masterList = new ArrayList<>(getRestaurant().getData(DataType.STAFF));
        List<String> ret = new ArrayList<>();
        if (masterList.size() == 0) {
            ret.add("There is no staff on the roster.");
            return ret;
        }

        masterList.sort((item1, item2) -> {
            switch (sortOption) {
                case 2: return ((Staff) item1).getName().compareTo(((Staff) item2).getName());
                case 3: return ((Staff) item1).getTitle().compareTo(((Staff) item2).getTitle());
                case 4: return ((Staff) item1).getGender().compareTo(((Staff) item2).getGender());
            }

            return Integer.compare(item1.getId(), item2.getId());
        });

        ret.add("ID // Name // Title // Gender");
        for (RestaurantData o : masterList) {
            ret.add(o.toTableString());
        }

        return ret;
    }

    public List<String> getStaffNames() {
        List<String> nameList = new ArrayList<>();

        for (RestaurantData o : getRestaurant().getData(DataType.STAFF)) {
            nameList.add(((Staff) o).getName());
        }

        return nameList;
    }

    private void addNewStaff(String name, String title, String gender) throws IOException {
        getRestaurant().save(new Staff(getRestaurant().incrementAndGetCounter(DataType.STAFF), name, title, gender));
    }

    private void updateStaffInfo(int index, String name, String title, String gender) throws IOException, Restaurant.FileIDMismatchException {
        Staff staff = (Staff) getRestaurant().getData(DataType.STAFF).get(index);
        name = (name.isBlank()? staff.getName() : name);
        title = (title.isBlank()? staff.getTitle() : title);
        gender = (gender.isBlank()? staff.getGender() : gender);
        getRestaurant().update(staff);
        staff.update(name, title, gender);
    }

    private boolean removeStaff(int index) throws IOException, Restaurant.FileIDMismatchException {
        if (getRestaurant().getDataFromIndex(DataType.STAFF, index).getId() != getRestaurant().getSessionStaffId()) {
            getRestaurant().remove(getRestaurant().getData(DataType.STAFF).get(index));
            return true;
        }

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
        int choice = 1;
        List<String> displayList;

        do {
            displayList = getDisplay(choice);
            getCs().printDisplayTable("Staff Roster", displayList, true, true);
        } while ((choice = getCs().printChoices("Select a sort option", "Command // Corresponding Function", Arrays.asList("Sort by ID", "Sort by name", "Sort by title", "Sort by gender"), new String[] {"Go back"})) != -1);
    }

    private void addStaff() {
        getCs().printInstructions(new String[] {"Note:", "Enter '/quit' in name to return to main menu."});
        String name = getCs().getString("Enter STAFF name");
        if (name.equalsIgnoreCase("/quit")) {
            return;
        }

        String title = getCs().getString("Enter STAFF title");
        String gender;

        do {
            getCs().printInstructions(new String[]{"M = Male", "F = Female", "O = Others"});
            gender = getCs().getString("Enter gender");
        } while (!gender.equalsIgnoreCase("M") && !gender.equalsIgnoreCase("F") && !gender.equalsIgnoreCase("O"));

        try {
            addNewStaff(name, title, gender);
            System.out.println("Staff has been added successfully.");
        } catch (IOException e) {
            System.out.println("Failed to add staff: " + e.getMessage());
        }
    }

    private void manageStaff() {
        List<String> nameList = getStaffNames();
        int staffIndex = getCs().printChoices("Select a staff to manage", "Index // Staff Name", nameList, new String[]{"Go back"}) - 1;
        if (staffIndex == -1) {
            return;
        }

        int action = getCs().printChoices("Select an action", "Index // Action", new String[] {"Change name.", "Change title.", "Change gender.", "Remove STAFF from roster."}, new String[]{"Go back"});
        if (action == -1) {
            return;
        }

        if (action == 4) {
            getCs().printInstructions(new String[]{"Warning: This action cannot be undone.", "Y = Yes", "Any other input = NO"});
            if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
                try {
                    if (removeStaff(staffIndex)) {
                        System.out.println("Staff has been removed successfully.");
                    } else {
                        System.out.println("The staff account is currently logged in.");
                    }
                } catch (IOException | Restaurant.FileIDMismatchException e) {
                    System.out.println("Failed to remove staff: " + e.getMessage());
                }
            } else {
                System.out.println("Remove operation aborted.");
            }
        } else {
            String what = (action == 1)? "name" : (action == 3)? "title" : "gender";
            getCs().printInstructions(new String[] {"Note:", "Enter '/quit' to return to main menu."});

            String input = getCs().getString("Enter the new " + what);
            if (input.equalsIgnoreCase("/quit")) {
                return;
            }

            try {
                updateStaffInfo(staffIndex, (action == 1) ? input : "", (action == 2) ? input : "", (action == 3) ? input : "");
            } catch (IOException | Restaurant.FileIDMismatchException e) {
                System.out.println("Failed to update staff: " + e.getMessage());
            }
        }
    }
}
