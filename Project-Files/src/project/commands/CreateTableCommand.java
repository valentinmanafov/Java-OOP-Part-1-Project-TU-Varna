package project.commands;

import project.*;
import java.util.*;

public class CreateTableCommand implements CommandHandler {

    private final Database database;

    public CreateTableCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open. Use 'open <filepath>'.");
            return;
        }
        try {
            if (args.length < 3 || args.length % 2 == 0) {
                System.out.println("Usage: createtable <table_name> <col1_name> <col1_type> ...");
                return;
            }
            String tableName = args[0];
            List<Column> columns = new ArrayList<>();
            for (int i = 1; i < args.length; i += 2) {
                String colName = args[i];
                String typeName = args[i + 1];
                DataType colType;
                try {
                    colType = DataType.valueOf(typeName.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: Invalid type: " + typeName);
                    return;
                }
                for (Column existing : columns) {
                    if (existing.getName().equalsIgnoreCase(colName)) {
                        System.out.println("ERROR: Duplicate column name '" + colName + "'.");
                        return;
                    }
                }
                columns.add(new Column(colName, colType));
            }
            if (columns.isEmpty()) {
                System.out.println("ERROR: Must define at least one column.");
                return;
            }

            Table newTable = new Table(tableName, columns);
            String defaultTablePath = tableName + ".txt";

            database.registerNewTable(newTable, defaultTablePath);

            System.out.println("Table '" + tableName + "' created successfully and registered.");
            FileHandler.writeTableToFile(newTable, defaultTablePath);

        } catch (DatabaseOperationException | IllegalArgumentException e) {
            System.out.println("ERROR: creating table: " + e.getMessage());
        }
    }
}