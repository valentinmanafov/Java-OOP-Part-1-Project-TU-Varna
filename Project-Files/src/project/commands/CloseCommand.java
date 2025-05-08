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
            System.out.println("No catalog file open.");
            return;
        }
        if (database.hasUnsavedChanges()) {
            System.out.print("Warning: Unsaved changes. Close anyway? (yes/no): ");
            String confirmation = "";
            try {
                confirmation = inputScanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                System.out.println("Input error. Close aborted.");
                return;
            }
            if (!confirmation.equals("yes")) {
                System.out.println("Close aborted.");
                return;
            }
        }
        database.closeCatalog();
    }
}