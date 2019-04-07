package tools;

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputHelper {
    private Scanner scanner;

    public InputHelper() {
        this.scanner = new Scanner(System.in);
    }

    public int getInt(String message, int lowerBound, int upperBound) {

        while (true) {
            System.out.println();
            System.out.print(message + ": ");
            try {
                int input = scanner.nextInt();
                scanner.nextLine();

                if (input >= lowerBound && input <= upperBound) {
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
            System.out.println();
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
        while (true) {
            System.out.println();
            System.out.print(message + ": ");

            if (!(input = scanner.nextLine()).isBlank()) {
                return input;
            }

            System.out.println("ERROR: Input cannot be blank.");
        }
    }
}
