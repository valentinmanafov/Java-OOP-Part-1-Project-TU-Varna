package project;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

public class Database {
    private Map<String, Table> tables;

    public Database() { this.tables = new HashMap<>(); }

    public void addTable(Table table) throws DatabaseOperationException {
        if (table == null || table.getName() == null) throw new DatabaseOperationException("Cannot add null table or table with null name.");
        String tableName = table.getName();
        if (tables.containsKey(tableName)) throw new DatabaseOperationException("project.Table named '" + tableName + "' already exists.");
        tables.put(tableName, table);
    }

    public Table getTable(String name) throws DatabaseOperationException {
        Table table = tables.get(name);
        if (table == null) throw new DatabaseOperationException("project.Table '" + name + "' not found.");
        return table;
    }

    public void removeTable(String name) throws DatabaseOperationException {
        if (tables.remove(name) == null) throw new DatabaseOperationException("project.Table '" + name + "' not found.");
    }

    public Set<String> getTableNames() { return Collections.unmodifiableSet(tables.keySet()); }

    public void renameTable(String oldName, String newName) throws DatabaseOperationException {
        if (oldName == null || newName == null || oldName.trim().isEmpty() || newName.trim().isEmpty()) throw new DatabaseOperationException("project.Table names cannot be null or empty.");
        if (oldName.equalsIgnoreCase(newName)) return;

        Table table = tables.get(oldName);
        if (table == null) throw new DatabaseOperationException("project.Table '" + oldName + "' not found.");
        if (tables.containsKey(newName)) throw new DatabaseOperationException("project.Table named '" + newName + "' already exists.");

        tables.remove(oldName);
        try { table.setName(newName); }
        catch (IllegalArgumentException e) { tables.put(oldName, table); throw new DatabaseOperationException("Invalid new table name: " + e.getMessage(), e); }
        tables.put(newName, table);
        System.out.println("project.Table '" + oldName + "' successfully renamed to '" + newName + "'.");
    }
}