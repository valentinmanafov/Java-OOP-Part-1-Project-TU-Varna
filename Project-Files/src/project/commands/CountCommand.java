package project.commands;

import project.*;

/**
 * Command handler for counting rows in a table that match a specific criterion.
 */
public class CountCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a CountCommand.
     * @param database The database instance to operate on.
     */
    public CountCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the count command.
     * Counts the number of rows in a specified table where the value in a given column
     * matches a specified search value.
     * Usage: count &lt;table&gt; &lt;column index&gt; &lt;value&gt;
     * @param args Command arguments: table name, search column index, search value.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: count <table> <column index> <value>");
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

            Column searchColumn = table.getColumn(searchColIndex); // Validates column index
            int count = 0;

            try {
                // Iterate through each row and check for a match using TypeParser.looselyEquals
                for (Row row : table.getRows()) {
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchValue, searchColumn.getType())) {
                        count++;
                    }
                }
            } catch (IndexOutOfBoundsException e) {

                throw new DatabaseOperationException("ERROR: During count - column index out of bounds for a row.", e);
            }
            System.out.println("Count: " + count);

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}