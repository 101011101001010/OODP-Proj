package enums;

import core.RestaurantData;
import menu.AlaCarteItem;
import menu.PromotionPackage;
import staff.Staff;
import tables.Order;
import tables.Table;

/**
 * Enumerator of restaurant data object types and their respective classes.
 */
public enum DataType {
    ALA_CARTE_ITEM(AlaCarteItem.class),
    PROMO_PACKAGE(PromotionPackage.class),
    TABLE(Table.class),
    STAFF(Staff.class),
    ORDER(Order.class),
    REVENUE(null);

    private Class<? extends RestaurantData> c;

    DataType(Class<? extends RestaurantData> c) {
        this.c = c;
    }

    /**
     * Obtains the class associated with a data type
     * @return class of data type object
     */
    public Class<? extends RestaurantData> getC() {
        return c;
    }
}
