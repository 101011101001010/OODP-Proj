package Classes;

import client.BaseManager;
import client.Restaurant;
import client.RestaurantAsset;
import client.enums.AssetType;
import client.enums.Op;
import tools.FileIO;
import tools.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaffManager extends BaseManager {
    private final AssetType asset = AssetType.STAFF;
    private final FileIO.FileNames fileName = FileIO.FileNames.STAFF_FILE;

    public StaffManager(Restaurant restaurant) {
        super(restaurant);
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
            if (displayList == null) {
                System.out.println("Failed to get list.");
                return;
            }
            getCs().printDisplayTable("Staff Roster", displayList);
        } while ((choice = getCs().printChoices("Command // Corresponding Function", Arrays.asList("Sort by ID", "Sort by name", "Sort by title", "Sort by gender"), new String[] {"Go back"})) != -1);
    }

    private void addStaff() {
        getCs().printInstructions(new String[] {"Note:", "Enter '/quit' in name to return to main menu."});
        String name = getCs().getString("Enter staff name");
        if (name.equalsIgnoreCase("/quit")) {
            return;
        }

        String title = getCs().getString("Enter staff title");
        String gender;
        do {
            getCs().printInstructions(new String[]{"M = Male", "F = Female", "O = Others"});
            gender = getCs().getString("Enter gender");
        } while (!gender.equalsIgnoreCase("M") && !gender.equalsIgnoreCase("F") && !gender.equalsIgnoreCase("O"));
        System.out.println(addStaff(name, title, gender));
    }

    private void manageStaff() {
        List<String> nameList = getStaffNames();
        if (nameList == null) {
            System.out.println("Failed to get list.");
            return;
        }

        int staffIndex = getCs().printChoices("Index // Staff Name", nameList, new String[]{"Go back"});
        if (staffIndex == -1) {
            return;
        }

        staffIndex -= 1;
        String[] actions = new String[] {"Change name.", "Change title.", "Change gender.", "Remove staff from roster."};
        int action = getCs().printChoices("Index // Action", actions, new String[]{"Go back"});
        if (action == -1) {
            return;
        }

        if (action == 4) {
            getCs().printInstructions(new String[]{"Warning: This action cannot be undone.", "Y = Yes", "Any other input = NO"});
            if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
                System.out.println(removeStaff(staffIndex));
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
            System.out.println(updateStaffInfo(staffIndex, (action == 1)? input : "", (action == 2)? input : "", (action == 3)? input : ""));
        }
    }

    @Override
    public Pair<Op, String> init() {
        FileIO f = new FileIO();
        List<String> fileData = f.read(fileName);
        String splitStr = " // ";

        if (fileData == null) {
            return (new Pair<>(Op.FAILED, "Failed to read files."));
        }

        if (!getRestaurant().registerClassToAsset(Staff.class, AssetType.STAFF)) {
            return (new Pair<>(Op.FAILED, "Failed to register class."));
        }

        for (String data : fileData) {
            String[] datas = data.split(splitStr);

            if (datas.length != 4) {
                continue;
            }

            int id = Integer.parseInt(datas[0]);
            getRestaurant().add(new Staff(id, datas[1], datas[2], datas[3]));

            if (id > getRestaurant().getCounter(asset)) {
                getRestaurant().setCounter(asset, id);
            }
        }

        return (new Pair<>(Op.SUCCESS, "Staff OK."));
    }

    private List<String> getDisplay(int... sortOptions) {
        int sortOption = (sortOptions.length > 0)? sortOptions[0] : 1;
        List<? extends RestaurantAsset> masterList = new ArrayList<>(getRestaurant().getAsset(asset));

        List<String> ret = new ArrayList<>();
        if (masterList.size() == 0) {
            ret.add("There is no staff registered yet.");
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

        for (RestaurantAsset o : masterList) {
            ret.add(o.toString());
        }

        return ret;
    }

    private List<String> getStaffNames() {
        List<String> nameList = new ArrayList<>();

        for (RestaurantAsset o : getRestaurant().getAsset(asset)) {
            nameList.add(((Staff) o).getName());
        }

        return nameList;
    }

    /*
    public boolean isValidStaff(int staffId) {
        for (RestaurantAsset o : getRestaurant().getAsset(asset)) {
            if (o.getId() == staffId) {
                return true;
            }
        }

        return false;
    }
    */

    private String addStaff(String name, String title, String gender) {
        Staff staff = new Staff(getRestaurant().incrementAndGetCounter(asset), name, title, gender);
        FileIO f = new FileIO();
        Pair<Op, String> response = f.writeLine(fileName, staff.toString());

        if (response.getLeft() == Op.SUCCESS) {
            getRestaurant().add(staff);
        }

        return response.getRight();
    }

    private String updateStaffInfo(int index, String name, String title, String gender) {
        Staff staff = (Staff) getRestaurant().getAsset(asset).get(index);
        FileIO f = new FileIO();
        int fileId = Integer.parseInt(f.read(fileName).get(index).split(" // ")[0]);

        if (fileId != staff.getId()) {
            return "File ID mismatch for remove. (" + fileId + " VS " + staff.getId() + ")";
        }

        name = (name.isBlank()? staff.getName() : name);
        title = (title.isBlank()? staff.getTitle() : title);
        gender = (gender.isBlank()? staff.getGender() : gender);

        Pair<Op, String> response = f.updateLine(fileName, index, staff.toString());

        if (response.getLeft().equals(Op.SUCCESS)) {
            staff.update(name, title, gender);
        }

        return response.getRight();
    }

    private String removeStaff(int index) {
        Staff staff = (Staff) getRestaurant().getAsset(asset).get(index);
        FileIO f = new FileIO();
        int fileId = Integer.parseInt(f.read(fileName).get(index).split(" // ")[0]);

        if (fileId != staff.getId()) {
            return "File ID mismatch for remove. (" + fileId + " VS " + staff.getId() + ")";
        }

        Pair<Op, String> response = f.removeLine(fileName, index);

        if (response.getLeft() == Op.SUCCESS) {
            getRestaurant().remove(staff);
        }

        return response.getRight();
    }
}
