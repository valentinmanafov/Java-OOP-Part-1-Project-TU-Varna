package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for deleting rows from a table based on a specific criterion.
 */
public class DeleteCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a DeleteCommand.
     * @param database The database instance from which rows will be deleted.
     */
    public DeleteCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the delete command.
     * Deletes rows from the specified table where the value in a given column
     * matches a specified search value.
     * Usage: delete &lt;table&gt; &lt;column index&gt; &lt;value&gt;
     * @param args Command arguments: table name, search column index, search value.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: delete <table> <column index> <value>");
                return;
            }
            String tableName = args[0];
            String searchColNStr = args[1];
            String searchValue = args[2];

            Table table = database.getTable(tableName);
            int searchColIndex;
            try {
                searchColIndex = Integer.parseInt(searchColNStr);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid index. Column index must be a number.");
                return;
            }

            Column searchColumn = table.getColumn(searchColIndex);
            List<Row> remainingRows = new ArrayList<>();
            List<Row> currentRows = table.getRows();
            int originalRowCount = currentRows.size();

            try {
                //Iterate through rows and keep only those that do not match the criteria.
                for (Row row : currentRows) {
                    if (!TypeParser.looselyEquals(row.getValue(searchColIndex), searchValue, searchColumn.getType())) {
                        remainingRows.add(row);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("ERROR: During delete - column index out of bounds for a row's values.", e);
            }

            int deletedCount = originalRowCount - remainingRows.size();

            if (deletedCount > 0) {
                table.setRows(remainingRows);
                database.dataModified(tableName);
                System.out.println("Deleted " + deletedCount + " row(s) from '" + tableName + "'.");
            } else {
                System.out.println("WARNING: No rows matched criteria for deletion in table '" + tableName + "'.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}