package menu;

import client.RestaurantAsset;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MenuItem extends RestaurantAsset {
    private String name;
    private BigDecimal price;

    MenuItem(int id, String name, BigDecimal price) {
        super(id);
        this.name = name;
        this.price = price;
        this.price = this.price.setScale(2, RoundingMode.FLOOR);
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String toPrintString() {
        return (getId() + " // " + name + " // " + price);
    }

    public String toTableString() {
        return (getId() + " // " + name + " // " + price);
    }
}
