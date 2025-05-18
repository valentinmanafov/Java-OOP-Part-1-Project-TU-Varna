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
            System.out.print("WARNING: Unsaved changes exist. Exit anyway without saving? (Y/N): ");
            String confirmation = "";
            try {
                confirmation = inputScanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                System.out.println("ERROR: Exit aborted.");
                return;
            }
            if (!confirmation.equals("Y")) {
                System.out.println("WARNING: Exit aborted. Use 'save' or 'saveas' first.");
                return;
            }
        }
        System.out.println("Exiting the program...");
        if (inputScanner != null) inputScanner.close();
        System.exit(0);
    }
}