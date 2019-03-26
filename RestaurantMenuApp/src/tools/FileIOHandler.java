package tools;

import constants.AppConstants;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class FileIOHandler {

    public static boolean buildDirectory() {
        try {
            Files.createDirectories(Paths.get(AppConstants.FILE_DIR));
            System.out.println(AppConstants.FILE_DIR);
        }

        catch (IOException e) {
            //App.printError(e, AppConstants.IO_ID + 1);
            return false;
        }

        for (String fileName : AppConstants.FILE_NAMES) {
            Path path = Paths.get(AppConstants.FILE_DIR + fileName + ".txt");

            try {
                Files.createFile(path);
            }

            catch (FileAlreadyExistsException ignored) {}

            catch (IOException e) {
                //App.printError(e, AppConstants.IO_ID + 2);
                return false;
            }
        }

        return true;
    }

    public static List<String> read(String file) {
        List<String> fileData = null;
        Path path = Paths.get(AppConstants.FILE_DIR + file.toLowerCase() + ".txt");

        try {
            fileData = Files.readAllLines(path);
        }

        catch (IOException e) {
            //App.printError(e, AppConstants.IO_ID + 3);
        }

        return fileData;
    }

    public static void write(String fileName, String data) {
        Path path = Paths.get(AppConstants.FILE_DIR + fileName.toLowerCase() + ".txt");
        data = data + "\n";

        try {
            Files.write(path, data.getBytes(), StandardOpenOption.APPEND);
        }

        catch (IOException e) {
            //App.printError(e, AppConstants.IO_ID + 4);
        }
    }

    public static void replace(String fileName, int line, String data) {
        List<String> fileData;
        Path path = Paths.get(AppConstants.FILE_DIR + fileName.toLowerCase() + ".txt");

        try {
            fileData = Files.readAllLines(path);
            fileData.set(line, data);
            Files.write(path, fileData);
        }

        catch (IOException e) {
            //App.printError(e, AppConstants.IO_ID + 5);
        }
    }

    public static void remove(String fileName, int line) {
        List<String> fileData;
        Path path = Paths.get(AppConstants.FILE_DIR + fileName.toLowerCase() + ".txt");

        try {
            fileData = Files.readAllLines(path);
            fileData.remove(line);
            Files.write(path, fileData);
        }

        catch (IOException e) {
            //App.printError(e, AppConstants.IO_ID + 6);
        }
    }

    public static boolean isFileExists(String fileName) {
        Path path = Paths.get(AppConstants.FILE_DIR + fileName.toLowerCase() + ".txt");

        try {
            Files.createFile(path);
            return false;
        }

        catch (FileAlreadyExistsException e) {
            return true;
        }

        catch (IOException e) {
            //App.printError(e, AppConstants.IO_ID + 7);
            return false;
        }
    }

    public static void deleteFile(String fileName) {
        Path path = Paths.get(AppConstants.FILE_DIR + fileName.toLowerCase() + ".txt");

        try {
            Files.deleteIfExists(path);
        }

        catch (IOException e) {
            //App.printError(e, AppConstants.IO_ID + 8);
        }
    }
}
