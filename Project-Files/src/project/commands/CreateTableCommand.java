package project.commands;

import project.*;
import java.util.*;

/**
 * Command handler for creating a new table in the database.
 */
public class CreateTableCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a CreateTableCommand.
     * @param database The database instance where the table will be created.
     */
    public CreateTableCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the create table command.
     * Creates a new table with the specified name and column definitions.
     * The table is registered in the database and an empty data file for it is created.
     * Usage: createtable &lt;table_name&gt; &lt;col1_name&gt; &lt;col1_type&gt; [&lt;col2_name&gt; &lt;col2_type&gt; ...]
     * Column types can be: Integer, Double, String.
     * @param args Command arguments: table name, followed by pairs of column name and column type.
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open. Use 'open <filepath>'.");
            return;
        }
        try {

            if (args.length < 3 || args.length % 2 == 0) {
                System.out.println("Usage: createtable <table_name> <col1_name> <col1_type> ...");
                return;
            }
            String tableName = args[0];
            List<Column> columns = new ArrayList<>();

            // Parse column definitions from arguments.
            // Iterate through argument pairs for column name and type.
            for (int i = 1; i < args.length; i += 2) {
                String colName = args[i];
                String typeName = args[i + 1];
                DataType colType;
                try {
                    colType = DataType.valueOf(typeName.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: Invalid type: '" + typeName + "'. Valid types are INTEGER, DOUBLE, STRING.");
                    return;
                }
                for (Column existing : columns) {
                    if (existing.getName().equalsIgnoreCase(colName)) {
                        System.out.println("ERROR: Duplicate column name '" + colName + "' specified in command.");
                        return;
                    }
                }
                columns.add(new Column(colName, colType));
            }

            if (columns.isEmpty()) {
                System.out.println("ERROR: Must define at least one column for the table.");
                return;
            }

            Table newTable = new Table(tableName, columns);
            String defaultTablePath = tableName + ".txt";

            database.registerNewTable(newTable, defaultTablePath);

            System.out.println("Table '" + tableName + "' created successfully and registered in the database catalog.");
            FileHandler.writeTableToFile(newTable, defaultTablePath);
            System.out.println("Schema for table '" + tableName + "' saved to '" + defaultTablePath + "'.");


        } catch (DatabaseOperationException | IllegalArgumentException e) {
            System.out.println("ERROR: creating table: " + e.getMessage());
        }
    }
}