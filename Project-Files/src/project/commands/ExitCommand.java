package project.commands;

import project.*;

import java.util.Scanner;

/**
 * Command handler for exiting the application.
 */
public class ExitCommand implements CommandHandler {

    private final Scanner inputScanner;
    private final Database database;

    /**
     * Constructs an ExitCommand.
     * @param database The database instance, used to check for unsaved changes.
     * @param scanner  The scanner to read user confirmation if there are unsaved changes.
     */
    public ExitCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    /**
     * Executes the exit command.
     * If a database catalog is open and has unsaved changes, the user is prompted
     * for confirmation before exiting. Otherwise, the program terminates.
     * @param args Command arguments (not used for this command).
     */
    @Override
    public void execute(String[] args) {
        // Check for unsaved changes before exiting.
        if (database.isCatalogOpen() && database.hasUnsavedChanges()) {
            System.out.print("WARNING: Unsaved changes exist. Exit anyway without saving? (Y/N): ");
            String confirmation = "";
            try {
                confirmation = inputScanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                System.out.println("ERROR: Input error. Exit aborted. Please try again.");
                return;
            }
            if (!confirmation.equals("Y")) {
                System.out.println("WARNING: Exit aborted. Use 'save' or 'saveas' to save changes, or 'close' to close the database without saving (after confirmation).");
                return;
            }
        }
        System.out.println("Exiting the program...");
        if (inputScanner != null) {
            inputScanner.close();
        }
        System.exit(0);
    }
}