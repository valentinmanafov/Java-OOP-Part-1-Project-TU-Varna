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
            System.out.println("ERROR: No database file open to save.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Usage: saveas <file>");
            return;
        }
        String newCatalogPath = args[0];

        try {
            FileHandler.saveCatalogAndTables(database, newCatalogPath);
            database.markCatalogAsSaved(newCatalogPath);
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: Saving database to '" + newCatalogPath + "': " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: During saveas: " + e.getMessage());
        }
    }
}