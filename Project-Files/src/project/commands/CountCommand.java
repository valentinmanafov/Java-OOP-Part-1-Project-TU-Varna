package project.commands;

import project.*;

public class CountCommand implements CommandHandler {

    private final Database database;

    public CountCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: count <table> <column> <value>");
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
                System.out.println("ERROR: Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(searchColIndex);
            int count = 0;
            try {
                for (Row row : table.getRows()) {
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchValue, searchColumn.getType())) {
                        count++;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("ERROR: During count", e);
            }
            System.out.println("Count: " + count);
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}