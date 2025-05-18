package project;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages the database, including its catalog of tables,
 * loaded tables in memory, and tracking unsaved changes.
 */
public class Database {

    private String currentCatalogFilePath;
    private Map<String, String> tableFilePathsRegistry; // Stores table file path
    private Map<String, Table> allTablesInMemory;       // Stores table object
    private boolean hasUnsavedChangesGlobal;

    /**
     * Constructs a new Database instance, initializing internal structures.
     */
    public Database() {
        this.currentCatalogFilePath = null;
        this.tableFilePathsRegistry = new LinkedHashMap<>(); // Preserves insertion order for catalog writing
        this.allTablesInMemory = new HashMap<>();
        this.hasUnsavedChangesGlobal = false;
    }

    /**
     * Checks if a database catalog file is currently open.
     * @return True if a catalog is open, false otherwise.
     */
    public boolean isCatalogOpen() {
        return this.currentCatalogFilePath != null;
    }

    /**
     * Gets the file path of the currently open database catalog.
     * @return The file path, or null if no catalog is open.
     */
    public String getCurrentCatalogFilePath() {
        return currentCatalogFilePath;
    }

    /**
     * Checks if there are any unsaved changes in the database (either to table data or catalog structure).
     * @return True if there are unsaved changes, false otherwise.
     */
    public boolean hasUnsavedChanges() {
        return this.hasUnsavedChangesGlobal;
    }

    /**
     * Loads a database catalog from the specified file path and its associated tables into memory.
     * If a catalog is already open, it will be closed first (without saving changes).
     * It reads the catalog file to get the registry of table names and their corresponding data file paths.
     * Then, for each entry, it attempts to load the table data from its file.
     * @param filePath The path to the database catalog file.
     * @param registry A map containing table names as keys and their file paths as values, typically read from the catalog file.
     * @throws DatabaseOperationException If any error occurs during catalog loading or table loading.
     */
    public void loadCatalog(String filePath, Map<String, String> registry) throws DatabaseOperationException {
        closeDatabaseInternal();

        this.currentCatalogFilePath = filePath;
        this.tableFilePathsRegistry = new LinkedHashMap<>(registry); // Use a new map from the loaded registry

        System.out.println("Database catalog '" + new File(filePath).getName() + "' definition loaded.");

        if (this.tableFilePathsRegistry.isEmpty()) {
            System.out.println("WARNING: Database catalog is empty.");
        }

        // Iterate through the registry and load each table.
        // Handles cases where table name in file might differ from registered name.
        for (Map.Entry<String, String> entry : this.tableFilePathsRegistry.entrySet()) {
            String tableName = entry.getKey();
            String tableDataFilePath = entry.getValue();
            try {
                System.out.println("Loading table '" + tableName + "' from " + tableDataFilePath + "...");
                Table table = FileHandler.readTableFromFile(tableDataFilePath);
                if (!table.getName().equals(tableName)) {
                    System.out.println("WARNING: Table name in file ('" + table.getName() +
                            "') differs from registered name ('" + tableName + "'). Using registered name.");
                    table.setName(tableName);
                }
                this.allTablesInMemory.put(tableName, table);
            } catch (DatabaseOperationException e) {
                closeDatabaseInternal();
                throw new DatabaseOperationException("Failed to load table '" + tableName + "' from '" + tableDataFilePath + "': " + e.getMessage(), e);
            }
        }

        this.hasUnsavedChangesGlobal = false;
        System.out.println("All tables for database '" + new File(filePath).getName() + "' loaded successfully. " + this.allTablesInMemory.size() + " table(s) in memory.");
    }


    /**
     * Closes the currently open database catalog.
     * This clears all loaded tables from memory and resets the unsaved changes flag.
     * It does not automatically save changes, that must be done explicitly before closing if desired.
     */
    public void closeCatalog() {
        if (!isCatalogOpen()) {
            System.out.println("WARNING: No database file is currently open.");
            return;
        }
        String closedFileName = new File(currentCatalogFilePath).getName();
        closeDatabaseInternal();
        System.out.println("Successfully closed database: " + closedFileName);
    }

    /**
     * Internal helper method to reset the database state.
     * Clears the current catalog path, table registry, in-memory tables, and unsaved changes flag.
     */
    private void closeDatabaseInternal() {
        this.currentCatalogFilePath = null;
        this.tableFilePathsRegistry.clear();
        this.allTablesInMemory.clear();
        this.hasUnsavedChangesGlobal = false;
    }

    /**
     * Marks the current catalog as saved to the specified file path.
     * This typically occurs after a 'save' or 'saveas' operation.
     * @param filePath The path where the catalog was saved.
     */
    public void markCatalogAsSaved(String filePath) {
        this.currentCatalogFilePath = filePath;
        this.hasUnsavedChangesGlobal = false;
    }

    /**
     * Retrieves a table by its name from the in-memory store.
     * @param name The name of the table to retrieve.
     * @return The Table object.
     * @throws DatabaseOperationException If no catalog is open, or if the table is not found or not loaded.
     */
    public Table getTable(String name) throws DatabaseOperationException {
        if (!isCatalogOpen()) {
            throw new DatabaseOperationException("ERROR: No database file open.");
        }
        Table table = this.allTablesInMemory.get(name);
        if (table == null) {
            if (this.tableFilePathsRegistry.containsKey(name)) {
                throw new DatabaseOperationException("ERROR: Table '" + name + "' is registered but was not found in memory (load may have failed during open operation).");
            } else {
                throw new DatabaseOperationException("ERROR: Table '" + name + "' not found in the database (not listed in catalog).");
            }
        }
        return table;
    }

    /**
     * Registers a newly created table in the database.
     * The table is added to the in-memory store and its path to the file registry.
     * Marks the database as having unsaved changes.
     * @param table The Table object to register.
     * @param filePath The file path where this table will be/is saved.
     * @throws DatabaseOperationException If no catalog is open, the table name is invalid, or the table name already exists.
     */
    public void registerNewTable(Table table, String filePath) throws DatabaseOperationException {
        if (!isCatalogOpen()) {
            throw new DatabaseOperationException("ERROR: No database file open to register new table.");
        }
        String tableName = table.getName();
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new DatabaseOperationException("ERROR: Table name cannot be null or empty for registration.");
        }
        if (this.tableFilePathsRegistry.containsKey(tableName) || this.allTablesInMemory.containsKey(tableName)) {
            throw new DatabaseOperationException("ERROR: Table name '" + tableName + "' already exists in the database.");
        }
        this.tableFilePathsRegistry.put(tableName, filePath);
        this.allTablesInMemory.put(tableName, table);
        this.hasUnsavedChangesGlobal = true;
    }

    /**
     * Registers an imported table into the database.
     * Similar to {@link #registerNewTable(Table, String)}, but typically used for tables read from external files not initially part of the catalog.
     * Marks the database as having unsaved changes.
     * @param table The imported Table object.
     * @param filePath The file path from which this table was imported.
     * @throws DatabaseOperationException If no catalog is open, the table name is invalid, or the table name already exists.
     */
    public void registerImportedTable(Table table, String filePath) throws DatabaseOperationException {
        if (!isCatalogOpen()) {
            throw new DatabaseOperationException("ERROR: No database file open to import table into.");
        }
        String tableName = table.getName();
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new DatabaseOperationException("ERROR: Imported table name cannot be null or empty.");
        }
        if (this.tableFilePathsRegistry.containsKey(tableName) || this.allTablesInMemory.containsKey(tableName)) {
            throw new DatabaseOperationException("ERROR: Table name '" + tableName + "' already exists in the database. Cannot import.");
        }
        this.tableFilePathsRegistry.put(tableName, filePath);
        this.allTablesInMemory.put(tableName, table);
        this.hasUnsavedChangesGlobal = true;
    }

    /**
     * Marks that data within a specific table has been modified.
     * This sets the global unsaved changes flag for the database.
     * @param tableName The name of the table that was modified.
     * @throws DatabaseOperationException If no catalog is open or if the table is not known/loaded.
     */
    public void dataModified(String tableName) throws DatabaseOperationException {
        if (!isCatalogOpen()) {
            throw new DatabaseOperationException("ERROR: No database file open to modify data.");
        }
        if (!this.allTablesInMemory.containsKey(tableName)) {
            throw new DatabaseOperationException("ERROR: Cannot mark data modified for an unknown or unloaded table: '" + tableName + "'.");
        }
        this.hasUnsavedChangesGlobal = true;
    }

    /**
     * Removes a table's registration from the database.
     * This removes the table from the in-memory store and the file path registry.
     * It does not delete the table's data file from disk.
     * Marks the database as having unsaved changes.
     * @param name The name of the table to remove.
     * @throws DatabaseOperationException If no catalog is open or if the table is not found.
     */
    public void removeTableRegistration(String name) throws DatabaseOperationException {
        if (!isCatalogOpen()) {
            throw new DatabaseOperationException("ERROR: No database file open.");
        }
        if (!this.tableFilePathsRegistry.containsKey(name) && !this.allTablesInMemory.containsKey(name)) {
            throw new DatabaseOperationException("ERROR: Table '" + name + "' not found for removal.");
        }
        this.tableFilePathsRegistry.remove(name);
        this.allTablesInMemory.remove(name);
        this.hasUnsavedChangesGlobal = true;
        System.out.println("Table '" + name + "' removed from database (memory and registration).");
    }

    /**
     * Renames a table in the database, updating its registration and in-memory representation.
     * This includes changing the table's name in the file path registry and updating the {@link Table} object's name.
     * Marks the database as having unsaved changes.
     * @param oldName The current name of the table.
     * @param newName The new name for the table.
     * @param newFilePath The new file path for the table's data file (often derived from newName).
     * @throws DatabaseOperationException If no catalog is open, names are invalid, old name not found, or new name already exists.
     */
    public void renameTableRegistration(String oldName, String newName, String newFilePath) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database file open.");
        if (oldName == null || newName == null || oldName.trim().isEmpty() || newName.trim().isEmpty())
            throw new DatabaseOperationException("ERROR: Table names cannot be null or empty for renaming.");

        //Handle renaming to the same name
        if (oldName.equalsIgnoreCase(newName)) {
            if (this.tableFilePathsRegistry.containsKey(oldName) && !this.tableFilePathsRegistry.get(oldName).equals(newFilePath)) {
                this.tableFilePathsRegistry.put(oldName, newFilePath);
                this.hasUnsavedChangesGlobal = true;
                System.out.println("Table '" + oldName + "' file path in registry updated to '" + newFilePath + "'.");
            }
            return;
        }

        if (!this.allTablesInMemory.containsKey(oldName) || !this.tableFilePathsRegistry.containsKey(oldName))
            throw new DatabaseOperationException("ERROR: Table '" + oldName + "' not found for renaming.");
        if (this.allTablesInMemory.containsKey(newName) || this.tableFilePathsRegistry.containsKey(newName))
            throw new DatabaseOperationException("ERROR: Target table name '" + newName + "' already exists.");

        Table tableToRename = this.allTablesInMemory.remove(oldName);
        this.tableFilePathsRegistry.remove(oldName);

        tableToRename.setName(newName); // Update the name within the Table object itself
        this.allTablesInMemory.put(newName, tableToRename);
        this.tableFilePathsRegistry.put(newName, newFilePath); // Register under new name and path

        this.hasUnsavedChangesGlobal = true;
        System.out.println("Table renamed from '" + oldName + "' to '" + newName + "'.");
    }

    /**
     * Gets an unmodifiable view of the table file path registry.
     * @return An unmodifiable map of table names to their file paths.
     */
    public Map<String, String> getTableRegistry() {
        return Collections.unmodifiableMap(this.tableFilePathsRegistry);
    }

    /**
     * Gets an unmodifiable view of all tables currently loaded in memory.
     * @return An unmodifiable map of table names to {@link Table} objects.
     */
    public Map<String, Table> getAllTablesInMemory() {
        return Collections.unmodifiableMap(allTablesInMemory);
    }

    /**
     * Gets an unmodifiable set of the names of all tables registered in the catalog.
     * @return An unmodifiable set of table names.
     */
    public Set<String> getTableNames() {
        return Collections.unmodifiableSet(this.tableFilePathsRegistry.keySet());
    }

    /**
     * Gets the file path associated with a given table name from the registry.
     * @param tableName The name of the table.
     * @return The file path for the table's data.
     * @throws DatabaseOperationException If the table name is not found in the registry.
     */
    public String getTableFilePath(String tableName) throws DatabaseOperationException {
        String path = this.tableFilePathsRegistry.get(tableName);
        if (path == null) {
            throw new DatabaseOperationException("ERROR: Table '" + tableName + "' not found in path registry.");
        }
        return path;
    }

    /**
     * Gets a set of names for all tables currently loaded in memory that are considered modified
     * If {@code hasUnsavedChangesGlobal} is true, it returns all loaded table names.
     * @return An unmodifiable set of names of loaded tables if unsaved changes exist, otherwise an empty set.
     */
    public Set<String> getModifiedLoadedTableNames() {
        if (this.hasUnsavedChangesGlobal) {
            return Collections.unmodifiableSet(this.allTablesInMemory.keySet());
        }
        return Collections.emptySet();
    }

    /**
     * Retrieves a table directly from the in-memory map without throwing an exception if not found
     * (returns null instead). Used internally.
     * @param name The name of the table.
     * @return The {@link Table} object, or null if not found in memory.
     */
    public Table getLoadedTable(String name) {
        return this.allTablesInMemory.get(name);
    }
}