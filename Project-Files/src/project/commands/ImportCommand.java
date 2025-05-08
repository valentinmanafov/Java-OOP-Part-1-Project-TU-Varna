package project.commands;

import project.*;

import java.util.*;
import java.io.File;

public class ImportCommand implements CommandHandler {

    private final Database database;

    public ImportCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("Error: No catalog file open. Use 'open <filepath>'.");
            return;
        }
        try {
            if (args.length != 1) {
                System.out.println("Usage: import <table_filepath.txt>");
                return;
            }
            String tableFilePath = args[0];
            File tableFile = new File(tableFilePath);
            if (!tableFile.exists()) {
                System.out.println("Error: Table file not found: " + tableFilePath);
                return;
            }

            Table table = FileHandler.readTableFromFile(tableFilePath);
            String tableNameFromFile = table.getName();

            database.registerImportedTable(table, tableFilePath);

            System.out.println("Table '" + tableNameFromFile + "' imported successfully from '" + tableFilePath + "' and registered in catalog.");

        } catch (DatabaseOperationException e) {
            System.out.println("Error importing file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during import: " + e.getMessage());
        }
    }
}