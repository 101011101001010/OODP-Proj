package tools;

import java.io.IOException;
import java.util.*;

public class ConsoleHelper {
    private Scanner scanner;
    private int MAX_LENGTH = 80;

    public ConsoleHelper() {
        this.scanner = new Scanner(System.in);
    }

    public void sendWelcome(List<String[]> mainCLIOptions) {
        MAX_LENGTH = 60;
        printTitle("Restaurant Reservation and Point of Sale System", true);
        //printColumns(new String[] {"ID // Text Commands // Function Call"}, true, false, false, true, true);

        //String format = "  %1$-50s  ";
        List<String> list;
        int optionsIndex = 1;

        for (int i = 0; i < mainCLIOptions.size(); i++) {
            String[] options = mainCLIOptions.get(i);

            list = new ArrayList<>(Arrays.asList(options));
            for (int index = 0; index < list.size(); index++) {
                list.set(index, optionsIndex + " // " + list.get(index));
                optionsIndex++;
            }

            list.add(0, "ID // Function Call");

            if (i == 0) {
                printColumns(list, true, false, true, true, true);
            } else {
                printColumns(list, true, false, true, true, false);
            }
        }

        printColumns(new String[] {"ID // Function Call", "-1 // Switch staff account", "-2 // Exit program"}, true, false, true, false, false);
        printFooter();
        MAX_LENGTH = 80;
    }

    public void printInstructions(String[] body) {
        System.out.println();
        printDivider('-');
        printColumns(Arrays.asList(body), false, false, false, false, true);
        printDivider('-');
        System.out.println();
    }

    public void printDisplayTable(String title, List<String> body, boolean horizontalDivider, boolean verticalDivider) {
        printTitle(title, true);
        printColumns(body, verticalDivider, horizontalDivider, true, false, true);
        printDivider('=');
    }

    public void printChoicesSimple(String header, String[] options, String[] footerOptions) {
        printChoicesSimple(header, Arrays.asList(options), footerOptions);
    }

    public void printChoicesSimple(String header, List<String> options, String[] footerOptions) {
        System.out.println();
        printDivider('=');
        List<String> list = new ArrayList<>(Collections.singletonList(header));
        list.addAll(options);

        for (int index = 1; index <= options.size(); index++) {
            list.set(index, index + " // " + list.get(index));
        }

        printColumns(list, true, false, true, true, true);

        list = new ArrayList<>(Collections.singletonList(header));
        list.addAll(Arrays.asList(footerOptions));

        for (int index = 1; index <= footerOptions.length; index++) {
            list.set(index, (0 - index) + " // " + list.get(index));
        }

        printColumns(list, true, false, true, false, false);
        printFooter();
    }

    public int printChoices(String message, String header, String[] options, String[] footerOptions) {
        return printChoices(message, header, Arrays.asList(options), footerOptions);
    }

    public int printChoices(String message, String header, List<String> options, String[] footerOptions) {
        System.out.println();
        printDivider('=');
        List<String> list = new ArrayList<>(Collections.singletonList(header));
        list.addAll(options);

        for (int index = 1; index <= options.size(); index++) {
            list.set(index, index + " // " + list.get(index));
        }

        printColumns(list, true, false, true, true, true);

        list = new ArrayList<>(Collections.singletonList(header));
        list.addAll(Arrays.asList(footerOptions));

        for (int index = 1; index <= footerOptions.length; index++) {
            list.set(index, (0 - index) + " // " + list.get(index));
        }

        printColumns(list, true, false, true, false, false);
        printFooter();

        return getInt(message, options.size(), (0 - footerOptions.length));
    }

    public void clearCmd() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ignored) {};
    }

    public void setMaxLength(int maxLength) {
        this.MAX_LENGTH = maxLength;
    }

    public void printTitle(String title, boolean center) {
        int pad = ((MAX_LENGTH) - title.length());
        System.out.println();
        printDivider('=');
        System.out.print("|  ");

        if (center) {
            System.out.print(" ".repeat(pad / 2));
            System.out.print(title);
            System.out.print(" ".repeat(pad - (pad / 2)));
        } else {
            System.out.print(title);
            System.out.print(" ".repeat(pad));
        }
        System.out.println("  |");
        printDivider('=');
    }

    public void printFooter() {
        printDivider('=');
        System.out.println();
    }

    public void printColumns(String[] stringList, boolean vDivider, boolean hDivider, boolean headerDivider, boolean bottomDivider, boolean displayFirstRow) {
        printColumns(Arrays.asList(stringList), vDivider, hDivider, headerDivider, bottomDivider, displayFirstRow);
    }

    public void printColumns(List<String> stringList, boolean vDivider, boolean hDivider, boolean headerDivider, boolean bottomDivider, boolean displayFirstRow) {
        if (stringList == null) {
            return;
        }

        ////////// Determine each column's length based on text length. //////////
        List<Integer> cellLengths = new ArrayList<>();
        for (String entryString : stringList) {
            String[] entryStrings = entryString.split(" // ");

            for (int cell = 0; cell < entryStrings.length; cell++) {
                int cellLength = entryStrings[cell].length();

                if (cell >= cellLengths.size()) {
                    cellLengths.add(cellLength);
                } else {
                    cellLengths.set(cell, Math.max(cellLength, cellLengths.get(cell)));
                }
            }
        }

        ////////// Calculate max allowable length per column and make adjustments accordingly. //////////
        int totalStringSpace = (MAX_LENGTH) - (5 * (cellLengths.size() - 1));
        int maxCellLength = (int) Math.floor(1.0 * totalStringSpace / cellLengths.size());
        int cellsWithMaxLength = 0;

        for (int cell = 0; cell < cellLengths.size(); cell++) {
            int cellLength = cellLengths.get(cell);
            cellLengths.set(cell, Math.min(cellLength, maxCellLength));

            if (cellLength > maxCellLength) {
                cellsWithMaxLength++;
            }
        }

        int extraSpace = totalStringSpace - cellLengths.stream().mapToInt(Integer::intValue).sum();

        if (cellsWithMaxLength == 0) {
            int index = 0;
            int largestCellLength = 0;
            int largestCellIndex = 0;

            for (int cellLength : cellLengths) {
                if (cellLength > largestCellLength) {
                    largestCellLength = cellLength;
                    largestCellIndex = index;
                }

                index++;
            }

            cellLengths.set(largestCellIndex, cellLengths.get(largestCellIndex) + extraSpace);
            extraSpace = 0;
        }

        while (extraSpace > 0) {
            for (int index = 0; index < cellLengths.size(); index++) {
                int size = cellLengths.get(index);

                if (size >= maxCellLength) {
                    cellLengths.set(index, size + 1);
                    extraSpace--;

                    if (extraSpace <= 0) {
                        break;
                    }
                }
            }
        }

        ////////// String adjustments in case they're too long, then print row by row. //////////
        /* Note: 1 list entry = 1 row */
        for (int entry = 0; entry < stringList.size(); entry++) {
            if (!displayFirstRow && entry == 0) {
                continue;
            }
            List<List<String>> entryStringList = new ArrayList<>();
            String[] entryStrings = stringList.get(entry).split(" // ");
            int maxRowCount = 0;

            for (int cell = 0; cell < cellLengths.size(); cell++) {
                List<String> cellStringList = new ArrayList<>();
                int cellLength = cellLengths.get(cell);

                if (cell < entryStrings.length) {
                    String cellString = entryStrings[cell].trim().replace("--", "\n");
                    String[] cellStrings = cellString.split("\n");

                    for (String cellRowString : cellStrings) {
                        int strIndex = 0;
                        int trailSpace = 0;
                        String[] strs = cellRowString.split(" ");
                        StringBuilder longStrings = new StringBuilder();

                        for (String str : strs) {
                            if (str.length() > cellLength) {
                                longStrings.append(str).append(" ");
                            } else {
                                if (longStrings.length() > 0) {
                                    cellStringList.addAll(subString(longStrings.toString(), cellLength, trailSpace));
                                    trailSpace = 0;
                                    longStrings = new StringBuilder();
                                }

                                int listSize = cellStringList.size();
                                if ((listSize > 0) && (cellStringList.get(listSize - 1).length() + str.length() < cellLength) && (strIndex > 0)) {
                                    cellStringList.set(listSize - 1, cellStringList.get(listSize - 1) + " " + str);
                                } else {
                                    cellStringList.add(str);
                                    trailSpace = 0;
                                }
                            }

                            strIndex++;
                        }

                        if (longStrings.length() > 0) {
                            cellStringList.addAll(subString(longStrings.toString(), cellLength, trailSpace));
                        }
                    }

                    if (cellStringList.size() > maxRowCount) {
                        maxRowCount = cellStringList.size();
                    }
                } else {
                    for (int row = 0; row < maxRowCount; row++) {
                        String toAdd = " ".repeat(cellLength);
                        cellStringList.add(toAdd);
                    }
                }

                entryStringList.add(cellStringList);
            }

            for (int cellRow = 0; cellRow < maxRowCount; cellRow++) {
                System.out.print("|  ");
                for (int cell = 0; cell < cellLengths.size(); cell++) {
                    int cellLength = cellLengths.get(cell);
                    List<String> cellStringList = entryStringList.get(cell);

                    if (cellRow < cellStringList.size()) {
                        String printStr = cellStringList.get(cellRow);

                        if (printStr.length() < cellLength) {
                            if (printStr.matches("^-?\\d*\\.?\\d*$")) {
                                printStr = " ".repeat(cellLength - printStr.length()) + printStr;
                            } else {
                                printStr += " ".repeat(cellLength - printStr.length());
                            }
                        }

                        System.out.print(printStr);
                    } else {
                        System.out.print(" ".repeat(cellLength));
                    }

                    if (cell != cellLengths.size() - 1) {
                        if (vDivider) {
                            System.out.print("  |  ");
                        } else {
                            System.out.print(" ".repeat(5));
                        }
                    }
                }
                System.out.println("  |");
            }

            if ((headerDivider && entry == 0 && entry != stringList.size() - 1) || (hDivider && entry != stringList.size() - 1)) {
                printColumnDivider(cellLengths, vDivider);
            }
        }

        if (bottomDivider) {
            printColumnDivider(cellLengths, vDivider);
        }
    }

    private List<String> subString(String s, int maxLength, int trailSpace) {
        List<String> ret = new ArrayList<>();

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

    public void printDivider(char c) {
        System.out.println("|" + (c + "").repeat(MAX_LENGTH + 4) + "|");
    }

    private void printColumnDivider(List<Integer> columnLengths, boolean vDivider) {
        System.out.print("|");

        for (int column = 0; column < columnLengths.size(); column++) {
            int length = columnLengths.get(column);
            System.out.print("-".repeat(length + 4));

            if (column != (columnLengths.size() - 1)) {
                System.out.print(vDivider? "|" : '-');
            }
        }

        System.out.println("|");
    }

    public int getInt(String message, int... bounds) {
        int upperBound = (bounds.length >= 1)? bounds[0] : Integer.MAX_VALUE;
        int lowerBound = (bounds.length >= 2)? bounds[1] : -1;

        while (true) {
            System.out.print(message + ": ");
            try {
                int input = scanner.nextInt();
                scanner.nextLine();

                if (input >= lowerBound && input <= upperBound && input != 0) {
                    return input;
                } else {
                    System.out.println("Invalid input. Please try again.");
                }
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    public double getDouble(String message, double... bounds) {
        double upperBound = (bounds.length >= 1)? bounds[0] : 1000000;
        double lowerBound = (bounds.length >= 2)? bounds[1] : 0;

        while (true) {
            System.out.print(message + ": ");
            try {
                double input = scanner.nextDouble();
                scanner.nextLine();

                if (input >= lowerBound && input <= upperBound) {
                    return input;
                } else {
                    System.out.println("ERROR: Invalid input. Please try again.");
                }
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("ERROR: Invalid input. Please try again.");
            }
        }
    }

    public String getString(String message) {
        String input;
        System.out.print(message + ": ");
        while ((input = scanner.nextLine()).isBlank()) {
            System.out.println("ERROR: Input cannot be blank.");
            System.out.print(message + ": ");
        }
        return input;
    }
}
