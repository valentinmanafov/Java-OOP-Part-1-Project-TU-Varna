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
            if (args.length < 2) {
                System.out.println("Usage: insert <table_name> <values...>");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName);
            List<Column> columns = table.getColumns();
            int expectedValues = columns.size();
            int providedValues = args.length - 1;

            if (providedValues != expectedValues) {
                System.out.println("ERROR:: Expected " + expectedValues + " values for " + expectedValues + " columns, but got " + providedValues + ".");
                return;
            }

            List<Object> parsedValues = new ArrayList<>();
            for (int i = 0; i < expectedValues; i++) {
                String inputValue = args[i + 1];
                Column column = columns.get(i);
                try {
                    parsedValues.add(TypeParser.parse(inputValue, column.getType()));
                } catch (DatabaseOperationException e) {
                    System.out.println("ERROR: Parsing value for column '" + column.getName() + "': " + e.getMessage());
                    return;
                }
            }

            table.addRow(new Row(parsedValues));
            database.dataModified(tableName);
            System.out.println("Row inserted into '" + tableName + "'.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR:: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: During insert: " + e.getMessage());
        }
    }
}