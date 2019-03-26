package tools;

import constants.MenuConstants;
import tools.ScannerHandler;

import java.util.List;

public class MenuFactory {

    public static void printMenu(String title, String[] options) {
        System.out.println();
        printMenuHeader(title);

        for (int i = 0; i < options.length; i++) {
            if (title.equals(MenuConstants.APP_TITLE)) {
                //if (i == 0 || i == 4) {
                    printMenuItems(i, options[i]);
                //}
            } else {
                printMenuItems(i, options[i]);
            }
        }

        printDivider('-');

        if (title.equals(MenuConstants.APP_TITLE)) {
            printMenuItems(-1, MenuConstants.MSG_EXIT_PROGRAM);
        } else {
            printMenuItems(-1, MenuConstants.MSG_GO_BACK);
            printMenuItems(-2, MenuConstants.MSG_MAIN_MENU);
        }

        printDivider('=');
        System.out.println();
    }


    public static void printMenu(String title, List<String> options) {
        System.out.println();
        printMenuHeader(title);

        for (int i = 0; i < options.size(); i++) {
            if (title.equals(MenuConstants.APP_TITLE)) {
                //if (i == 0 || i == 4) {
                printMenuItems(i, options.get(i));
                //}
            } else {
                printMenuItems(i, options.get(i));
            }
        }

        printDivider('-');

        if (title.equals(MenuConstants.APP_TITLE)) {
            printMenuItems(-1, MenuConstants.MSG_EXIT_PROGRAM);
        } else {
            printMenuItems(-1, MenuConstants.MSG_GO_BACK);
            printMenuItems(-2, MenuConstants.MSG_MAIN_MENU);
        }

        printDivider('=');
        System.out.println();
    }

    public static void printMenuX(String title, String[] options) {
        String[] data;
        System.out.println();
        printMenuHeader(title);

        for (int i = 0; i < options.length; i++) {
            data = options[i].split("\n");
            printMenuItems(i, data[0]);

            for (int j = 1; j < data.length; j++) {
                printMenuItems(data[j]);
            }

            if (i != (options.length - 1)) {
                printMenuItems("");
            }
        }

        if (options.length > 0) {
            printDivider('-');
        }

        int terminatingInput = MenuConstants.OPTIONS_TERMINATE;
        printMenuItems(terminatingInput, MenuConstants.MSG_GO_BACK);
        printMenuItems((terminatingInput - 1), MenuConstants.MSG_MAIN_MENU);

        printDivider('=');
        System.out.println();
    }

    public static void printMenuX(String title, List<String> options) {
        String[] data;
        System.out.println();
        printMenuHeader(title);

        for (int i = 0; i < options.size(); i++) {
            data = options.get(i).split("\n");
            printMenuItems(i, data[0]);

            for (int j = 1; j < data.length; j++) {
                printMenuItems(data[j]);
            }

            if (i != (options.size() - 1)) {
                printMenuItems("");
            }
        }

        if (options.size() > 0) {
            printDivider('-');
        }

        int terminatingInput = MenuConstants.OPTIONS_TERMINATE;
        printMenuItems(terminatingInput, MenuConstants.MSG_GO_BACK);
        printMenuItems((terminatingInput - 1), MenuConstants.MSG_MAIN_MENU);

        printDivider('=');
        System.out.println();
    }

    public static int loopChoice(ScannerHandler sc, int upperBoundPlusOne) {
        int choice;
        int terminatingInput = MenuConstants.OPTIONS_TERMINATE;
        int lowerBound = MenuConstants.OPTIONS_LOWER_BOUND;

        do {
            choice = sc.getInt(MenuConstants.MSG_ENTER_CHOICE);

            if (choice == terminatingInput || choice == (terminatingInput - 1)) {
                return choice;
            }

            if (choice < lowerBound || choice > (upperBoundPlusOne - 1)) {
                System.out.println(MenuConstants.MSG_INVALID_CHOICE);
            }
        } while (choice < lowerBound || choice > (upperBoundPlusOne - 1));

        return choice;
    }

    //////////////////////////
    /* SUPPORTING FUNCTIONS */
    //////////////////////////
    private static void printDivider(char character) {
        System.out.print("|");
        for (int i = 1; i < MenuConstants.PRINT_DEFAULT_LENGTH - 1; i++) {
            System.out.print(character);
        }
        System.out.println("|");
    }

    private static void printMenuHeader(String text) {
        printDivider('=');
        System.out.printf(getHeaderFormat(), text);
        System.out.println();
        printDivider('=');
    }

    private static void printMenuItems(String text) {
        System.out.printf(getStringFormat(), "|", text, "|");
        System.out.println();
    }

    private static void printMenuItems(int index, String text) {
        System.out.printf(getStringFormat(), "| [" + index + "]", text, "|");
        System.out.println();
    }

    private static String getHeaderFormat() {
        int pad = (MenuConstants.PRINT_DEFAULT_LENGTH - 4);
        return ("| %1$-" + pad + "s |");
    }

    private static String getStringFormat() {
        int pad = (MenuConstants.PRINT_DEFAULT_LENGTH - 11);
        return ("%1$-8s %2$-" + pad + "s %3$s");
    }
}
