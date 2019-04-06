package tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    public static void notice(String message) {
        message = "\n\t[Notice] " + message;
        System.out.println(message);
    }

    public static void warning(String message) {
        message = "\n\t[Warning] " + message;
        System.out.println(message);
        writeToFile(message);
    }

    public static void error(String message) {
        message = "\n\t[Error] " + message;
        System.out.println(message);
        writeToFile(message);
    }

    public static void writeToFile(String message) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String dateTime = LocalDateTime.now().format(dateTimeFormat);
        FileIO.logToFile(dateTime + ": " + message);
    }
}
