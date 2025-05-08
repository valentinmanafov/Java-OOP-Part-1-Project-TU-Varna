package project.commands;

import project.*;

import java.util.*;

public class DescribeCommand implements CommandHandler {

    private final Database database;

    public DescribeCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: describe <table_name>");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName);
            System.out.println("Structure for Table: '" + table.getName() + "'");
            List<Column> columns = table.getColumns();
            if (columns.isEmpty()) {
                System.out.println("  (No columns defined)");
            } else {
                System.out.println("Columns:");
                for (int i = 0; i < columns.size(); i++) {
                    Column col = columns.get(i);
                    System.out.printf("  [%d] %s (%s)\n", i, col.getName(), col.getType());
                }
            }
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}