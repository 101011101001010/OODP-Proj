package tools;

import enums.DataType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileIO {
    private final String FILE_DIR = System.getProperty("user.dir") + "/DataStorage/";

    public FileIO() throws IOException {
        Path dirPath = Paths.get(FILE_DIR);

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw (new IOException("Failed to create directories: " + e.getMessage()));
            }
        }
    }

    public List<String> read(DataType dataType) throws IOException {
        return read(dataType.name());
    }

    public List<String> read(String fileName) throws IOException {
        try {
            return Files.readAllLines(getPath(fileName));
        } catch (IOException e) {
            throw (new IOException("File IO error when attempting to read file '" + fileName + "': " + e.getMessage()));
        }
    }

    public void writeLine(String fileName, String text) throws IOException {
        try {
            createFileIfNotExists(fileName);
            Path filePath = getPath(fileName);
            text += "\n";
            Files.write(filePath, text.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw (new IOException("File IO error when attempting to write to file for '" + fileName + "': " + e.getMessage()));
        }
    }

    public void updateLine(String fileName, int line, String text) throws IOException {
        try {
            createFileIfNotExists(fileName);
            Path filePath = getPath(fileName);
            List<String> fileData = Files.readAllLines(filePath);
            fileData.set(line, text);
            Files.write(filePath, fileData);
        } catch (IOException e) {
            throw (new IOException("File IO error when attempting to update line for '" + fileName + "', line + " + line + ": " + e.getMessage()));
        }
    }

    public void removeLine(String fileName, int line) throws IOException {
        try {
            createFileIfNotExists(fileName);
            Path filePath = getPath(fileName);
            List<String> fileData = Files.readAllLines(filePath);
            fileData.remove(line);
            Files.write(filePath, fileData);
        } catch (IOException e) {
            throw (new IOException("File IO error when attempting to remove line for '" + fileName + "', line + " + line + ": " + e.getMessage()));
        }
    }

    public void clearFile(String fileName) throws IOException {
        try {
            createFileIfNotExists(fileName);
            Path filePath = getPath(fileName);
            List<String> fileData = new ArrayList<>();
            Files.write(filePath, fileData);
        } catch (IOException e) {
            throw (new IOException("File IO error when attempting to clear file for '" + fileName + "': " + e.getMessage()));
        }
    }

    private Path getPath(String fileName) {
        String FILE_EXT = ".txt";
        return Paths.get(FILE_DIR + fileName.toLowerCase() + FILE_EXT);
    }

    private void createFileIfNotExists(String fileName) throws IOException {
        Path filePath = getPath(fileName);

        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw (new IOException("Failed to create file for '" + fileName + "': " + e.getMessage()));
            }
        }
    }

    static void logToFile(String text, Exception exception) {
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
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            pw.close();
            Files.write(path, (sw.toString() + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            ConsolePrinter.printMessage(ConsolePrinter.MessageType.FAILED, "Failed to log to file: " + e.getMessage());
        }
    }
}
