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

/**
 * Handles input / output for text files in plain text format.
 * Each data type defined in the enumerator will have a text files dedicated to it.
 * File names could also be specified to allow IO to other text files.
 * Files are stored in the source directory under the folder DataStorage.
 * External files will need to be stored in this directory as well.
 */
public class FileIO {
    /**
     * File path constant for use in this class.
     */
    private final String FILE_DIR = System.getProperty("user.dir") + "/DataStorage/";

    /**
     * Create the storage directory if it does not exist.
     * @throws IOException Thrown if the function fails to check if the directory exists or if there is an error when creating the directory.
     */
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

    /**
     * Reads in data from a pre-defined data text file in the form of a list of strings. Each line in the file is saved as a list entry.
     * @param dataType Data type file to read from
     * @return List of lines from the text file
     * @throws IOException Thrown if the function fails to check if the file exists or if there is an error in the read process.
     */
    public List<String> read(DataType dataType) throws IOException {
        return read(dataType.name());
    }

    /**
     * Reads in data from a user-specified text file in the form of a list of strings. Each line in the file is saved as a list entry.
     * @param fileName Text file to read from.
     * @return List of lines from the text file.
     * @throws IOException Thrown if the function fails to check if the file exists or if there is an error in the read process.
     */
    public List<String> read(String fileName) throws IOException {
        try {
            return Files.readAllLines(getPath(fileName));
        } catch (IOException e) {
            throw (new IOException("File IO error when attempting to read file '" + fileName + "': " + e.getMessage()));
        }
    }

    /**
     * Writes a line of data into the specified text file.
     * @param fileName Text file to write to.
     * @param text Line of text to write.
     * @throws IOException Thrown if the function fails to check if the file exists or if there is an error in the write process.
     */
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

    /**
     * Replaces a line of data in the specified text file.
     * @param fileName Text file to update.
     * @param line Line index to replace.
     * @param text Replacement text.
     * @throws IOException Thrown if the function fails to check if the file exists or if there is an error in the read or write process.
     */
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

    /**
     * Removes a line of data in the specified text file.
     * @param fileName Text file to remove data from.
     * @param line Line index to remove.
     * @throws IOException Thrown if the function fails to check if the file exists or if there is an error in the read or write process.
     */
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

    /**
     * Clears the specified text file of all data.
     * @param fileName Text file to clear data from.
     * @throws IOException Thrown if the function fails to check if the file exists or if there is an error in the write process.
     */
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

    /**
     * Formats the path string for use in the other functions by appending the directory and extension.
     * @param fileName Text file name to be formatted into the string
     * @return Formatted path
     */
    private Path getPath(String fileName) {
        String FILE_EXT = ".txt";
        return Paths.get(FILE_DIR + fileName.toLowerCase() + FILE_EXT);
    }

    /**
     * Creates the specified file if it does not exist.
     * @param fileName File name to create or check
     * @throws IOException Thrown if the function fails to check if the file exists or if there is an error when creating the file.
     */
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

    /**
     * Static method for logging exceptions or messages to a text file. The data is logged into 'log.txt' in the DataStorage directory.
     * @param text Additional text to log
     * @param exception Exception to log
     */
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
