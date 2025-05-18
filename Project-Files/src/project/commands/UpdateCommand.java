package project.commands;

import project.*;

import java.util.*;

public class UpdateCommand implements CommandHandler {

    private final Database database;

    public UpdateCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 5) {
                System.out.println("Usage: update <table> <search column index> <search value> <target column index> <target value>");
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
                System.out.println("Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(searchColIndex);
            Column targetColumn = table.getColumn(targetColIndex);
            Object targetValueObject;
            try {
                targetValueObject = TypeParser.parse(targetValStr, targetColumn.getType());
            } catch (DatabaseOperationException e) {
                System.out.println("ERROR: Parsing target value: " + e.getMessage());
                return;
            }
            int updatedCount = 0;
            try {
                int rowCount = table.getRows().size();
                for (int i = 0; i < rowCount; i++) {
                    Row row = table.getRow(i);
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchVal, searchColumn.getType())) {
                        row.setValue(targetColIndex, targetValueObject);
                        updatedCount++;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("ERROR: During update", e);
            }
            if (updatedCount > 0) {
                database.dataModified(tableName);
                System.out.println("Updated " + updatedCount + " row(s).");
            } else {
                System.out.println("WARNING: No rows matched criteria.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}