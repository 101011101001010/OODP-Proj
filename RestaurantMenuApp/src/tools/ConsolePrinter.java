package tools;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ConsolePrinter {
    /**
     * Message types for printed messages.
     */
    public enum MessageType {
        SUCCESS,
        FAILED,
        WARNING,
        ERROR,
        FATAL
    }

    /**
     * CLI formatting for the main options.
     * @param mainCLIOptions list of string arrays obtained from the different managers in main
     */
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

    /**
     * Adds a command index to every option for users to select. If footerOptions is supplied (not null), footer options will be formatted in with descending indices starting from 0 inclusive.
     * @param options List of option strings to be formatted
     * @param footerOptions Additional footer options to be formatted, usually used for options to 'go back'.
     *                      May be null.
     *                      If null, the default footer option will be 'Go back' (go up one menu level).
     * @return List of formatted strings with command indices.
     */
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

    /**
     * Wrapper that prints a table of instructions without any dividers.
     * Primarily used to convey instructions to users.
     * @param instructionList List of instruction strings to be printed.
     */
    public static void printInstructions(List<String> instructionList) {
        printTable("", "", instructionList, false);
    }

    /**
     * Wrapper for printing a table without a title. See the full printTable function for more details.
     * @param columnHeaders The first row, 'header row', for the table.
     * @param stringList List of strings to be printed, post-formatted with column division symbols. See print formatting documentation for more details.
     * @param verticalDivider Specifies if column dividers are to be printed in the table.
     */
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
            boolean header = false;

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

                    if (cellRowString.startsWith("\\SUB")) {
                        cellRowString = cellRowString.replace("\\SUB", "");
                        cellRowString += "    ";
                        header = true;
                    }

                    if (header) {
                        cellRowString = cellRowString.toUpperCase();
                    }

                    if (row == 0 && columnHeaders.length() > 0) {
                        final int pad = Math.max(cellLength - stringLength, 0);
                        System.out.print(" ".repeat(pad / 2) + cellRowString.toUpperCase() + " ".repeat(pad - (pad / 2)));
                    } else {
                        if (cellRowString.matches("^(-?\\d+|-?\\d+\\.\\d+)$")) {
                            final int pad = Math.max(cellLength - stringLength, 0);
                            System.out.print(" ".repeat(pad - (pad / 2)) + cellRowString + " ".repeat(pad / 2));
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

            if ((columnHeaders.length() > 0 && row == 0) || ((horizontalDivider || header) && row != stringListProcessed.size() - 1)) {
                printDivider('-', cellLengths, verticalDivider);
            }
        }

        printDivider('=', totalLength);
    }

    /**
     * Calculates the length for each column in the table.
     * Column length is capped based on the overall maximum string length.
     * If each column takes up lesser length than the overall maximum length, each column is padded until its overall length matches the defined maximum length.
     * @param title Title of the table. Overall max length = whichever is higher: pre-defined length or title length.
     * @param stringList List of strings to be printed.
     * @return List of cell lengths.
     */
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

    /**
     * Prepares each row of string for column printing.
     * @param rowString Array of strings to be processed. Each array index holds a string for a column.
     * @param cellLengths List of calculated lengths for each column.
     * @return List of a list of processed cell strings for printing for one row.
     */
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

    /**
     * Processes strings for printing in each column by truncating long strings and appending them to the next row.
     * Strings are truncated by space and newline.
     * @param cellString Original cell string to be processed
     * @param cellLength List of calculated lengths for each column.
     * @return List of processed cell strings for a row.
     */
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


    /**
     * Breaks up a space-less string into small strings up to the supplied maxLength.
     * If specified, the string will be trimmed to the length of the trailSpace for the first element.
     * @param s String to be broken up.
     * @param maxLength Maximum length of a broken up string.
     * @param trailSpace The first broken up element will have the length of the trailSpace instead.
     * @return List of formatted strings for print.
     */
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

    /**
     * Prints the title row of a table.
     * Text in title rows are automatically centered.
     * @param title Title string to print
     * @param length Total column lengths not including table online characters like '|'.
     */
    private static void printTitle(String title, int length) {
        final int pad = Math.max(length - title.length(), 0);
        System.out.println();
        printDivider('=', length);
        System.out.println("|  " + " ".repeat(pad / 2) + title.toUpperCase() + " ".repeat(pad - (pad / 2)) + "  |");
        printDivider('=', length);
    }

    /**
     * Prints a table divider with no column dividers up to the supplied length.
     * The length should be the overall column lengths not including the table outline characters '|'.
     * @param c Character to repeat as divider.
     * @param length Length to repeat characters up to.
     */
    private static void printDivider(char c, int length) {
        System.out.println("|" + (c + "").repeat(length + 4) + "|");
    }

    /**
     * Prints a table divider by repeating the supplied character up to the total length of the supplied cellLengths.
     * The length should be the overall column lengths not including the table outline characters '|'.
     * @param c Character to be repeated.
     * @param cellLengths List of calculated lengths for each column.
     * @param verticalDivider The table divider conforms to the table column dividers if set to true.
     */
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

    /**
     * Clears the command prompt of all outputs.
     * Note: Does not work in IntelliJ terminal.
     */
    public static void clearCmd() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ignored) {}
    }

    /**
     * Prints a message on the CLI of a message type.
     * @param messageType Message type, appended to the start of the message.
     * @param message Message to be printed.
     */
    public static void printMessage(MessageType messageType, String message) {
        clearCmd();

        message = "\n\t[" + messageType.name() + "] " + message;
        System.out.println(message);
    }

    /**
     * Logs an error to text file while printing a message to the CLI at the same time.
     * @param message Additional message to log to file.
     * @param e Exception to log to file.
     */
    public static void logToFile(String message, Exception e) {
        printMessage(MessageType.ERROR, message);
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String dateTime = LocalDateTime.now().format(dateTimeFormat);
        FileIO.logToFile(dateTime + ": " + message, e);
    }


}
