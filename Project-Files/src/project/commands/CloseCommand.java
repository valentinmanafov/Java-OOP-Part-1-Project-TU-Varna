package project.commands;

import project.*;

import java.util.Scanner;

public class CloseCommand implements CommandHandler {
    private final Database database;
    private final Scanner inputScanner;

    public CloseCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("WARNING: No database file open.");
            return;
        }
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
                System.out.println("Close aborted.");
                return;
            }
        }
        database.closeCatalog();
    }
}