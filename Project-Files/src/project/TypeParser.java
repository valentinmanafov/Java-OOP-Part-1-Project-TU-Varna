package project;

import java.util.*;

/**
 * Utility class for parsing string values into specific data types
 * and for comparing values with a degree of type flexibility.
 */
public class TypeParser {

    /**
     * Parses a string value into an object of the specified target data type.
     * Handles "NULL" string literals, numeric parsing for INTEGER and DOUBLE,
     * and string parsing (expecting quotes and handling escapes).
     * @param value The string value to parse.
     * @param targetType The {@link DataType} to parse the value into.
     * @return The parsed object (e.g., Integer, Double, String), or null if the input value represents null.
     * @throws DatabaseOperationException If parsing fails due to incorrect format or unsupported type.
     */
    public static Object parse(String value, DataType targetType) throws DatabaseOperationException {
        if (value == null || value.equalsIgnoreCase("NULL")) {
            return null;
        }

        switch (targetType) {
            case INTEGER:
                try {
                    String intValue = value;
                    if (value.startsWith("+")) {
                        intValue = value.substring(1);
                    }
                    return Integer.parseInt(intValue);
                } catch (NumberFormatException e) {
                    throw new DatabaseOperationException("ERROR: Cannot parse '" + value + "' as INTEGER", e);
                }
            case DOUBLE:
                try {
                    String doubleValue = value;
                    if (value.startsWith("+")) {
                        doubleValue = value.substring(1);
                    }
                    return Double.parseDouble(doubleValue);
                } catch (NumberFormatException e) {
                    throw new DatabaseOperationException("ERROR: Cannot parse '" + value + "' as DOUBLE", e);
                }
            case STRING:
                // Strings are expected to be enclosed in double quotes.
                // Internal quotes or backslashes must be escaped with a backslash.
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    String content = value.substring(1, value.length() - 1); // Remove surrounding quotes
                    StringBuilder parsedString = new StringBuilder();
                    boolean escaped = false;
                    for (char c : content.toCharArray()) {
                        if (escaped) {
                            if (c == '\\' || c == '"') {
                                parsedString.append(c);
                            } else {
                                parsedString.append('\\');
                                parsedString.append(c);
                            }
                            escaped = false;
                        } else {
                            if (c == '\\') {
                                escaped = true;
                            } else {
                                parsedString.append(c);
                            }
                        }
                    }

                    if (escaped) {
                        parsedString.append('\\');
                    }
                    return parsedString.toString();
                } else {

                    throw new DatabaseOperationException("ERROR: Invalid STRING format: Value must be enclosed in double quotes (\"). Got: " + value);
                }
            default:
                throw new DatabaseOperationException("ERROR: Unsupported data type: " + targetType);
        }
    }

    /**
     * Performs a "loose" equality check between a value from a table (already typed)
     * and a string search value provided by the user.
     * The search value is first parsed into the column's data type before comparison.
     * Handles "NULL" string literals for search values.
     * @param tableValue The value from the table (e.g., Integer, Double, String, or null).
     * @param searchValue The string value to compare against, as input by the user.
     * @param columnType The {@link DataType} of the column from which tableValue originated.
     * @return True if the values are considered equal after parsing searchValue, false otherwise.
     */
    public static boolean looselyEquals(Object tableValue, String searchValue, DataType columnType) {
        if (tableValue == null) {
            // If the table cell is NULL, it matches only if the searchValue is "NULL" (case-insensitive).
            return searchValue != null && searchValue.equalsIgnoreCase("NULL");
        }
        if (searchValue != null && searchValue.equalsIgnoreCase("NULL")) {
            return false;
        }
        if (searchValue == null) {
            return false;
        }

        try {
            Object parsedSearchValue = parse(searchValue, columnType);

            if (tableValue instanceof String && !(searchValue.startsWith("\"") && searchValue.endsWith("\"")) && !searchValue.equalsIgnoreCase("NULL")) {

                return false;
            }

            return Objects.equals(tableValue, parsedSearchValue);

        } catch (DatabaseOperationException e) {

            return false;
        }
    }
}