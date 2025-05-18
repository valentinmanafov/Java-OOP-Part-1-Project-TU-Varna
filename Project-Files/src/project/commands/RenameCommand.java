package project.commands;

import project.*;

import java.io.File;

/**
 * Command handler for renaming an existing table in the database.
 */
public class RenameCommand implements CommandHandler {

    private final Database database;

    /**
     * Constructs a RenameCommand.
     * @param database The database instance where the table rename operation will occur.
     */
    public RenameCommand(Database database) {
        this.database = database;
    }

    /**
     * Executes the rename command.
     * Renames a table from an old name to a new name. This involves:
     * 1. Updating the table's registration in the database (in-memory name and file path).
     * 2. Attempting to rename the corresponding data file on disk.
     * If the disk file rename fails, a warning is issued, and an attempt is made to save
     * the table's current data to the new file path.
     * Usage: rename &lt;old_table_name&gt; &lt;new_table_name&gt;
     * @param args Command arguments: old table name, new table name.
     */
    @Override
    public void execute(String[] args) {
        if (!database.isCatalogOpen()) {
            System.out.println("ERROR: No database file open. Use 'open <filepath>'.");
            return;
        }
        try {
            if (args.length != 2) {
                System.out.println("Usage: rename <old_table_name> <new_table_name>");
                return;
            }
            String oldName = args[0];
            String newName = args[1];

            if (oldName.equalsIgnoreCase(newName)) {
                System.out.println("WARNING: New name is the same as the old name. No rename operation performed.");
                return;
            }

            String oldFilePath = database.getTableFilePath(oldName);
            String newFilePath = newName + ".txt";


            database.renameTableRegistration(oldName, newName, newFilePath);

            // Attempt to rename the physical file on disk.
            File oldFile = new File(oldFilePath);
            File newFile = new File(newFilePath);

            if (oldFile.exists()) {
                if (newFile.exists() && !oldFilePath.equalsIgnoreCase(newFilePath)) {
                    System.out.println("WARNING: A file named '" + newFile.getName() + "' already exists. The table file '" + oldFile.getName() + "' was not renamed on disk to avoid overwriting.");
                    System.out.println("WARNING: The table '" + oldName + "' is now known as '" + newName + "' in the database and expects its data at '" + newFilePath + "'.");
                    System.out.println("WARNING: Please resolve the file conflict manually or save the table '" + newName + "' to ensure data persistence under the new name.");
                    attemptSaveToNewPath(newName, newFilePath);
                } else if (oldFile.renameTo(newFile)) {
                    System.out.println("Table data file on disk successfully renamed from '" + oldFile.getName() + "' to '" + newFile.getName() + "'.");
                } else {
                    System.out.println("WARNING: Could not automatically rename the table data file on disk from '" + oldFilePath + "' to '" + newFilePath + "'.");
                    System.out.println("WARNING: The table is renamed in the database catalog, but you might need to manually rename the file or save the table '" + newName + "'.");
                    attemptSaveToNewPath(newName, newFilePath);
                }
            } else {
                System.out.println("WARNING: Original table data file '" + oldFilePath + "' not found on disk. The table is renamed in the catalog.");
                System.out.println("WARNING: If the table had data, ensure it's saved under the new name '" + newName + "' to the file '" + newFilePath + "'.");
                attemptSaveToNewPath(newName, newFilePath);
            }
        } catch (DatabaseOperationException | IllegalArgumentException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during rename: " + e.getMessage());
        }
    }

    /**
     * Helper method to attempt saving the renamed table's data to its new file path,
     * typically used if automatic file rename fails or the old file doesn't exist.
     * @param tableName The new name of the table.
     * @param newFilePath The new file path for the table's data.
     */
    private void attemptSaveToNewPath(String tableName, String newFilePath) {
        try {
            Table renamedTable = database.getLoadedTable(tableName);
            if (renamedTable != null) {
                FileHandler.writeTableToFile(renamedTable, newFilePath);
                System.out.println("Successfully saved current data for table '" + tableName + "' to new file: '" + newFilePath + "'.");
            } else {
                System.out.println("WARNING: Could not retrieve table '" + tableName + "' from memory to save to '" + newFilePath + "'.");
            }
        } catch (DatabaseOperationException writeEx) {
            System.out.println("ERROR: Attempting to save table '" + tableName + "' to '" + newFilePath + "' after rename issues: " + writeEx.getMessage());
        }
    }
}