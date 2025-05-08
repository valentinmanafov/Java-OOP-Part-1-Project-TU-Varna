package project.commands;

import project.*;

import java.util.Scanner;

public class ExitCommand implements CommandHandler {

    private final Scanner inputScanner;
    private final Database database;

    public ExitCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        if (database.isCatalogOpen() && database.hasUnsavedChanges()) {
            System.out.print("Warning: Unsaved changes exist. Exit anyway without saving? (yes/no): ");
            String confirmation = "";
            try {
                confirmation = inputScanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                System.out.println("Input error. Exit aborted.");
                return;
            }
            if (!confirmation.equals("yes")) {
                System.out.println("Exit aborted. Use 'save' or 'saveas' first.");
                return;
            }
        }
        System.out.println("Exiting the program...");
        if (inputScanner != null) inputScanner.close();
        System.exit(0);
    }
}