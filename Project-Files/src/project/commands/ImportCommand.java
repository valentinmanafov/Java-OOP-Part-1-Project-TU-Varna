package project.commands;

import project.*;

import java.util.*;
import java.io.File;

/**
 * Command handler for importing a table from a text file into the database.
 */
public class ImportCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an ImportCommand.
     * @param database The database instance into which the table will be imported.
     */
    public ImportCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the import command.
     * Reads a table structure and data from the specified text file using {@link FileHandler#readTableFromFile(String)}.
     * The imported table is then registered in the current database.
     * The table name used for registration is the name specified within the imported file itself.
     * Usage: import &lt;file.txt&gt;
     * @param args Command arguments: the file path of the text file to import.
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open. Use 'open <filepath>'.");
            return;
        }
        try {
            if (args.length != 1) {
                System.out.println("Usage: import <file.txt>");
                return;
            }
            String tableFilePath = args[0];
            File tableFile = new File(tableFilePath);
            if (!tableFile.exists()) {
                System.out.println("ERROR: Table file not found: " + tableFilePath);
                return;
            }

            Table table = FileHandler.readTableFromFile(tableFilePath);
            String tableNameFromFile = table.getName();

            database.registerImportedTable(table, tableFilePath);

            System.out.println("Table '" + tableNameFromFile + "' imported successfully from '" + tableFilePath + "' and registered in database.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: Importing file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during import: " + e.getMessage());
        }
    }
}