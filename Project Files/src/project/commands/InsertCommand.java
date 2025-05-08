package project.commands;

import project.*;
import java.util.*;

public class InsertCommand implements CommandHandler {

    private final Database database;

    public InsertCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            // Usage: insert <table_name> <value1> <value2> ... (values for all columns in order)
            if (args.length < 2) { // Need at least table name and one value (for a one-column table)
                System.out.println("Usage: insert <table_name> <value1> <value2> ...");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName); // Throws DatabaseOperationException
            List<Column> columns = table.getColumns();
            int expectedValues = columns.size();
            int providedValues = args.length - 1; // Number of values provided by user

            if (providedValues != expectedValues) {
                System.out.println("Error: Expected " + expectedValues + " values for " + expectedValues + " columns, but got " + providedValues + ".");
                return;
            }

            List<Object> parsedValues = new ArrayList<>();
            for (int i = 0; i < expectedValues; i++) {
                String inputValue = args[i + 1]; // Get value corresponding to column index i
                Column column = columns.get(i);
                try {
                    parsedValues.add(TypeParser.parse(inputValue, column.getType())); // Throws DatabaseOperationException
                } catch (DatabaseOperationException e) {
                    System.out.println("Error parsing value for column '" + column.getName() + "' (" + column.getType() + "): '" + inputValue + "'. " + e.getMessage());
                    return; // Stop insert on first error
                }
            }

            table.addRow(new Row(parsedValues)); // Throws DatabaseOperationException
            System.out.println("Row inserted into '" + tableName + "'.");
            // Removed auto-save

        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during insert: " + e.getMessage());
        }
    }
}