package tools;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Scanner wrapper for obtaining inputs from the CLI.
 * Simple error checking is provided by these functions.
 */
public class InputHelper {
    /**
     * Scanner instance used to obtain inputs.
     */
    private Scanner scanner;

    /**
     * Initialises the local scanner instance.
     */
    public InputHelper() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Obtains an integer input from the CLI with the specified message prompt. The valid inputs are bounded between lowerBound and upperBound, both inclusive.
     * If the input is out of bounds or invalid (ex. String), an error message will be thrown and the function will prompt for another input.
     * @param message Message prompt to be printed in the CLI.
     * @param lowerBound Valid inputs lower bound, inclusive.
     * @param upperBound Valid inputs upper bound, inclusive.
     * @return Input obtained by the scanner instance.
     */
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

    /**
     * Obtains a double input from the CLI with the specified message prompt.
     * Optional bounds may be specified after the first argument. Valid inputs will then be bounded to the specified bounds, both inclusive.
     * If no bounds are specified, default bound values will be used.
     * If the input is out of bounds or invalid (ex. String), an error message will be thrown and the function will prompt for another input.
     * @param message Message prompt to be printed in the CLI.
     * @param bounds Optional bound arguments to be specified in the form of (..., upperBound, lowerBound)
     *               The first argument specifies the upper bound. The default upper bound is 1000000.
     *               The second argument specifies the lower bound. The default lower bound is 0.
     * @return Input obtained by the scanner instance.
     */
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

    /**
     * Obtains a string input from the CLI with the specified message prompt.
     * Inputs obtained may not be empty, else an error will be thrown and the function will prompt for another input.
     * @param message Message prompt to be printed in the CLI.
     * @return Input obtained by the scanner instance.
     */
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
