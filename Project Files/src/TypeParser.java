public class TypeParser {

    public static Object parse(String value, DataType targetType) throws DatabaseOperationException {
        if (value == null || value.equalsIgnoreCase("NULL")) {
            return null;
        }
        try {
            switch (targetType) {
                case INTEGER:
                    String intValue = value;
                    if (value.startsWith("+")) {
                        intValue = value.substring(1);
                    }
                    return Integer.parseInt(intValue);
                case DOUBLE:
                    String doubleValue = value;
                    if (value.startsWith("+")) {
                        doubleValue = value.substring(1);
                    }
                    return Double.parseDouble(doubleValue);
                case STRING:
                    if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                        return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
                    }
                    return value;
                default:
                    throw new DatabaseOperationException("Internal Error: Unsupported data type: " + targetType);
            }
        } catch (NumberFormatException e) {
            throw new DatabaseOperationException("Cannot parse '" + value + "' as " + targetType, e);
        }
    }

    public static boolean valueMatchesType(Object v, DataType t) {
        return false;
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
            return tableValue.equals(parsedSearchValue);
        } catch (DatabaseOperationException e) {
            return false;
        }
    }
}