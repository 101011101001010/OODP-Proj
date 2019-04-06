package tools;

import java.util.Collections;

public class Log {
    public static void notice(String message) {
        System.out.println();
        System.out.println("\t[Notice] " + message);
    }

    public static void warning(String message) {
        System.out.println();
        System.out.println("\t[Warning] " + message);
    }
}
