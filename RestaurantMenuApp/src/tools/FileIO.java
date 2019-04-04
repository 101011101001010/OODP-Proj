package tools;

import enums.AssetType;
import enums.FileName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileIO {
    private final String FILE_DIR = System.getProperty("user.dir") + "/DataStorage/";
    private final String FILE_EXT = ".txt";

    public void checkFiles() throws IOException {
        Path path = Paths.get(FILE_DIR);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        for (FileName fileName : FileName.values()) {
            path = Paths.get(FILE_DIR + fileName.name().toLowerCase() + FILE_EXT);

            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        }
    }

    public List<String> read(FileName fileName) throws IOException {
        return Files.readAllLines(Paths.get(FILE_DIR + fileName.name().toLowerCase() + FILE_EXT));
    }

    public void writeLine(FileName fileName, String data) throws IOException {
        Path filePath = Paths.get(FILE_DIR + fileName.name().toLowerCase() + FILE_EXT);
        data += "\n";
        Files.write(filePath, data.getBytes(), StandardOpenOption.APPEND);
    }

    public void updateLine(FileName fileName, int line, String data) throws IOException {
        Path filePath = Paths.get(FILE_DIR + fileName.name().toLowerCase() + FILE_EXT);
        List<String> fileData = Files.readAllLines(filePath);
        fileData.set(line, data);
        Files.write(filePath, fileData);
    }

    public void removeLine(FileName fileName, int line) throws IOException {
        Path filePath = Paths.get(FILE_DIR + fileName.name().toLowerCase() + FILE_EXT);
        List<String> fileData = Files.readAllLines(filePath);
        fileData.remove(line);
        Files.write(filePath, fileData);
    }
}
