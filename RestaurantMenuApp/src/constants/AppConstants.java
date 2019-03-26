package constants;

public class AppConstants {

    public static final String FILE_DIR = System.getProperty("user.dir") + "/DataStorage/";
    public static final String FILE_SEPARATOR = ", ";

    public static final String[] CODE_NAMES = {
            "Classes.MenuManager",
            "Classes.PromotionManager",
            "Classes.OrderManager",
            "Classes.ReservationManager",
            "Classes.TableManager",
            "Classes.StaffManager",
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
