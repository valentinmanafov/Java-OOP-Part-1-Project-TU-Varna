package project.commands;

import project.*;

import java.util.*;

public class DeleteCommand implements CommandHandler {

    private final Database database;

    public DeleteCommand(Database database) {
        this.database = database;
    }

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
                System.out.println("ERROR: Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(searchColIndex);
            List<Row> remainingRows = new ArrayList<>();
            List<Row> currentRows = table.getRows();
            int originalRowCount = currentRows.size();
            try {
                for (Row row : currentRows) {
                    if (!TypeParser.looselyEquals(row.getValue(searchColIndex), searchValue, searchColumn.getType()))
                        remainingRows.add(row);
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("ERROR: During delete", e);
            }
            int deletedCount = originalRowCount - remainingRows.size();
            if (deletedCount > 0) {
                table.setRows(remainingRows);
                database.dataModified(tableName);
                System.out.println("Deleted " + deletedCount + " row(s).");
            } else {
                System.out.println("WARNING: No rows matched criteria.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}