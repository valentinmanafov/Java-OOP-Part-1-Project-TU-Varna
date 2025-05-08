package project;

import java.util.*;

public class TypeParser {

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
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    String content = value.substring(1, value.length() - 1);
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

    public static boolean looselyEquals(Object tableValue, String searchValue, DataType columnType) {
        if (tableValue == null) {
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