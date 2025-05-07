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
        try {
            Set<String> tableNamesSet = database.getTableNames();
            if (tableNamesSet.isEmpty()) {
                System.out.println("No tables loaded.");
            } else {
                List<String> tableNamesList = new ArrayList<>(tableNamesSet);
                Collections.sort(tableNamesList);
                System.out.println("Loaded Tables:");
                for (String name : tableNamesList) {
                    System.out.println("  - " + name);
                }
            }
        } catch (Exception e) {
            System.out.println("Error listing tables: " + e.getMessage());
        }
    }
}