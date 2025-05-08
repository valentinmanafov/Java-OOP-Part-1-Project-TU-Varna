package project.commands;

import project.*;

import java.util.*;

public class SaveAsCommand implements CommandHandler {

    private final Database database;

    public SaveAsCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("Error: No catalog file open to save.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Usage: saveas <new_catalog_filepath>");
            return;
        }
        String newCatalogPath = args[0];

        try {
            System.out.println("Note: Table file paths referenced in the new catalog may need manual adjustment if relative paths were used.");
            FileHandler.saveCatalogAndTables(database, newCatalogPath);
            database.markCatalogAsSaved(newCatalogPath);
        } catch (DatabaseOperationException e) {
            System.out.println("Error saving database to '" + newCatalogPath + "': " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during saveas: " + e.getMessage());
        }
    }
}