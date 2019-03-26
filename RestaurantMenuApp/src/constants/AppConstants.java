package constants;

public class AppConstants {

    public static final int MENU_ID = 0;
    public static final int PROMO_ID = 100;
    public static final int IO_ID = 1000;

    public static final String FILE_DIR = System.getProperty("user.dir")+"\\DataStorage\\";

    public static final String FILE_PROMO= "promo";
    public static final String FILE_STAFF = "staff";

    public static final String[] CODE_NAMES2 = {
            "items.FoodMenu",
            "items.Promo",
            "orders.Order",
            "tables.Booking",
            "staff.Staff",
            "sales.Sales"
    };

    public static final String[] CODE_NAMES = {
            "Classes.MenuManager",
            "Classes.PromotionManager",
            "Classes.OrderManager",
            "Classes.ReservationManager",
            "Classes.TableManager",
            "Classes.Staff",
            "Classes.Sales"
    };

    public static final String[] FILE_NAMES = {
        "menu_items",
        "promotion_items",
        "order",
        "reservations",
        "staff",
        "revenue"
    };

}
