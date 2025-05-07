package project;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private String name;
    private List<Column> columns;
    private List<Row> rows;

    public Table(String name, List<Column> columns) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("project.Table name cannot be null or empty.");
        if (columns == null) throw new IllegalArgumentException("project.Column list cannot be null.");
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    public Table(String name, List<Column> columns, List<Row> rows) {
        this(name, columns);
        if (rows == null) throw new IllegalArgumentException("project.Row list cannot be null.");
        this.rows = new ArrayList<>(rows);
    }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("project.Table name cannot be null or empty.");
        this.name = name;
    }

    public List<Column> getColumns() { return new ArrayList<>(columns); }
    public List<Row> getRows() { return new ArrayList<>(rows); }

    public Row getRow(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < rows.size()) return rows.get(index);
        throw new IndexOutOfBoundsException("project.Row index out of bounds: " + index);
    }

    public void setRows(List<Row> newRows) {
        if (newRows == null) throw new IllegalArgumentException("New row list cannot be null.");
        this.rows = new ArrayList<>(newRows);
    }

    public Column getColumn(int index) throws DatabaseOperationException {
        if (index >= 0 && index < columns.size()) return columns.get(index);
        throw new DatabaseOperationException("project.Column index " + index + " is out of bounds for table '" + name + "'.");
    }

    public int getColumnIndex(String columnName) throws DatabaseOperationException {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) return i;
        }
        throw new DatabaseOperationException("project.Column '" + columnName + "' not found in table '" + name + "'.");
    }

    public void addRow(Row row) throws DatabaseOperationException {
        if (row.size() != this.columns.size()) {
            throw new DatabaseOperationException("Cannot add row: project.Row size (" + row.size()
                    + ") does not match table column count (" + this.columns.size() + ").");
        }
        this.rows.add(row);
    }

    public void addColumn(Column column) throws DatabaseOperationException {
        for (Column existingCol : this.columns) {
            if (existingCol.getName().equalsIgnoreCase(column.getName())) {
                throw new DatabaseOperationException("project.Column '" + column.getName() + "' already exists in table '" + name + "'.");
            }
        }
        this.columns.add(column);
        for (Row row : this.rows) { row.addValue(null); }
    }

    public boolean removeRow(Row row) { return this.rows.remove(row); }
}