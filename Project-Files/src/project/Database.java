package project;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class Database {

    private String currentCatalogFilePath;
    private Map<String, String> tableFilePathsRegistry;
    private Map<String, Table> allTablesInMemory;
    private boolean hasUnsavedChangesGlobal;

    public Database() {
        this.currentCatalogFilePath = null;
        this.tableFilePathsRegistry = new LinkedHashMap<>();
        this.allTablesInMemory = new HashMap<>();
        this.hasUnsavedChangesGlobal = false;
    }

    public boolean isCatalogOpen() {
        return this.currentCatalogFilePath != null;
    }

    public String getCurrentCatalogFilePath() {
        return currentCatalogFilePath;
    }

    public boolean hasUnsavedChanges() {
        return this.hasUnsavedChangesGlobal;
    }

    public void loadCatalog(String filePath, Map<String, String> registry) throws DatabaseOperationException {
        closeDatabaseInternal();

        this.currentCatalogFilePath = filePath;
        this.tableFilePathsRegistry = new LinkedHashMap<>(registry);

        System.out.println("Database catalog '" + new File(filePath).getName() + "' definition loaded.");

        if (this.tableFilePathsRegistry.isEmpty()) {
            System.out.println("WARNING: Database catalog is empty.");
        }

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


    public void closeCatalog() {
        if (!isCatalogOpen()) {
            System.out.println("WARNING: No database file is currently open.");
            return;
        }
        String closedFileName = new File(currentCatalogFilePath).getName();
        closeDatabaseInternal();
        System.out.println("Successfully closed database: " + closedFileName);
    }

    private void closeDatabaseInternal() {
        this.currentCatalogFilePath = null;
        this.tableFilePathsRegistry.clear();
        this.allTablesInMemory.clear();
        this.hasUnsavedChangesGlobal = false;
        System.gc();
    }

    public void markCatalogAsSaved(String filePath) {
        this.currentCatalogFilePath = filePath;
        this.hasUnsavedChangesGlobal = false;
    }


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

    public void dataModified(String tableName) throws DatabaseOperationException {
        if (!isCatalogOpen()) {
            throw new DatabaseOperationException("ERROR: No database file open to modify data.");
        }
        if (!this.allTablesInMemory.containsKey(tableName)) {
            throw new DatabaseOperationException("ERROR: Cannot mark data modified for an unknown or unloaded table: '" + tableName + "'.");
        }
        this.hasUnsavedChangesGlobal = true;
    }


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

    public void renameTableRegistration(String oldName, String newName, String newFilePath) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database file open.");
        if (oldName == null || newName == null || oldName.trim().isEmpty() || newName.trim().isEmpty())
            throw new DatabaseOperationException("ERROR: Table names cannot be null or empty for renaming.");

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

        tableToRename.setName(newName);
        this.allTablesInMemory.put(newName, tableToRename);
        this.tableFilePathsRegistry.put(newName, newFilePath);

        this.hasUnsavedChangesGlobal = true;
        System.out.println("Table renamed from '" + oldName + "' to '" + newName + "'.");
    }


    public Map<String, String> getTableRegistry() {
        return Collections.unmodifiableMap(this.tableFilePathsRegistry);
    }


    public Map<String, Table> getAllTablesInMemory() {
        return Collections.unmodifiableMap(allTablesInMemory);
    }


    public Set<String> getTableNames() {
        return Collections.unmodifiableSet(this.tableFilePathsRegistry.keySet());
    }


    public String getTableFilePath(String tableName) throws DatabaseOperationException {
        String path = this.tableFilePathsRegistry.get(tableName);
        if (path == null) {
            throw new DatabaseOperationException("ERROR: Table '" + tableName + "' not found in path registry.");
        }
        return path;
    }


    public Set<String> getModifiedLoadedTableNames() {
        if (this.hasUnsavedChangesGlobal) {
            return Collections.unmodifiableSet(this.allTablesInMemory.keySet());
        }
        return Collections.emptySet();
    }


    public Table getLoadedTable(String name) {
        return this.allTablesInMemory.get(name);
    }
}