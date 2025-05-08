package project.commands;

import project.*;

import java.util.*;

public class ExportCommand implements CommandHandler {

    private final Database database;

    public ExportCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: export <table_name> <filename.txt>");
                return;
            }
            String tableName = args[0];
            String filename = args[1];
            Table table = database.getTable(tableName);
            FileHandler.writeTableToFile(table, filename);
        } catch (DatabaseOperationException e) {
            System.out.println("Error exporting table: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during export: " + e.getMessage());
        }
    }
}