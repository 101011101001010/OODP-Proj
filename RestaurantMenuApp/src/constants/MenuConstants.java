package constants;

public class MenuConstants {
    public static final int PRINT_DEFAULT_LENGTH = 59;
    public static final int OPTIONS_LOWER_BOUND = 0;
    public static final int OPTIONS_TERMINATE = -1;

    public static final String APP_TITLE = "Restaurant Reservation and Point of Sale System (RRPSS)";

    public static final String MSG_ENTER_CHOICE = "Enter choice: ";
    public static final String MSG_INVALID_CHOICE = "Invalid choice.";
    public static final String MSG_GO_BACK = "Go Back";
    public static final String MSG_MAIN_MENU = "Main Menu";
    public static final String MSG_EXIT_PROGRAM = "Exit Program";
    public static final String MSG_CONTINUE = "Enter anything to continue...";

    public static final String MENU_HANDLER_CLASS_PREFIX = "console.MenuHandler_";
    public static final String MENU_HANDLER_CALL_METHOD = "start";

    //////////////////
    /* MENU OPTIONS */
    //////////////////
    public static final String[] OPTIONS_MAIN = {
            "Food Menu",
            "Promotion Packages",
            "Orders & Invoice",
            "Bookings",
            "Tables",
            "Staff",
            "Print Revenue"
    };

    public static final String[] MENU_ACTIONS = {
            "View",
            "Create",
            "Update",
            "Remove"
    };

    public static final String MENU_HEADER_0 = OPTIONS_MAIN[0];
    public static final String[] MENU_SUB_0 = {
            MENU_HEADER_0 + " - " + MENU_ACTIONS[0],
            MENU_HEADER_0 + " - " + MENU_ACTIONS[1],
            MENU_HEADER_0 + " - " + MENU_ACTIONS[2],
            MENU_HEADER_0 + " - " + MENU_ACTIONS[3]
    };
    public static final String[] MENU_SORT_ACTIONS_0 = {
            "Sort by Name",
            "Sort by Price",
            "Sort by Category"
    };

    public static final String MENU_HEADER_1 = OPTIONS_MAIN[1];
    public static final String[] MENU_SUB_1 = {
            MENU_HEADER_1 + " - " + MENU_ACTIONS[0],
            MENU_HEADER_1 + " - " + MENU_ACTIONS[1],
            MENU_HEADER_1 + " - " + MENU_ACTIONS[2],
            MENU_HEADER_1 + " - " + MENU_ACTIONS[3]
    };

    public static final String MENU_HEADER_4 = OPTIONS_MAIN[4];
    public static final String[] MENU_SUB_4 = {
            MENU_HEADER_4 + " - " + MENU_ACTIONS[0],
            MENU_HEADER_4 + " - " + MENU_ACTIONS[1],
            MENU_HEADER_4 + " - " + MENU_ACTIONS[2],
            MENU_HEADER_4 + " - " + MENU_ACTIONS[3]
    };
}
