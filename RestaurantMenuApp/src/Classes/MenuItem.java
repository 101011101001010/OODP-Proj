package Classes;

import client.RestaurantAsset;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class MenuItem extends RestaurantAsset {
    private String name;
    private BigDecimal price;

    public MenuItem(int id, String name, BigDecimal price) {
        super(id);
        this.name = name;
        this.price = price;
        this.price = this.price.setScale(2, RoundingMode.FLOOR);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public abstract String toString();
    public abstract String toDisplayString();
}
