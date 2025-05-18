package project.commands;

import project.CommandHandler;
import project.Database;

import java.util.*;

/**
 * Command handler for displaying a list of all tables registered in the current database catalog.
 */
public class ShowTablesCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a ShowTablesCommand.
     * @param database The database instance from which to retrieve the list of table names.
     */
    public ShowTablesCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the showtables command.
     * Retrieves the set of all registered table names from the database,
     * sorts them alphabetically, and prints them to the console.
     * If no database is open or if the catalog is empty, appropriate messages are displayed.
     * @param args Command arguments (not used for this command).
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open. Use 'open <filepath>'.");
            return;
        }
        try {

            Set<String> tableNamesSet = database.getTableNames();

            if (tableNamesSet.isEmpty()) {
                System.out.println("WARNING: No tables are currently registered in the database catalog.");
            } else {
                List<String> tableNamesList = new ArrayList<>(tableNamesSet);
                Collections.sort(tableNamesList);

                System.out.println("Registered Tables in '" + new java.io.File(database.getCurrentCatalogFilePath()).getName() + "':");
                for (String name : tableNamesList) {
                    System.out.println("  - " + name);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred while listing tables: " + e.getMessage());
        }
    }
}