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
        try {
            if (args.length < 3 || args.length % 2 == 0) {
                System.out.println("Usage: createtable <table_name> <col1_name> <col1_type> [col2_name col2_type] ...");
                System.out.println("Types: Integer, Double, String");
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
                    System.out.println("Invalid type '" + typeName + "' for column '" + colName + "'.");
                    return;
                }
                for(Column existing : columns) {
                    if (existing.getName().equalsIgnoreCase(colName)) {
                        System.out.println("Error: Duplicate column name '" + colName + "' in definition.");
                        return;
                    }
                }
                columns.add(new Column(colName, colType));
            }
            if (columns.isEmpty()) {
                System.out.println("Error: Must define at least one column.");
                return;
            }
            Table newTable = new Table(tableName, columns);
            database.addTable(newTable);
            System.out.println("Table '" + tableName + "' created successfully.");

        } catch (DatabaseOperationException | IllegalArgumentException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }
}