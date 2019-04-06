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

    public void checkFiles() throws IOException {
        Path path = Paths.get(FILE_DIR);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        for (DataType dataType : DataType.values()) {
            path = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);

            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        }
    }

    public List<String> read(DataType dataType) throws IOException {
        return Files.readAllLines(Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT));
    }

    public void writeLine(DataType dataType, String data) throws IOException {
        Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
        data += "\n";
        Files.write(filePath, data.getBytes(), StandardOpenOption.APPEND);
    }

    public void updateLine(DataType dataType, int line, String data) throws IOException {
        Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
        List<String> fileData = Files.readAllLines(filePath);
        fileData.set(line, data);
        Files.write(filePath, fileData);
    }

    public void removeLine(DataType dataType, int line) throws IOException {
        Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
        List<String> fileData = Files.readAllLines(filePath);
        fileData.remove(line);
        Files.write(filePath, fileData);
    }

    public void clearFile(DataType dataType) throws IOException {
        Path filePath = Paths.get(FILE_DIR + dataType.name().toLowerCase() + FILE_EXT);
        Files.write(filePath, new ArrayList<String>());
    }
}
