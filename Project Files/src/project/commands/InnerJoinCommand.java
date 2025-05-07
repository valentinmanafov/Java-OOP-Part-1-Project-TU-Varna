package project.commands;

import project.*;
import java.util.*;

public class InnerJoinCommand implements CommandHandler {

    private final Database database;

    public InnerJoinCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 4) {
                System.out.println("Usage: innerjoin <tbl1> <idx1> <tbl2> <idx2>");
                return;
            }
            String t1Name = args[0];
            String c1IdxStr = args[1];
            String t2Name = args[2];
            String c2IdxStr = args[3];

            Table t1 = database.getTable(t1Name);
            Table t2 = database.getTable(t2Name);

            if (t1Name.equalsIgnoreCase(t2Name)) {
                System.out.println("Cannot self-join.");
                return;
            }

            int c1Idx, c2Idx;
            try {
                c1Idx = Integer.parseInt(c1IdxStr);
                c2Idx = Integer.parseInt(c2IdxStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid index.");
                return;
            }
            Column col1 = t1.getColumn(c1Idx);
            Column col2 = t2.getColumn(c2Idx);

            List<Column> joinedCols = new ArrayList<>();
            for (Column c : t1.getColumns()) {
                joinedCols.add(new Column(t1Name + "." + c.getName(), c.getType()));
            }
            for (Column c : t2.getColumns()) {
                joinedCols.add(new Column(t2Name + "." + c.getName(), c.getType()));
            }

            String finalJoinedTableName = generateUniqueJoinName(t1Name, t2Name);
            Table joinedTable = new Table(finalJoinedTableName, joinedCols);
            int rowsJoined = 0;
            try {
                for (Row r1 : t1.getRows()) {
                    Object v1 = r1.getValue(c1Idx);
                    if (v1 == null) continue;
                    for (Row r2 : t2.getRows()) {
                        Object v2 = r2.getValue(c2Idx);
                        if (v2 == null) continue;
                        if (v1.equals(v2)) {
                            List<Object> vals = new ArrayList<>(r1.getValues());
                            vals.addAll(r2.getValues());
                            joinedTable.addRow(new Row(vals));
                            rowsJoined++;
                        }
                    }
                }
            } catch (IndexOutOfBoundsException | DatabaseOperationException e) {
                throw new DatabaseOperationException("Internal error during join", e);
            }
            database.addTable(joinedTable);
            System.out.println("Inner join created table: '" + finalJoinedTableName + "' (" + rowsJoined + " rows).");
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private String generateUniqueJoinName(String t1, String t2) throws DatabaseOperationException {
        String base = "join_" + t1 + "_" + t2;
        String name = base;
        int suffix = 1;
        while (true) {
            try {
                database.getTable(name);
                name = base + "_" + (++suffix);
            } catch (DatabaseOperationException e) {
                return name;
            }
        }
    }
}