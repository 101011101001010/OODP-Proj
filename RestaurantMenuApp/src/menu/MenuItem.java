package menu;

import core.RestaurantData;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MenuItem extends RestaurantData {
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

    public String toFileString() {
        return (getId() + " // " + name + " // " + price);
    }

    public String toDisplayString() {
        return (name + " // " + price);
    }


}
