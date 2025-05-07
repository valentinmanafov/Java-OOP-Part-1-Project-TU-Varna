package project.commands;

import project.CommandHandler;
import project.Database;
import project.DatabaseOperationException;

public class RenameCommand implements CommandHandler {

    private final Database database;

    public RenameCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: rename <old_table_name> <new_table_name>");
                return;
            }
            String oldName = args[0];
            String newName = args[1];
            database.renameTable(oldName, newName);
        } catch (DatabaseOperationException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}