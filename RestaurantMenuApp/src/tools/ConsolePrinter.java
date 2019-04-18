package tools;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ConsolePrinter {
    public enum MessageType {
        SUCCESS,
        FAILED,
        WARNING,
        ERROR,
        FATAL
    }

    public static void sendWelcome(List<String[]> mainCLIOptions) {
        final List<String> optionsList = new ArrayList<>();

        for (String[] options : mainCLIOptions) {
            optionsList.addAll(Arrays.asList(options));
            optionsList.add("---");
        }

        optionsList.remove(optionsList.size() - 1);
        String[] footerOptions = new String[] {"Switch staff account", "Exit program"};
        final List<String> mainDisplay = formatChoiceList(optionsList, Arrays.asList(footerOptions));
        printTable("Scam Money Restaurant", "Command // Function", mainDisplay, true);
    }

    public static List<String> formatChoiceList(List<String> options, List<String> footerOptions) {
        if (options == null) {
            return null;
        }

        if (footerOptions == null) {
            footerOptions= Collections.singletonList("Go back");
        }

        List<String> mergedOptions = new ArrayList<>();
        int optionsIndex = 1;

        for (String option : options) {
            if (option.equals("---") || option.startsWith("\\SUB")) {
                mergedOptions.add(option);
                continue;
            }

            mergedOptions.add(optionsIndex + " // " + option);
            optionsIndex++;
        }

        mergedOptions.add("---");
        for (int index = 0; index < footerOptions.size(); index++) {
            mergedOptions.add((0 - index) + " // " + footerOptions.get(index));
        }

        return mergedOptions;
    }

    public static void printInstructions(List<String> instructionList) {
        printTable("", "", instructionList, false);
    }

    public static void printTable(String columnHeaders, List<String> stringList, boolean verticalDivider) {
        printTable("", columnHeaders, stringList, verticalDivider);
    }

    public static void printTable(String title, String columnHeaders, List<String> stringList, boolean verticalDivider) {
        if (stringList == null || stringList.size() == 0) {
            return;
        }

        List<String> stringListCopy = new ArrayList<>(stringList);

        if (columnHeaders.length() > 0) {
            stringListCopy.add(0, columnHeaders);
        }

        final List<Integer> cellLengths = calculateCellLengths(title, stringListCopy);
        final List<String[]> stringListProcessed = stringListCopy.stream().map(s -> s.split(" // ")).collect(Collectors.toList());
        final int totalLength = cellLengths.stream().mapToInt(Integer::intValue).sum() + (5 * (cellLengths.size() - 1));

        if (title.length() > 0) {
            printTitle(title, totalLength);
        } else {
            System.out.println();
            printDivider('=', totalLength);
        }

        boolean horizontalDivider = false;

        for (int row = 0; row < stringListProcessed.size(); row++) {
            String[] rowString = stringListProcessed.get(row);
            final List<List<String>> rowStringList = processRowString(rowString, cellLengths);
            if (rowStringList.size() == 0) {
                continue;
            }

            int maxRowCount = rowStringList.get(0).size();
            for (int cellRow = 0; cellRow < maxRowCount; cellRow++) {
                if (rowStringList.size() == 1 && rowStringList.get(0).size() == 1 && rowStringList.get(0).get(0).equals("---")) {
                    printDivider('-', cellLengths, verticalDivider);
                    break;
                }

                if (rowStringList.size() == 1 && rowStringList.get(0).size() == 1 && rowStringList.get(0).get(0).startsWith("\\SUB")) {
                    String s = rowStringList.get(0).get(0);
                    s = s.replace("\\SUB", "");
                    s = s.toUpperCase();
                    final String stringPadding = " ".repeat(totalLength - s.length());

                    if (((row > 0 && columnHeaders.length() == 0) || (row > 1)) && !horizontalDivider) {
                        printDivider('-', cellLengths, verticalDivider);
                    }

                    System.out.println("|  " + s + stringPadding + "  |");
                    printDivider('-', cellLengths, verticalDivider);
                    break;
                }

                System.out.print("|  ");

                for (int cell = 0; cell < cellLengths.size(); cell++) {
                    String cellRowString = rowStringList.get(cell).get(cellRow);
                    final int cellLength = cellLengths.get(cell);
                    final int stringLength = cellRowString.length();
                    final String stringPadding = " ".repeat(cellLength - stringLength);

                    if (row == 0 && columnHeaders.length() > 0) {
                        final int pad = Math.max(cellLength - stringLength, 0);
                        System.out.print(" ".repeat(pad / 2) + cellRowString.toUpperCase() + " ".repeat(pad - (pad / 2)));
                    } else {
                        if (cellRowString.matches("^(-?\\d+|-?\\d+\\.\\d+)$")) {
                            final int pad = Math.max(cellLength - stringLength, 0);
                            System.out.print(" ".repeat(pad - (pad / 2)) + cellRowString + " ".repeat(pad / 2));
                            //System.out.print(stringPadding + cellRowString);
                        } else {
                            System.out.print(cellRowString + stringPadding);
                        }
                    }

                    if (cell != cellLengths.size() - 1) {
                        if (verticalDivider) {
                            System.out.print("  |  ");
                        } else {
                            System.out.print(" ".repeat(5));
                        }
                    }
                }

                System.out.println("  |");
            }

            if (maxRowCount > 1) {
                horizontalDivider = true;
            }

            if ((columnHeaders.length() > 0 && row == 0) || (horizontalDivider && row != stringListProcessed.size() - 1)) {
                printDivider('-', cellLengths, verticalDivider);
            }
        }

        printDivider('=', totalLength);
    }

    private static List<Integer> calculateCellLengths(String title, List<String> stringList) {
        List<int[]> lengthPerRowList = stringList.stream().map(s -> s.split(" // ")).map(s -> Arrays.stream(s).mapToInt(String::length).toArray()).collect(Collectors.toList());
        int maxCellCount = lengthPerRowList.stream().mapToInt(v -> v.length).max().orElse(0);
        List<Integer> cellLengths = new ArrayList<>(Collections.nCopies(maxCellCount, 0));

        for (int[] lengths : lengthPerRowList) {
            for (int cell = 0; cell < maxCellCount; cell++) {
                int cellLength = (cell < lengths.length)? lengths[cell] : 0;
                cellLengths.set(cell, Math.max(cellLengths.get(cell), cellLength));
            }
        }

        int MAX_LENGTH = 80;
        int maxLength = Math.max(title.length(), MAX_LENGTH);
        int totalStringSpace = maxLength - (5 * (cellLengths.size() - 1));
        int totalRowLength = cellLengths.stream().mapToInt(Integer::intValue).sum();

        if (totalRowLength > totalStringSpace) {
            int theoreticalMaxLength = totalStringSpace / cellLengths.size();
            int bigCellsCount = (int) cellLengths.stream().filter(i -> i > theoreticalMaxLength).count();
            int totalSpaceForBigCells = totalStringSpace - cellLengths.stream().filter(i -> i <= theoreticalMaxLength).mapToInt(Integer::intValue).sum();
            int maxLengthPerBigCell = totalSpaceForBigCells / bigCellsCount;

            for (int cell = 0; cell < cellLengths.size(); cell++) {
                if (cellLengths.get(cell) > theoreticalMaxLength) {
                    cellLengths.set(cell, maxLengthPerBigCell);
                }
            }
        }

        totalRowLength = cellLengths.stream().mapToInt(Integer::intValue).sum();
        int extraSpace = totalStringSpace - totalRowLength;
        int index = 0;

        while (extraSpace > 0) {
            cellLengths.set(index, cellLengths.get(index) + 1);
            extraSpace--;
            index++;

            if (index >= cellLengths.size()) {
                index = 0;
            }
        }

        return cellLengths;
    }

    private static List<List<String>> processRowString(String[] rowString, List<Integer> cellLengths) {
        final List<List<String>> ret = new ArrayList<>();
        int maxRowCount = 0;

        for (int cell = 0; cell < cellLengths.size(); cell++) {
            final List<String> cellStringList = new ArrayList<>();
            if (rowString.length == 1 && (rowString[0].equals("---") || rowString[0].startsWith("\\SUB"))) {
                cellStringList.add(rowString[0]);
                ret.add(cellStringList);
                return ret;
            }

            if (cell < rowString.length) {
                final List<String> processedString = processCellString(rowString[cell], cellLengths.get(cell));
                if (processedString.size() > maxRowCount) {
                    maxRowCount = processedString.size();
                }

                cellStringList.addAll(processedString);
            }

            ret.add(cellStringList);
        }

        for (int cell = 0; cell < ret.size(); cell++) {
            final List<String> cellStringList = ret.get(cell);
            for (int index = cellStringList.size(); index < maxRowCount; index++) {
                final String paddedString = " ".repeat(cellLengths.get(cell));
                cellStringList.add(paddedString);
            }
        }

        return ret;
    }

    private static List<String> processCellString(String cellString, int cellLength) {
        final List<String> cellWords = new ArrayList<>();
        Arrays.stream(cellString.trim().replace("\n", "\n\\\\").split("\n")).forEach(s -> cellWords.addAll(Arrays.asList(s.split(" "))));
        int trailSpace = 0;
        StringBuilder longStrings = new StringBuilder();
        final List<String> ret = new ArrayList<>();

        for (String word : cellWords) {
            if (word.length() <= cellLength) {
                if (longStrings.length() > 0) {
                    ret.addAll(subString(longStrings.toString(), cellLength, trailSpace));
                    trailSpace = 0;
                    longStrings = new StringBuilder();
                }

                int listSize = ret.size();
                if ((listSize > 0) && (ret.get(listSize - 1).length() + word.length() < cellLength) && (!word.startsWith("\\\\"))) {
                    ret.set(listSize - 1, ret.get(listSize - 1) + " " + word);
                } else {
                    word = word.replace("\\\\", "");
                    ret.add(word);
                    trailSpace = Math.max(cellLength - word.length() - 1, 0);
                }
            } else {
                word = word.replace("\\\\", "");
                longStrings.append(word).append(" ");
            }
        }

        if (longStrings.length() > 0) {
            ret.addAll(subString(longStrings.toString(), cellLength, trailSpace));
        }

        return ret;
    }

    private static List<String> subString(String s, int maxLength, int trailSpace) {
        final List<String> ret = new ArrayList<>();

        if (s.length() <= maxLength) {
            ret.add(s);
            return ret;
        }

        if (trailSpace != 0) {
            String s1 = s.substring(0, trailSpace);
            ret.add(s1);

            String s2 = s.substring(trailSpace);
            ret.addAll(subString(s2, maxLength, 0));
            return ret;
        } else {
            String s1 = s.substring(0, maxLength);
            ret.add(s1);

            String s2 = s.substring(maxLength);
            ret.addAll(subString(s2, maxLength, 0));
            return ret;
        }
    }

    private static void printTitle(String title, int length) {
        final int pad = Math.max(length - title.length(), 0);
        System.out.println();
        printDivider('=', length);
        System.out.println("|  " + " ".repeat(pad / 2) + title.toUpperCase() + " ".repeat(pad - (pad / 2)) + "  |");
        printDivider('=', length);
    }

    private static void printDivider(char c, int length) {
        System.out.println("|" + (c + "").repeat(length + 4) + "|");
    }

    private static void printDivider(char c, List<Integer> cellLengths, boolean verticalDivider) {
        System.out.print("|");

        for (int column = 0; column < cellLengths.size(); column++) {
            int length = cellLengths.get(column);
            System.out.print((c + "").repeat(length + 4));

            if (column != (cellLengths.size() - 1)) {
                System.out.print(verticalDivider? "|" : c);
            }
        }

        System.out.println("|");
    }

    public static void clearCmd() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ignored) {}
    }

    public static void printMessage(MessageType messageType, String message) {
        clearCmd();

        message = "\n\t[" + messageType.name() + "] " + message;
        System.out.println(message);
    }

    public static void logToFile(String message, Exception e) {
        printMessage(MessageType.ERROR, message);
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String dateTime = LocalDateTime.now().format(dateTimeFormat);
        FileIO.logToFile(dateTime + ": " + message, e);
    }


}
