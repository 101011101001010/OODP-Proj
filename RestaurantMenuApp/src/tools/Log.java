package tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    public static void notice(String message) {
        System.out.println();
        System.out.println("\t[Notice] " + message);
    }

    public static void warning(Object o, String message) {
        System.out.println();
        System.out.println("\t[Warning] " + message);
        toFile("<" + o.getClass().getSimpleName() + "> [Warning] " + message);
    }

    public static void error(Object o, String message) {
        System.out.println();
        System.out.println("\t[Error] " + message);
        toFile("<" + o.getClass().getSimpleName() + "> [Error] " + message);
    }


    private static void toFile(String text) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String dateTime = LocalDateTime.now().format(format);
        (new FileIO()).log(dateTime + ": " + text);
    }
}
