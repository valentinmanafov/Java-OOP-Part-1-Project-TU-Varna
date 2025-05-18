package project.commands;

import project.*;

import java.util.Scanner;

/**
 * Command handler for closing the currently open database catalog.
 */
public class CloseCommand implements CommandHandler {
    private final Database database;
    private final Scanner inputScanner;

    /**
     * Constructs a CloseCommand.
     * @param database The database instance to operate on.
     * @param scanner  The scanner to read user confirmation if there are unsaved changes.
     */
    public CloseCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    /**
     * Executes the close command.
     * If a database catalog is open, it will be closed.
     * If there are unsaved changes, the user is prompted for confirmation before closing.
     * Closing a database clears its loaded tables from memory and resets its state.
     * @param args Command arguments (not used for this command).
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("WARNING: No database file open.");
            return;
        }

        // Handle unsaved changes before closing
        if (database.hasUnsavedChanges()) {
            System.out.print("WARNING: Unsaved changes. Close anyway? (Y/N): ");
            String confirmation = "";
            try {
                confirmation = inputScanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                System.out.println("WARNING: Input error. Close aborted.");
                return;
            }
            if (!confirmation.equals("Y")) {
                System.out.println("Close aborted. Please 'save' or 'saveas' your changes.");
                return;
            }
        }
        database.closeCatalog();
    }
}