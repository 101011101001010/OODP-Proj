package core;

public abstract class RestaurantData {
    private int id;

    public RestaurantData(int id) {
        this.id = id;
    }

    public boolean matchId(int id) {
        return (this.id == id);
    }

    public int getId() {
        return id;
    }

    public abstract String toFileString();
    public abstract String toDisplayString();
}
