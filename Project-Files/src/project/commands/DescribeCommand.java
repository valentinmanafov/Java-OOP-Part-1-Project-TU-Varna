package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for displaying the structure (schema) of a table.
 */
public class DescribeCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a DescribeCommand.
     * @param database The database instance to query for table structure.
     */
    public DescribeCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the describe command.
     * Displays the name and data type of each column in the specified table, along with their indices.
     * Usage: describe &lt;table&gt;
     * @param args Command arguments: table name.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: describe <table>");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName);

            System.out.println("Structure for Table: '" + table.getName() + "'");
            List<Column> columns = table.getColumns();

            if (columns.isEmpty()) {
                System.out.println("  (No columns defined for this table)");
            } else {
                System.out.println("Columns:");
                for (int i = 0; i < columns.size(); i++) {
                    Column col = columns.get(i);
                    System.out.printf("  [%d] %s (%s)\n", i, col.getName(), col.getType());
                }
            }
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}