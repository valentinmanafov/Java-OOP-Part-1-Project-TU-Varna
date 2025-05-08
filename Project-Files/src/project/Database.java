package project;

import java.util.*;
import java.io.File;

public class Database {

    private Map<String, Table> loadedTablesInMemory;
    private Map<String, String> tableRegistry;
    private String currentCatalogFilePath = null;
    private Set<String> modifiedLoadedTables;
    private boolean hasUnsavedCatalogChanges = false;

    public Database() {
        this.loadedTablesInMemory = new HashMap<>();
        this.tableRegistry = new LinkedHashMap<>();
        this.modifiedLoadedTables = new HashSet<>();
    }

    public boolean isCatalogOpen() {
        return this.currentCatalogFilePath != null;
    }

    public String getCurrentCatalogFilePath() {
        return currentCatalogFilePath;
    }

    public boolean hasUnsavedChanges() {
        return this.hasUnsavedCatalogChanges || !this.modifiedLoadedTables.isEmpty();
    }

    public void loadCatalog(String filePath, Map<String, String> registry) {
        closeCatalogInternal();
        this.currentCatalogFilePath = filePath;
        this.tableRegistry = new LinkedHashMap<>(registry);
        this.hasUnsavedCatalogChanges = false;
        this.modifiedLoadedTables.clear();
        this.loadedTablesInMemory.clear();
        System.out.println("Successfully opened database: " + new File(filePath).getName());
        if (tableRegistry.isEmpty()) {
            System.out.println("WARNING: Database is empty");
        } else {
            System.out.println("Registered tables: " + String.join(", ", tableRegistry.keySet()));

        }
    }

    public void closeCatalog() {
        if (!isCatalogOpen()) {
            System.out.println("WARING: No database file is currently open.");
            return;
        }
        String closedFileName = new File(currentCatalogFilePath).getName();
        closeCatalogInternal();
        System.out.println("Successfully closed database: " + closedFileName);
    }

    private void closeCatalogInternal() {
        this.currentCatalogFilePath = null;
        this.tableRegistry.clear();
        this.loadedTablesInMemory.clear();
        this.modifiedLoadedTables.clear();
        this.hasUnsavedCatalogChanges = false;
        System.gc();
    }

    public void markCatalogAsSaved(String filePath) {
        this.currentCatalogFilePath = filePath;
        this.hasUnsavedCatalogChanges = false;
        this.modifiedLoadedTables.clear();
    }

    private void markCatalogStructureChanged() {
        if (isCatalogOpen()) {
            this.hasUnsavedCatalogChanges = true;
        }
    }

    public void markTableDataModified(String tableName) {
        if (isCatalogOpen() && loadedTablesInMemory.containsKey(tableName)) {
            this.modifiedLoadedTables.add(tableName);
            this.hasUnsavedCatalogChanges = true;
        }
    }

    public Table getTable(String name) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database file open.");
        if (!tableRegistry.containsKey(name))
            throw new DatabaseOperationException("ERROR: Table '" + name + "' not registered in the database.");
        if (loadedTablesInMemory.containsKey(name)) {
            return loadedTablesInMemory.get(name);
        }
        String filePath = tableRegistry.get(name);
        if (filePath == null)
            throw new DatabaseOperationException("ERROR: No file path registered for table '" + name + "'.");
        System.out.println("Loading table '" + name + "' from " + filePath + "...");
        Table loadedTable = FileHandler.readTableFromFile(filePath);
        if (!loadedTable.getName().equals(name)) {
            System.out.println("WARNING: Table name in file ('" + loadedTable.getName() + "') differs from registered name ('" + name + "'). Using registered name.");
            loadedTable.setName(name);
        }
        loadedTablesInMemory.put(name, loadedTable);
        return loadedTable;
    }

    public void registerNewTable(Table table, String filePath) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database file open.");
        if (table == null || table.getName() == null)
            throw new DatabaseOperationException("ERROR: Cannot register null table.");
        String tableName = table.getName();
        if (tableRegistry.containsKey(tableName))
            throw new DatabaseOperationException("ERROR: Table name '" + tableName + "' already exists in database.");
        tableRegistry.put(tableName, filePath);
        loadedTablesInMemory.put(tableName, table);
        markCatalogStructureChanged();
    }

    public void registerImportedTable(Table table, String filePath) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database file open.");
        if (table == null || table.getName() == null)
            throw new DatabaseOperationException("ERROR: Cannot register null table.");
        String tableName = table.getName();
        if (tableRegistry.containsKey(tableName))
            throw new DatabaseOperationException("ERROR: Table name '" + tableName + "' already exists in database.");
        tableRegistry.put(tableName, filePath);
        loadedTablesInMemory.put(tableName, table);
        markCatalogStructureChanged();
    }

    public void removeTableRegistration(String name) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database file open.");
        if (!tableRegistry.containsKey(name))
            throw new DatabaseOperationException("ERROR: Table '" + name + "' not found in database.");
        tableRegistry.remove(name);
        loadedTablesInMemory.remove(name);
        modifiedLoadedTables.remove(name);
        markCatalogStructureChanged();
        System.out.println("Table '" + name + "' removed from database.");
    }

    public void renameTableRegistration(String oldName, String newName, String newFilePath) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database file open.");
        if (oldName == null || newName == null || oldName.trim().isEmpty() || newName.trim().isEmpty())
            throw new DatabaseOperationException("ERROR: Table names cannot be empty.");
        if (oldName.equalsIgnoreCase(newName)) return;
        if (!tableRegistry.containsKey(oldName))
            throw new DatabaseOperationException("ERROR: Table '" + oldName + "' not found.");
        if (tableRegistry.containsKey(newName))
            throw new DatabaseOperationException("ERROR: Table name '" + newName + "' already exists.");
        tableRegistry.remove(oldName);
        tableRegistry.put(newName, newFilePath);
        if (loadedTablesInMemory.containsKey(oldName)) {
            Table table = loadedTablesInMemory.remove(oldName);
            try {
                table.setName(newName);
            } catch (IllegalArgumentException e) {
            }
            loadedTablesInMemory.put(newName, table);
            if (modifiedLoadedTables.remove(oldName)) modifiedLoadedTables.add(newName);
        }
        markCatalogStructureChanged();
        System.out.println("Table registration renamed from '" + oldName + "' to '" + newName + "'.");
    }

    public Map<String, String> getTableRegistry() {
        return Collections.unmodifiableMap(tableRegistry);
    }

    public Set<String> getModifiedLoadedTableNames() {
        return Collections.unmodifiableSet(modifiedLoadedTables);
    }

    public Table getLoadedTable(String name) {
        return loadedTablesInMemory.get(name);
    }

    public Set<String> getTableNames() {
        return Collections.unmodifiableSet(tableRegistry.keySet());
    }

    public String getTableFilePath(String tableName) throws DatabaseOperationException {
        if (!tableRegistry.containsKey(tableName)) {
            throw new DatabaseOperationException("ERROR: Table '" + tableName + "' not registered in database.");
        }
        return tableRegistry.get(tableName);
    }

    public void dataModified(String tableName) throws DatabaseOperationException {
        if (!isCatalogOpen()) throw new DatabaseOperationException("ERROR: Cannot modify data: No database file open.");
        if (!tableRegistry.containsKey(tableName))
            throw new DatabaseOperationException("ERROR: Cannot modify data: Table '" + tableName + "' not registered.");
        if (loadedTablesInMemory.containsKey(tableName)) {
            this.modifiedLoadedTables.add(tableName);
            this.hasUnsavedCatalogChanges = true;
        } else {
            System.out.println("WARNING: Modification marked for table '" + tableName + "' not loaded in memory.");
        }
    }

    public Map<String, Table> getLoadedTables() {
        return Collections.unmodifiableMap(loadedTablesInMemory);
    }
}