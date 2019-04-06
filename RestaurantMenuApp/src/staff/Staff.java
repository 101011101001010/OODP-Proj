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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getTitle() {
        return title;
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
