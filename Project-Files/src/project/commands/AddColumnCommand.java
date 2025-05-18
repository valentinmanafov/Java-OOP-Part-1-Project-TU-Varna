package project.commands;

import project.*;

/**
 * Command handler for adding a new column to an existing table.
 */
public class AddColumnCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an AddColumnCommand.
     * @param database The database instance to operate on.
     */
    public AddColumnCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the add column command.
     * Adds a new column with the specified name and type to the given table.
     * All existing rows in the table will have a null value added for this new column.
     * Usage: addcolumn &lt;table&gt; &lt;column name&gt; &lt;column type&gt;
     * Column types can be: Integer, Double, String.
     * @param args Command arguments: table name, new column name, new column type.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: addcolumn <table> <column name> <column type>\nColumn types can be: Integer, Double, String");
                return;
            }
            String tableName = args[0];
            String colName = args[1];
            String typeName = args[2];

            Table table = database.getTable(tableName);

            DataType colType;
            try {
                colType = DataType.valueOf(typeName.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("WARNING: Invalid type: " + typeName + ". Valid types are INTEGER, DOUBLE, STRING.");
                return;
            }

            table.addColumn(new Column(colName, colType));
            database.dataModified(tableName);
            System.out.println("Column '" + colName + "' added to '" + tableName + "'.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}