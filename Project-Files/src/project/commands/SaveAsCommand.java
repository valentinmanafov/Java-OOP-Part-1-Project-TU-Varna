package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for saving the current database (catalog and all modified tables)
 * to a new specified catalog file path.
 */
public class SaveAsCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a SaveAsCommand.
     * @param database The database instance to be saved.
     */
    public SaveAsCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the saveas command.
     * Saves all tables currently marked as modified (or all loaded tables if general unsaved changes exist)
     * to their respective data files (paths for these files are maintained in the catalog).
     * Then, saves the main database catalog to the new specified file path.
     * After a successful save, the database's current catalog path is updated to this new path,
     * and the unsaved changes flag is reset.
     * Usage: saveas &lt;new_catalog_filepath&gt;
     * @param args Command arguments: the new file path for the database catalog.
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open to 'saveas'. Please 'open' a database first.");
            return;
        }
        if (args.length != 1) {
            System.out.println("Usage: saveas <new_catalog_filepath>");
            return;
        }
        String newCatalogPath = args[0];

        try {

            System.out.println("Attempting to save database to new catalog file: '" + newCatalogPath + "'...");
            FileHandler.saveCatalogAndTables(database, newCatalogPath);

            database.markCatalogAsSaved(newCatalogPath);
            System.out.println("Database successfully saved as '" + newCatalogPath + "'. All changes committed.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: Saving database to '" + newCatalogPath + "': " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during 'saveas': " + e.getMessage());
        }
    }
}