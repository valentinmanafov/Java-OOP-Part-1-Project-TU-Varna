package project;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single row of data in a table.
 * A row consists of a list of object values, corresponding to the columns of the table.
 */
public class Row {
    private List<Object> values;

    /**
     * Constructs a new Row with a given list of values.
     * The provided list of values is copied to ensure immutability of the input list.
     * @param values The list of objects representing the values in the row.
     */
    public Row(List<Object> values) {

        this.values = new ArrayList<>(values);
    }

    /**
     * Gets a copy of the list of values in this row.
     * @return A new list containing the values of the row. Modifying this list will not affect the row itself.
     */
    public List<Object> getValues() {

        return new ArrayList<>(values);
    }

    /**
     * Gets the value at a specific index (column) in the row.
     * @param index The zero-based index of the value to retrieve.
     * @return The object value at the specified index.
     */
    public Object getValue(int index) throws IndexOutOfBoundsException {
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }

        throw new IndexOutOfBoundsException("ERROR: Row index out of bounds: " + index + ". Row size is " + values.size() + ".");
    }

    /**
     * Sets the value at a specific index (column) in the row.
     * @param index The zero-based index of the value to set.
     * @param value The new value to set at the specified index.
     */
    public void setValue(int index, Object value) throws IndexOutOfBoundsException {
        if (index >= 0 && index < values.size()) {
            values.set(index, value);
        } else {
            // Throw exception if index is invalid.
            throw new IndexOutOfBoundsException("ERROR: Row index out of bounds: " + index + ". Row size is " + values.size() + ".");
        }
    }

    /**
     * Gets the number of values (columns) in this row.
     * @return The size of the row.
     */
    public int size() {
        return values.size();
    }

    /**
     * Adds a value to the end of the row.
     * This method is protected and intended for use by the {@link Table} class when adding a new column
     * to existing rows.
     * @param value The value to add.
     */
    protected void addValue(Object value) {
        values.add(value);
    }

    /**
     * Returns a string representation of the row.
     * Values are comma-separated, and null values are represented as "NULL".
     * @return A string representation of the row's data.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            sb.append(value == null ? "NULL" : value.toString());
            if (i < values.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}