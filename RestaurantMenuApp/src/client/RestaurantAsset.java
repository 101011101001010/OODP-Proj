package client;

public abstract class RestaurantAsset {
    private int id;

    public RestaurantAsset(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract String toPrintString();
    public abstract String toTableString();
}
