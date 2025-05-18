package project;

/**
 * Represents a column in a database table, including its name and data type.
 */
public class Column {
    private String name;
    private DataType type;

    /**
     * Constructs a new Column with a specified name and data type.
     * @param name The name of the column.
     * @param type The data type of the column.
     */
    public Column(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Gets the name of the column.
     * @return The column name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the data type of the column.
     * @return The column data type.
     */
    public DataType getType() {
        return type;
    }

    /**
     * Returns a string representation of the column, including its name and type.
     * @return A string in the format "name (type)".
     */
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}