package tools;

import client.enums.Op;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileIO {
    public enum FileNames {
        FOOD_FILE,
        PROMO_FILE,
        ORDER_FILE,
        BOOKING_FILE,
        TABLE_FILE,
        STAFF_FILE,
        SALES_FILE
    }

    private Map<FileNames, String> fileMap;

    public FileIO() {
        fileMap = new HashMap<>();
        FileNames[] fileEnumValues = FileNames.values();

        String[] fileNames = new String[] {"menu_items.txt", "promotion_items.txt", "order.txt", "reservations.txt", "tables.txt", "staff.txt", "revenue.txt"};
        for (int index = 0; index < FileNames.values().length; index++) {
            fileMap.put(fileEnumValues[index], fileNames[index]);
        }
    }

    private final String FILE_DIR = System.getProperty("user.dir") + "/DataStorage/";

    public Pair<Op, String> prepare() {
        Path path = Paths.get(FILE_DIR);

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                return (new Pair<>(Op.FAILED, "Failed to create directories."));
            }
        }

        for (String fileName : fileMap.values()) {
            path = Paths.get(FILE_DIR + fileName);

            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                } catch (FileAlreadyExistsException e) {
                    // ???
                } catch (IOException e) {
                    return (new Pair<>(Op.FAILED, "Failed to create files."));
                }
            }
        }

        return (new Pair<>(Op.SUCCESS, "Files OK."));
    }

    public List<String> read(FileNames fileName) {
        Path filePath = Paths.get(FILE_DIR + fileMap.get(fileName));
        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            return null;
        }
    }

    public Pair<Op, String> writeLine(FileNames fileName, String data) {
        Path filePath = Paths.get(FILE_DIR + fileMap.get(fileName));
        data += "\n";

        try {
            Files.write(filePath, data.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            return (new Pair<>(Op.FAILED, "Failed to write to file."));
        }

        return (new Pair<>(Op.SUCCESS, "Operation completed successfully."));
    }

    public Pair<Op, String> updateLine(FileNames fileName, int line, String data) {
        Path filePath = Paths.get(FILE_DIR + fileMap.get(fileName));

        try {
            List<String> fileData = Files.readAllLines(filePath);
            fileData.set(line, data);
            Files.write(filePath, fileData);
        } catch (IOException e) {
            return (new Pair<>(Op.FAILED, "Failed to write to file."));
        }
        return (new Pair<>(Op.SUCCESS, "Operation completed successfully."));
    }

    public Pair<Op, String> removeLine(FileNames fileName, int line) {
        Path filePath = Paths.get(FILE_DIR + fileMap.get(fileName));

        try {
            List<String> fileData = Files.readAllLines(filePath);
            fileData.remove(line);
            Files.write(filePath, fileData);
        } catch (IOException e) {
            return (new Pair<>(Op.FAILED, "Failed to write to file."));
        }
        return (new Pair<>(Op.SUCCESS, "Operation completed successfully."));
    }
}
