package project.commands;

import project.CommandHandler;
import project.Database;

import java.util.*;

public class ShowTablesCommand implements CommandHandler {

    private final Database database;

    public ShowTablesCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("Error: No database file open. Use 'open <filepath>'.");
            return;
        }
        try {
            Set<String> tableNamesSet = database.getTableNames();
            if (tableNamesSet.isEmpty()) {
                System.out.println("WARNING: No tables registered in the current catalog.");
            } else {
                List<String> tableNamesList = new ArrayList<>(tableNamesSet);
                Collections.sort(tableNamesList);
                System.out.println("Registered Tables:");
                for (String name : tableNamesList) {
                    System.out.println("  - " + name);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: Listing tables: " + e.getMessage());
        }
    }
}