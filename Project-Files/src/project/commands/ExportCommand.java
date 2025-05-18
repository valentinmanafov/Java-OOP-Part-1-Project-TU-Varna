package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for exporting a table's data to a specified file.
 */
public class ExportCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an ExportCommand.
     * @param database The database instance from which the table will be exported.
     */
    public ExportCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the export command.
     * Retrieves the specified table from the database and writes its contents
     * (schema and data) to the given filename using {@link FileHandler#writeTableToFile(Table, String)}.
     * Usage: export &lt;table&gt; &lt;file.txt&gt;
     * @param args Command arguments: table name, output file name.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: export <table> <file.txt>");
                return;
            }
            String tableName = args[0];
            String filename = args[1];

            Table table = database.getTable(tableName);

            FileHandler.writeTableToFile(table, filename);
            System.out.println("Table '" + tableName + "' exported successfully to '" + filename + "'.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: Exporting table: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during export: " + e.getMessage());
        }
    }
}