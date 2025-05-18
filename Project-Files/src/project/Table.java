package project;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a table in the database, consisting of a name, a list of columns, and a list of rows.
 */
public class Table {
    private String name;
    private List<Column> columns;
    private List<Row> rows;

    /**
     * Constructs a new Table with a name and a list of columns.
     * Initializes an empty list of rows.
     * @param name The name of the table. Cannot be null or empty.
     * @param columns The list of {@link Column} objects defining the table structure. Cannot be null.
     * @throws IllegalArgumentException If name or columns list is null/empty.
     */
    public Table(String name, List<Column> columns) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("ERROR: Table name cannot be null or empty.");
        if (columns == null)
            throw new IllegalArgumentException("ERROR: Column list cannot be null.");
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    /**
     * Constructs a new Table with a name, a list of columns, and an initial list of rows.
     * @param name The name of the table. Cannot be null or empty.
     * @param columns The list of {@link Column} objects. Cannot be null.
     * @param rows The list of {@link Row} objects. Cannot be null.
     * @throws IllegalArgumentException If name, columns, or rows list is null/empty where not allowed.
     */
    public Table(String name, List<Column> columns, List<Row> rows) {
        this(name, columns);
        if (rows == null)
            throw new IllegalArgumentException("ERROR: Row list cannot be null.");
        this.rows = new ArrayList<>(rows);
    }

    /**
     * Gets the name of the table.
     * @return The table name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the table.
     * @param name The new name for the table. Cannot be null or empty.
     * @throws IllegalArgumentException If the provided name is null or empty.
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("ERROR: Table name cannot be null or empty.");
        this.name = name;
    }

    /**
     * Gets a copy of the list of columns in this table.
     * @return A new list containing the {@link Column} objects. Modifying this list will not affect the table.
     */
    public List<Column> getColumns() {
        return new ArrayList<>(columns);
    }

    /**
     * Gets a copy of the list of rows in this table.
     * @return A new list containing the {@link Row} objects. Modifying this list will not affect the table.
     */
    public List<Row> getRows() {
        return new ArrayList<>(rows);
    }

    /**
     * Retrieves a specific row from the table by its index.
     * @param index The zero-based index of the row.
     * @return The {@link Row} object at the specified index.
     * @throws IndexOutOfBoundsException If the index is out of range.
     */
    public Row getRow(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < rows.size()) {
            return rows.get(index);
        }
        throw new IndexOutOfBoundsException("ERROR: Row index out of bounds: " + index + " for table '" + this.name + "'. Table has " + rows.size() + " rows.");
    }

    /**
     * Replaces all rows in the table with a new list of rows.
     * @param newRows The new list of {@link Row} objects. Cannot be null.
     * @throws IllegalArgumentException If newRows is null.
     */
    public void setRows(List<Row> newRows) {
        if (newRows == null)
            throw new IllegalArgumentException("ERROR: New row list cannot be null.");
        this.rows = new ArrayList<>(newRows);
    }

    /**
     * Retrieves a specific column definition from the table by its index.
     * @param index The zero-based index of the column.
     * @return The {@link Column} object at the specified index.
     * @throws DatabaseOperationException If the index is out of range for the columns.
     */
    public Column getColumn(int index) throws DatabaseOperationException {
        if (index >= 0 && index < columns.size()) {
            return columns.get(index);
        }
        throw new DatabaseOperationException("ERROR: Column index " + index + " is out of bounds for table '" + name + "'. Table has " + columns.size() + " columns.");
    }

    /**
     * Finds the index of a column by its name (case-insensitive).
     * @param columnName The name of the column to find.
     * @return The zero-based index of the column.
     * @throws DatabaseOperationException If the column with the given name is not found.
     */
    public int getColumnIndex(String columnName) throws DatabaseOperationException {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new DatabaseOperationException("ERROR: Column '" + columnName + "' not found in table '" + name + "'.");
    }

    /**
     * Adds a new row to the table.
     * The number of values in the row must match the number of columns in the table.
     * @param row The {@link Row} object to add.
     * @throws DatabaseOperationException If the row's size does not match the table's column count.
     */
    public void addRow(Row row) throws DatabaseOperationException {
        // Ensure row structure matches table structure.
        if (row.size() != this.columns.size()) {
            throw new DatabaseOperationException("ERROR: Cannot add row, size (" + row.size()
                    + ") does not match table column count (" + this.columns.size() + ") in table '" + this.name + "'.");
        }
        this.rows.add(row);
    }

    /**
     * Adds a new column to the table definition.
     * All existing rows will be extended with a null value for this new column.
     * @param column The {@link Column} object to add.
     * @throws DatabaseOperationException If a column with the same name already exists (case-insensitive).
     */
    public void addColumn(Column column) throws DatabaseOperationException {
        // Check for duplicate column names and update existing rows.
        for (Column existingCol : this.columns) {
            if (existingCol.getName().equalsIgnoreCase(column.getName())) {
                throw new DatabaseOperationException("ERROR: Column '" + column.getName() + "' already exists in table '" + name + "'.");
            }
        }
        this.columns.add(column);
        for (Row row : this.rows) {
            row.addValue(null);
        }
    }

    /**
     * Removes a specific row from the table.
     * @param row The {@link Row} object to remove.
     * @return True if the row was found and removed, false otherwise.
     */
    public boolean removeRow(Row row) {
        return this.rows.remove(row);
    }
}