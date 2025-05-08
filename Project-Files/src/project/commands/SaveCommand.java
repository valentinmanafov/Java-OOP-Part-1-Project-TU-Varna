package project.commands;

import project.*;

import java.util.*;

public class SaveCommand implements CommandHandler {

    private final Database database;

    public SaveCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("Error: No catalog file open to save.");
            return;
        }

        String currentPath = database.getCurrentCatalogFilePath();
        if (currentPath == null) {
            System.out.println("Error: Cannot save, current file path is unknown. Use 'saveas'.");
            return;
        }

        if (!database.hasUnsavedChanges()) {
            System.out.println("No unsaved changes to save.");
            return;
        }

        try {
            FileHandler.saveCatalogAndTables(database, currentPath);
            database.markCatalogAsSaved(currentPath);
        } catch (DatabaseOperationException e) {
            System.out.println("Error saving database: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during save: " + e.getMessage());
        }
    }
}