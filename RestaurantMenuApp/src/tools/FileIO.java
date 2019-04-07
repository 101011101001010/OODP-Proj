package tools;

import enums.DataType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileIO {
    private final String FILE_DIR = System.getProperty("user.dir") + "/DataStorage/";
    private final String FILE_EXT = ".txt";

    public FileIO() {
        Path dirPath = Paths.get(FILE_DIR);

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to create directories: " + e.getMessage());
                return;
            }
        }

        for (DataType dataType : DataType.values()) {
            Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
            if (!Files.exists(filePath)) {
                try {
                    Files.createFile(filePath);
                } catch (IOException e) {
                    ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to create file for " + dataType.name() + ": " + e.getMessage());
                    return;
                }
            }
        }
    }

    public List<String> read(DataType dataType) {
        try {
            Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "File IO error when attempting to read file for " + dataType.name() + ": " + e.getMessage());
            return null;
        }
    }

    public boolean writeLine(DataType dataType, String text) {
        try {
            Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
            text += "\n";
            Files.write(filePath, text.getBytes(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "File IO error when attempting to write to file for " + dataType.name() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateLine(DataType dataType, int line, String text) {
        try {
            Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
            List<String> fileData = Files.readAllLines(filePath);
            fileData.set(line, text);
            Files.write(filePath, fileData);
            return true;
        } catch (IOException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "File IO error when attempting to update line on file for " + dataType.name() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean removeLine(DataType dataType, int line) {
        try {
            Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
            List<String> fileData = Files.readAllLines(filePath);
            fileData.remove(line);
            Files.write(filePath, fileData);
            return true;
        } catch (IOException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "File IO error when attempting to update line on file for " + dataType.name() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean clearFile(DataType dataType) {
        try {
            Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
            List<String> fileData = new ArrayList<>();
            Files.write(filePath, fileData);
            return true;
        } catch (IOException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "File IO error when attempting to clear file for " + dataType.name() + ": " + e.getMessage());
            return false;
        }
    }

    static void logToFile(String text) {
        final String FILE_DIR = System.getProperty("user.dir") + "/DataStorage/";
        Path path = Paths.get(FILE_DIR);

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to create directory for log: " + e.getMessage());
                return;
            }
        }

        path = Paths.get(FILE_DIR + "log.txt");

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to create file for log: " + e.getMessage());
                return;
            }
        }

        try {
            text += "\n";
            Files.write(path, text.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to log to file: " + e.getMessage());
        }
    }
}
