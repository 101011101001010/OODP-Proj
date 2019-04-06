package staff;

import client.RestaurantData;

public class Staff extends RestaurantData {
    private String name;
    private String title;

    Staff(int id, String name, String title) {
        super(id);
        this.name = name;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    void update(String name, String title) {
        this.name = name;
        this.title = title;
    }

    @Override
    public String toFileString() {
        return getId() + " // " + getName() + " // " + getTitle();
    }

    @Override
    public String toDisplayString() {
        return toFileString();
    }
}
