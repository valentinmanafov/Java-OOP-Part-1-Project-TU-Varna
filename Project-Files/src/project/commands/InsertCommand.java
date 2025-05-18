package project.commands;

import project.*;
import java.util.*;

/**
 * Command handler for inserting a new row of data into a table.
 */
public class InsertCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an InsertCommand.
     * @param database The database instance where the row will be inserted.
     */
    public InsertCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the insert command.
     * Inserts a new row into the specified table with the given values.
     * The number of values provided must exactly match the number of columns in the table.
     * Values are parsed according to their respective column data types.
     * Usage: insert &lt;table_name&gt; &lt;value1&gt; [&lt;value2&gt; ...]
     * @param args Command arguments: table name, followed by values for each column in order.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Usage: insert <table_name> <value1> [<value2> ...]");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName);
            List<Column> columns = table.getColumns();
            int expectedValues = columns.size();
            int providedValues = args.length - 1;

            // Validate that the number of provided values matches the number of columns.
            if (providedValues != expectedValues) {
                System.out.println("ERROR: Expected " + expectedValues + " values for " + expectedValues +
                        " columns in table '" + tableName + "', but got " + providedValues + ".");
                StringBuilder columnDetails = new StringBuilder("Columns are: ");
                for(int i=0; i < columns.size(); i++){
                    columnDetails.append(columns.get(i).getName()).append(" (").append(columns.get(i).getType()).append(")");
                    if(i < columns.size() -1) columnDetails.append(", ");
                }
                System.out.println(columnDetails.toString());
                return;
            }

            List<Object> parsedValues = new ArrayList<>();
            // Parse each provided string value into its corresponding column's data type.
            for (int i = 0; i < expectedValues; i++) {
                String inputValue = args[i + 1];
                Column column = columns.get(i);
                try {
                    parsedValues.add(TypeParser.parse(inputValue, column.getType()));
                } catch (DatabaseOperationException e) {
                    System.out.println("ERROR: Parsing value for column '" + column.getName() + "' (" + column.getType() + "): " + e.getMessage() +
                            ". Input was: '" + inputValue + "'.");
                    return;
                }
            }

            table.addRow(new Row(parsedValues));
            database.dataModified(tableName);
            System.out.println("Row inserted successfully into '" + tableName + "'.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during insert: " + e.getMessage());
        }
    }
}