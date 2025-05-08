package project.commands;

import project.*;
import java.util.*;

public class ImportCommand implements CommandHandler {

    private final Database database;

    public ImportCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: import <filename.txt>");
                return;
            }
            String filename = args[0];
            Table table = FileHandler.readTableFromFile(filename);

            try {
                database.getTable(table.getName());
                System.out.println("Error: Table named '" + table.getName() + "' already exists.");
                return;
            } catch (DatabaseOperationException e) {
            }

            database.addTable(table);
            System.out.println("Table '" + table.getName() + "' imported successfully from '" + filename + "'.");

        } catch (DatabaseOperationException e) {
            System.out.println("Error importing file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during import: " + e.getMessage());
        }
    }
}