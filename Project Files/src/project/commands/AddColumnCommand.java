package project.commands;

import project.*;
import java.util.*;

public class AddColumnCommand implements CommandHandler {

    private final Database database;

    public AddColumnCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: addcolumn <table_name> <col_name> <type>");
                System.out.println("Types: Integer, Double, String");
                return;
            }
            String tableName = args[0];
            String colName = args[1];
            String typeName = args[2];
            Table table = database.getTable(tableName);
            DataType colType;
            try {
                colType = DataType.valueOf(typeName.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type: " + typeName);
                return;
            }
            table.addColumn(new Column(colName, colType));
            System.out.println("Column '" + colName + "' added to '" + tableName + "'.");
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}