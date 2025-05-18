package project.commands;

import project.*;

import java.util.*;
import java.io.File;

/**
 * Command handler for opening a database catalog file.
 */
public class OpenCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an OpenCommand.
     * @param database The database instance that will manage the opened catalog and tables.
     */
    public OpenCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the open command.
     * Reads a database catalog from the specified file path and loads its associated tables into memory.
     * If a catalog is already open and has unsaved changes, the command will fail and prompt the user
     * to save or close the current catalog first.
     * Usage: open &lt;filepath&gt;
     * @param args Command arguments: the file path of the database catalog to open.
     */
    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: open <filepath>");
            return;
        }
        String filePath = args[0];

        // Prevent opening a new catalog if the current one has unsaved changes.
        if (database.isCatalogOpen()) {
            if (database.hasUnsavedChanges()) {
                System.out.println("ERROR: Unsaved changes exist in the current database ('" +
                        new File(database.getCurrentCatalogFilePath()).getName() +
                        "'). Please 'save', 'saveas', or 'close' the current database first.");
                return;
            }

            System.out.println("Note: Closing the currently open database ('" +
                    new File(database.getCurrentCatalogFilePath()).getName() +
                    "') as a new one is being opened.");
        }

        try {
            Map<String, String> registry = FileHandler.readCatalog(filePath);

            database.loadCatalog(filePath, registry);

        } catch (DatabaseOperationException e) {

            System.out.println("ERROR: Opening database catalog '" + filePath + "': " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during open: " + e.getMessage());
        }
    }
}