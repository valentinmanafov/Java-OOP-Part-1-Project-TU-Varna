package project.commands;

import project.*;

import java.util.*;
import java.io.File;

/**
 * Command handler for performing an inner join operation on two tables.
 */
public class InnerJoinCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs an InnerJoinCommand.
     * @param database The database instance containing the tables to be joined.
     */
    public InnerJoinCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the inner join command.
     * Joins two tables based on equality of values in specified columns.
     * A new table is created to store the result of the join. Column names in the new table
     * are prefixed with their original table names to avoid ambiguity (e.g., "table1.colA").
     * The new table is automatically named (e.g., "join_table1_table2") and registered
     * in the database. Its data is also saved to a new file.
     * Usage: innerjoin &lt;table1_name&gt; &lt;table1_column_index&gt; &lt;table2_name&gt; &lt;table2_column_index&gt;
     * @param args Command arguments: name of the first table, index of join column in first table,
     * name of the second table, index of join column in second table.
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open. Use 'open <filepath>'.");
            return;
        }
        try {
            if (args.length != 4) {
                System.out.println("Usage: innerjoin <table1> <column1_index> <table2> <column2_index>");
                return;
            }
            String t1Name = args[0];
            String c1IdxStr = args[1];
            String t2Name = args[2];
            String c2IdxStr = args[3];

            Table t1 = database.getTable(t1Name);
            Table t2 = database.getTable(t2Name);

            if (t1Name.equalsIgnoreCase(t2Name)) {
                System.out.println("WARNING: Self-join is not supported by this command. Tables must be different.");
                return;
            }

            int c1Idx, c2Idx;
            try {
                c1Idx = Integer.parseInt(c1IdxStr);
                c2Idx = Integer.parseInt(c2IdxStr);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid index. Column indices must be numbers.");
                return;
            }

            Column col1 = t1.getColumn(c1Idx);
            Column col2 = t2.getColumn(c2Idx);

            // Construct columns for the new joined table.
            // Column names are prefixed with original table names to avoid clashes.
            List<Column> joinedCols = new ArrayList<>();
            for (Column c : t1.getColumns()) {
                joinedCols.add(new Column(t1Name + "." + c.getName(), c.getType()));
            }
            for (Column c : t2.getColumns()) {
                joinedCols.add(new Column(t2Name + "." + c.getName(), c.getType()));
            }

            String finalJoinedTableName = generateUniqueJoinName(t1Name, t2Name);
            String finalJoinedTablePath = finalJoinedTableName + ".txt";
            Table joinedTable = new Table(finalJoinedTableName, joinedCols);
            int rowsJoined = 0;

            // Perform the join by iterating through rows of both tables.
            try {
                for (Row r1 : t1.getRows()) {
                    Object v1 = r1.getValue(c1Idx);
                    if (v1 == null) continue;

                    for (Row r2 : t2.getRows()) {
                        Object v2 = r2.getValue(c2Idx);
                        if (v2 == null) continue;

                        // If the values in the join columns are equal, create a new combined row.
                        if (v1.equals(v2)) {
                            List<Object> vals = new ArrayList<>(r1.getValues());
                            vals.addAll(r2.getValues());
                            joinedTable.addRow(new Row(vals));
                            rowsJoined++;
                        }
                    }
                }
            } catch (IndexOutOfBoundsException | DatabaseOperationException e) {
                throw new DatabaseOperationException("ERROR: An issue occurred during the join operation process.", e);
            }

            database.registerNewTable(joinedTable, finalJoinedTablePath);
            System.out.println("Inner join completed. New table '" + finalJoinedTableName + "' created with " + rowsJoined + " rows.");
            FileHandler.writeTableToFile(joinedTable, finalJoinedTablePath);
            System.out.println("Joined table '" + finalJoinedTableName + "' saved to '" + finalJoinedTablePath + "'.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Generates a unique name for the joined table to avoid naming conflicts.
     * Starts with "join_table1_table2" and appends a suffix (_2, _3, etc.) if the name already exists.
     * @param t1 Name of the first table.
     * @param t2 Name of the second table.
     * @return A unique name for the resulting joined table.
     * @throws DatabaseOperationException if database access fails.
     */
    private String generateUniqueJoinName(String t1, String t2) throws DatabaseOperationException {
        String base = "join_" + t1 + "_" + t2;
        String name = base;
        int suffix = 1;
        Set<String> existingNames = database.getTableNames();
        while (existingNames.contains(name)) {
            name = base + "_" + (++suffix);
        }
        return name;
    }
}