package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for updating values in rows of a table that match a specific criterion.
 */
public class UpdateCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an UpdateCommand.
     * @param database The database instance containing the table to be updated.
     */
    public UpdateCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the update command.
     * Finds rows in the specified table where the value in a search column (by index)
     * matches a given search value. For each matching row, it updates the value
     * in a target column (by index) to a new target value.
     * The new target value is parsed according to the data type of the target column.
     * Usage: update &lt;table&gt; &lt;search_column_index&gt; &lt;search_value&gt; &lt;target_column_index&gt; &lt;new_target_value&gt;
     * @param args Command arguments: table name, search column index, search value,
     * target column index, and the new target value.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 5) {
                System.out.println("Usage: update <table> <search_column_index> <search_value> <target_column_index> <new_target_value>");
                return;
            }
            String tableName = args[0];
            String searchColNStr = args[1];
            String searchVal = args[2];
            String targetColNStr = args[3];
            String targetValStr = args[4];

            Table table = database.getTable(tableName);
            int searchColIndex, targetColIndex;

            try {
                searchColIndex = Integer.parseInt(searchColNStr);
                targetColIndex = Integer.parseInt(targetColNStr);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid column index. Indices must be numbers.");
                return;
            }


            Column searchColumn = table.getColumn(searchColIndex);
            Column targetColumn = table.getColumn(targetColIndex);

            Object targetValueObject;
            try {

                targetValueObject = TypeParser.parse(targetValStr, targetColumn.getType());
            } catch (DatabaseOperationException e) {
                System.out.println("ERROR: Parsing target value '" + targetValStr + "' for column '" +
                        targetColumn.getName() + "' (" + targetColumn.getType() + "): " + e.getMessage());
                return;
            }

            int updatedCount = 0;
            try {
                List<Row> rows = table.getRows();
                for (Row row : rows) {
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchVal, searchColumn.getType())) {
                        row.setValue(targetColIndex, targetValueObject);
                        updatedCount++;
                    }
                }


            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("ERROR: During update - internal error accessing row data by index.", e);
            }

            if (updatedCount > 0) {
                database.dataModified(tableName);
                System.out.println("Updated " + updatedCount + " row(s) in table '" + tableName + "'.");
            } else {
                System.out.println("WARNING: No rows matched the update criteria in table '" + tableName + "'.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}