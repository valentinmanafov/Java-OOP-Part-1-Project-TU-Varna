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
            if (args.length < 4 || args.length % 2 != 1) {
                System.out.println("Usage: insert <table_name> <col1_name> <value1> [col2_name value2] ...");
                return;
            }

            String tableName = args[0];
            Table table = database.getTable(tableName);
            List<Column> tableColumns = table.getColumns();
            int numTableCols = tableColumns.size();

            Map<String, String> inputValuesMap = new HashMap<>();
            for (int i = 1; i < args.length; i += 2) {
                String colName = args[i];
                String colValue = args[i + 1];
                if (inputValuesMap.containsKey(colName.toLowerCase())) {
                    System.out.println("Error: Duplicate column name provided in insert statement: '" + colName + "'.");
                    return;
                }
                inputValuesMap.put(colName.toLowerCase(), colValue);
            }

            List<Object> newRowValues = new ArrayList<>(Collections.nCopies(numTableCols, null));

            for (Map.Entry<String, String> entry : inputValuesMap.entrySet()) {
                String inputColNameLower = entry.getKey();
                String inputValueStr = entry.getValue();
                boolean columnFound = false;

                for (int i = 0; i < numTableCols; i++) {
                    Column tableCol = tableColumns.get(i);
                    if (tableCol.getName().toLowerCase().equals(inputColNameLower)) {
                        try {
                            Object parsedValue = TypeParser.parse(inputValueStr, tableCol.getType());
                            newRowValues.set(i, parsedValue);
                            columnFound = true;
                            break;
                        } catch (DatabaseOperationException e) {
                            System.out.println("Error parsing value for column '" + tableCol.getName() + "': " + e.getMessage());
                            return;
                        }
                    }
                }

                if (!columnFound) {
                    System.out.println("Error: Column '" + inputColNameLower + "' not found in table '" + tableName + "'.");
                    return;
                }
            }

            table.addRow(new Row(newRowValues));
            System.out.println("Row inserted into '" + tableName + "'.");

        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during insert: " + e.getMessage());
        }
    }
}