package project.commands;

import project.*;

import java.io.File;

public class RenameCommand implements CommandHandler {

    private final Database database;

    public RenameCommand(Database database) {
        this.database = database;
    }

    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("Error: No catalog file open. Use 'open <filepath>'.");
            return;
        }
        try {
            if (args.length != 2) {
                System.out.println("Usage: rename <old_table_name> <new_table_name>");
                return;
            }
            String oldName = args[0];
            String newName = args[1];

            String oldFilePath = database.getTableFilePath(oldName);
            String newFilePath = newName + ".txt";

            database.renameTableRegistration(oldName, newName, newFilePath);

            File oldFile = new File(oldFilePath);
            File newFile = new File(newFilePath);

            if (oldFile.exists()) {
                if (oldFile.renameTo(newFile)) {
                    System.out.println("Table file renamed from '" + oldFile.getName() + "' to '" + newFile.getName() + "'.");
                } else {
                    System.out.println("Warning: Could not rename table file '" + oldFilePath + "' on disk automatically.");
                    System.out.println("Registry updated, but manual file rename might be needed.");
                    try {
                        Table renamedTable = database.getLoadedTable(newName);
                        if (renamedTable != null) {
                            FileHandler.writeTableToFile(renamedTable, newFilePath);
                            System.out.println("Saved current table data to new file: " + newFilePath);
                        }
                    } catch (DatabaseOperationException writeEx) {
                        System.out.println("Error saving table to new file path after failed rename: " + writeEx.getMessage());
                    }
                }
            } else {
                System.out.println("Warning: Original table file '" + oldFilePath + "' not found. Registry updated.");
                try {
                    Table renamedTable = database.getLoadedTable(newName);
                    if (renamedTable != null) {
                        FileHandler.writeTableToFile(renamedTable, newFilePath);
                        System.out.println("Saved current table data to new file: " + newFilePath);
                    }
                } catch (DatabaseOperationException writeEx) {
                    System.out.println("Error saving table to new file path: " + writeEx.getMessage());
                }
            }
        } catch (DatabaseOperationException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during rename: " + e.getMessage());
        }
    }
}