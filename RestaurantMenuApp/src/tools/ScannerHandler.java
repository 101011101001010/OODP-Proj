package tools;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ScannerHandler {
    private Scanner scanner;

    public ScannerHandler(Scanner scanner) {
        this.scanner = scanner;

    }

    public byte getByte(String message) {
        byte input;
        printMessage(message);

        try {
            input = scanner.nextByte();
            scanner.nextLine();
            return input;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return getByte(message);
        }
    }

    public short getShort(String message) {
        short input;
        printMessage(message);

        try {
            input = scanner.nextShort();
            scanner.nextLine();
            return input;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return getShort(message);
        }
    }

    public int getInt(String message) {
        int input;
        printMessage(message);

        try {
            input = scanner.nextInt();
            scanner.nextLine();
            return input;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return getInt(message);
        }
    }

    public long getLong(String message) {
        long input;
        printMessage(message);

        try {
            input = scanner.nextLong();
            scanner.nextLine();
            return input;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return getLong(message);
        }
    }

    public float getFloat(String message) {
        float input;
        printMessage(message);

        try {
            input = scanner.nextFloat();
            scanner.nextLine();
            return input;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return getFloat(message);
        }
    }

    public double getDouble(String message) {
        double input;
        printMessage(message);

        try {
            input = scanner.nextDouble();
            scanner.nextLine();
            return input;
        } catch (InputMismatchException e) {
            scanner.nextLine();
            return getDouble(message);
        }
    }


    public String getString(String message) {
        String input;
        printMessage(message);
        input = scanner.nextLine();
        return input;
    }

    private void printMessage(String message) {
        System.out.print(message);
    }
}
