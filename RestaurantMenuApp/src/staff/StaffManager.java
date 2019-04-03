package staff;

import client.BaseManager;
import client.Restaurant;
import client.RestaurantAsset;
import enums.AssetType;
import tools.FileIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaffManager extends BaseManager {
    private final AssetType assetType = AssetType.STAFF;

    public StaffManager(Restaurant restaurant) {
        super(restaurant);
    }

    @Override
    public void init() throws ManagerInitFailedException {
        FileIO f = new FileIO();
        List<String> fileData;

        try {
            fileData = f.read(assetType);
        } catch (IOException e) {
            throw (new ManagerInitFailedException(this, "Unable to load staff from file: " + e.getMessage()));
        }

        String splitStr = " // ";

        if (!getRestaurant().mapClassToAssetType(Staff.class, AssetType.STAFF)) {
            throw (new ManagerInitFailedException(this, "Failed to register class and asset to restaurant."));
        }

        for (String data : fileData) {
            String[] datas = data.split(splitStr);

            if (datas.length != 4) {
                continue;
            }

            int id = Integer.parseInt(datas[0]);

            try {
                getRestaurant().addFromFile(new Staff(id, datas[1], datas[2], datas[3]));
            } catch (Restaurant.AssetNotRegisteredException | IOException e) {
                throw (new ManagerInitFailedException(this, e.getMessage()));
            }

            if (id > getRestaurant().getCounter(assetType)) {
                getRestaurant().setCounter(assetType, id);
            }
        }
    }

    private List<String> getDisplay(int sortOption) throws Restaurant.AssetNotRegisteredException {
        List<? extends RestaurantAsset> masterList = new ArrayList<>(getRestaurant().getAsset(assetType));
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
        for (RestaurantAsset o : masterList) {
            ret.add(o.toTableString());
        }

        return ret;
    }

    private List<String> getStaffNames() throws Restaurant.AssetNotRegisteredException {
        List<String> nameList = new ArrayList<>();

        for (RestaurantAsset o : getRestaurant().getAsset(assetType)) {
            nameList.add(((Staff) o).getName());
        }

        return nameList;
    }


    public boolean isValidStaff(int staffId) throws Restaurant.AssetNotRegisteredException {
        for (RestaurantAsset o : getRestaurant().getAsset(assetType)) {
            if (o.getId() == staffId) {
                return true;
            }
        }

        return false;
    }

    private void addNewStaff(String name, String title, String gender) throws IOException, Restaurant.AssetNotRegisteredException {
        getRestaurant().addNew(new Staff(getRestaurant().incrementAndGetCounter(assetType), name, title, gender));
    }

    private void updateStaffInfo(int index, String name, String title, String gender) throws Restaurant.AssetNotRegisteredException, IOException, Restaurant.FileIDMismatchException {
        Staff staff = (Staff) getRestaurant().getAsset(assetType).get(index);
        name = (name.isBlank()? staff.getName() : name);
        title = (title.isBlank()? staff.getTitle() : title);
        gender = (gender.isBlank()? staff.getGender() : gender);
        getRestaurant().update(staff);
        staff.update(name, title, gender);
    }

    private void removeStaff(int index) throws Restaurant.AssetNotRegisteredException, IOException, Restaurant.FileIDMismatchException {
        getRestaurant().remove(getRestaurant().getAsset(assetType).get(index));
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
            try {
                displayList = getDisplay(choice);
            } catch (Restaurant.AssetNotRegisteredException e) {
                System.out.println(e.getMessage());
                return;
            }
            getCs().printDisplayTable("Staff Roster", displayList);
        } while ((choice = getCs().printChoices("Command // Corresponding Function", Arrays.asList("Sort by ID", "Sort by name", "Sort by title", "Sort by gender"), new String[] {"Go back"})) != -1);
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
        } catch (Restaurant.AssetNotRegisteredException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void manageStaff() {
        List<String> nameList;
        try {
            nameList = getStaffNames();
        } catch (Restaurant.AssetNotRegisteredException e) {
            System.out.println(e.getMessage());
            return;
        }

        int staffIndex = getCs().printChoices("Index // Staff Name", nameList, new String[]{"Go back"}) - 1;
        if (staffIndex == -2) {
            return;
        }

        int action = getCs().printChoices("Index // Action", new String[] {"Change name.", "Change title.", "Change gender.", "Remove STAFF from roster."}, new String[]{"Go back"});
        if (action == -1) {
            return;
        }

        if (action == 4) {
            getCs().printInstructions(new String[]{"Warning: This action cannot be undone.", "Y = Yes", "Any other input = NO"});
            if (getCs().getString("Confirm remove?").equalsIgnoreCase("Y")) {
                try {
                    removeStaff(staffIndex);
                } catch (Restaurant.AssetNotRegisteredException | IOException | Restaurant.FileIDMismatchException e) {
                    System.out.println(e.getMessage());
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
            } catch (Restaurant.AssetNotRegisteredException | IOException | Restaurant.FileIDMismatchException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
