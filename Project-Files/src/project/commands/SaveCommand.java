package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for saving the current state of the database (catalog and modified tables)
 * to its existing file paths.
 */
public class SaveCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a SaveCommand.
     * @param database The database instance to be saved.
     */
    public SaveCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the save command.
     * If a database catalog is open and there are unsaved changes, this command
     * writes all modified tables back to their respective data files and updates
     * the main catalog file. If there are no unsaved changes, it informs the user.
     * If the current catalog path is unknown (e.g., after 'open' failed or for a new, unsaved database),
     * it prompts the user to use 'saveas' instead.
     * @param args Command arguments (not used for this command).
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open to save. Please 'open' a database first.");
            return;
        }

        String currentPath = database.getCurrentCatalogFilePath();
        if (currentPath == null) {

            System.out.println("ERROR: Cannot save, the current database catalog file path is unknown. " +
                    "Please use 'saveas <filepath>' to specify a file for the new database.");
            return;
        }

        if (!database.hasUnsavedChanges()) {
            System.out.println("No unsaved changes detected in database '" + currentPath + "'. Save not required.");
            return;
        }

        try {

            System.out.println("Saving changes to database '" + currentPath + "'...");
            FileHandler.saveCatalogAndTables(database, currentPath);

            database.markCatalogAsSaved(currentPath);

            System.out.println("Database saved successfully to '" + currentPath + "'. All changes committed.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: Saving database to '" + currentPath + "': " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during save: " + e.getMessage());
        }
    }
}