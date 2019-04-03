package staff;

import client.RestaurantAsset;

public class Staff extends RestaurantAsset {
    private String name;
    private String title;
    private String gender;

    Staff(int id, String name, String title, String gender) {
        super(id);
        this.name = name;
        this.title = title;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getGender() {
        return gender;
    }

    void update(String name, String title, String gender) {
        this.name = name;
        this.title = title;
        this.gender = gender;
    }

    @Override
    public String toPrintString() {
        return getId() + " // " + getName() + " // " + getTitle() + " // " + getGender();
    }

    @Override
    public String toTableString() {
        return toPrintString();
    }
}
