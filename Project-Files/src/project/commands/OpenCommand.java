package project.commands;

import project.*;

import java.util.*;
import java.io.File;

public class OpenCommand implements CommandHandler {

    private final Database database;

    public OpenCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: open <catalog_filepath>");
            return;
        }
        String filePath = args[0];

        if (database.isCatalogOpen()) {
            if (database.hasUnsavedChanges()) {
                System.out.println("Error: Unsaved changes exist. Please 'save' or 'close' first.");
                return;
            }
        }

        try {
            Map<String, String> registry = FileHandler.readCatalog(filePath);
            database.loadCatalog(filePath, registry);
        } catch (DatabaseOperationException e) {
            System.out.println("Error opening catalog '" + filePath + "': " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during open: " + e.getMessage());
        }
    }
}