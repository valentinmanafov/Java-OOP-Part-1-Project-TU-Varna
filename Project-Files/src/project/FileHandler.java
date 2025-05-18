package project;

import java.io.*;
import java.util.*;

/**
 * Handles reading and writing database catalog files and table data files.
 * This class provides static methods for file operations.
 */
public class FileHandler {

    private static final String TABLE_NAME_PREFIX = "Table: ";
    private static final String DELIMITER_WRITE = " | ";
    private static final String HEADER_SEPARATOR_DELIMITER_WRITE = "-+-";
    // Regex for splitting, allowing for surrounding whitespace.
    private static final String DELIMITER_PATTERN_READ = "\\s*\\|\\s*";


    /**
     * Reads a database catalog file and returns a map of table names to their file paths.
     * Catalog file format: TableName,FilePath (one per line, '#' for comments).
     * @param catalogFilePath The path to the catalog file.
     * @return A {@link LinkedHashMap} preserving the order of entries, mapping table names to file paths.
     * @throws DatabaseOperationException If an I/O error occurs or the file format is invalid.
     */
    public static Map<String, String> readCatalog(String catalogFilePath) throws DatabaseOperationException {
        Map<String, String> registry = new LinkedHashMap<>(); // Preserves insertion order
        File catalogFile = new File(catalogFilePath);
        int lineNumber = 0;
        if (!catalogFile.exists()) {
            System.out.println("WARNING: Database file not found: " + catalogFilePath + ". Starting empty.");
            return registry;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(catalogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(",", 2);
                if (parts.length != 2) {
                    System.out.println("WARNING: Skipping invalid line " + lineNumber + " in database: " + line);
                    continue;
                }
                String tableName = parts[0].trim();
                String filePath = parts[1].trim();
                if (tableName.isEmpty() || filePath.isEmpty()) {
                    System.out.println("WARNING: Skipping invalid line " + lineNumber + " in database: Name or path empty.");
                    continue;
                }
                if (registry.containsKey(tableName)) {
                    System.out.println("WARNING: Duplicate table name '" + tableName + "' in database (line " + lineNumber + ").");
                    continue; // Skip duplicates, keeping the first encountered
                }
                registry.put(tableName, filePath);
            }
        } catch (IOException e) {
            throw new DatabaseOperationException("ERROR: Reading database file '" + catalogFilePath + "': " + e.getMessage(), e);
        }
        return registry;
    }

    /**
     * Writes the table registry (map of table names to file paths) to a catalog file.
     * @param registry The map of table names to file paths.
     * @param catalogFilePath The path to the catalog file to be written.
     * @throws DatabaseOperationException If an I/O error occurs.
     */
    public static void writeCatalog(Map<String, String> registry, String catalogFilePath) throws DatabaseOperationException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(catalogFilePath))) {
            writer.println("# Database File");
            writer.println("# Format: TableName,FilePath");
            for (Map.Entry<String, String> entry : registry.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            throw new DatabaseOperationException("ERROR: Writing database file '" + catalogFilePath + "': " + e.getMessage(), e);
        }
    }

    /**
     * Reads a table from its data file.
     * The file format includes the table name, column definitions, and row data,
     * using a pipe-delimited format.
     * Handles parsing of column types and row values.
     * @param filename The path to the table data file.
     * @return A {@link Table} object populated with data from the file.
     * @throws DatabaseOperationException If the file is not found, an I/O error occurs, or the file format is invalid.
     */
    public static Table readTableFromFile(String filename) throws DatabaseOperationException {
        String actualTableName = null;
        List<Column> importedColumns = new ArrayList<>();
        List<Row> importedRows = new ArrayList<>();
        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            line = reader.readLine();
            lineNumber++;
            if (line == null || !line.startsWith(TABLE_NAME_PREFIX))
                throw new DatabaseOperationException("ERROR: Missing '" + TABLE_NAME_PREFIX + "<name>' on Line 1 in " + filename);
            actualTableName = line.substring(TABLE_NAME_PREFIX.length()).trim();
            if (actualTableName.isEmpty())
                throw new DatabaseOperationException("ERROR: Table name empty (Line 1) in " + filename);

            line = reader.readLine();
            lineNumber++;
            String trimmedHeaderLine = (line != null) ? line.trim() : null;
            if (trimmedHeaderLine == null || !trimmedHeaderLine.startsWith("|") || !trimmedHeaderLine.endsWith("|"))
                throw new DatabaseOperationException("ERROR: Invalid column header format (Line 2) in " + filename + ". Expected format like '|Col1 - TYPE|Col2 - TYPE|'");

            //Parsing column definitions from header line.
            String[] colDefsArray = trimmedHeaderLine.substring(1, trimmedHeaderLine.length() - 1).split(DELIMITER_PATTERN_READ);
            if (colDefsArray.length == 1 && colDefsArray[0].trim().equalsIgnoreCase("(Table has no columns)")) {
            } else {
                for (String colDef : colDefsArray) {
                    if (colDef.trim().isEmpty() && colDefsArray.length == 1) { // Handles case like "| |" for empty header
                        break;
                    }
                    String[] parts = colDef.split("\\s*-\\s*", 2); // Split "Name - TYPE"
                    if (parts.length != 2)
                        throw new DatabaseOperationException("ERROR: Invalid column definition '" + colDef + "' (Line 2) in " + filename + ". Expected 'Name - TYPE'.");
                    String colName = parts[0].trim();
                    String typeName = parts[1].trim();
                    if (colName.isEmpty())
                        throw new DatabaseOperationException("ERROR: Column name empty (Line 2) in " + filename);
                    try {
                        importedColumns.add(new Column(colName, DataType.valueOf(typeName.toUpperCase())));
                    } catch (IllegalArgumentException e) {
                        throw new DatabaseOperationException("ERROR: Unknown data type '" + typeName + "' for column '" + colName + "' (Line 2) in " + filename);
                    }
                }
            }
            line = reader.readLine();
            lineNumber++;
            String trimmedSeparatorLine = (line != null) ? line.trim() : null;
            if (trimmedSeparatorLine == null || !trimmedSeparatorLine.startsWith("|") || !trimmedSeparatorLine.endsWith("|") || !trimmedSeparatorLine.contains("-")){
                if (!importedColumns.isEmpty() || (colDefsArray.length > 0 && !colDefsArray[0].trim().equalsIgnoreCase("(Table has no columns)"))) {
                    throw new DatabaseOperationException("ERROR: Missing or invalid header separator line (Line 3) in " + filename);
                }
            }

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedDataLine = line.trim();
                if (trimmedDataLine.startsWith("| (Table has no rows)")) continue; // Skip no rows placeholder message

                if (importedColumns.isEmpty()) {
                    if (!trimmedDataLine.equals("|") && !trimmedDataLine.equals("||") && !trimmedDataLine.equals("| |") && !trimmedDataLine.isEmpty()) {
                        System.out.println("WARNING: Line " + lineNumber + " has data but no columns defined. Skipping: " + line);
                    }
                    continue;
                }

                if (!trimmedDataLine.startsWith("|") || !trimmedDataLine.endsWith("|")) {
                    System.out.println("WARNING: Line " + lineNumber + " has invalid row format (must start and end with '|'). Skipping: " + line);
                    continue;
                }

                // Use -1 limit to include trailing empty strings if a column at the end is NULL or empty string
                String[] valuesStr = trimmedDataLine.substring(1, trimmedDataLine.length() - 1).split(DELIMITER_PATTERN_READ, -1);

                if (valuesStr.length != importedColumns.size()) {
                    System.out.println("WARNING: Line " + lineNumber + " value count (" + valuesStr.length + ") mismatch with column count (" + importedColumns.size() + "). Skipping: " + line);
                    continue;
                }

                List<Object> parsedValues = new ArrayList<>();
                for (int i = 0; i < importedColumns.size(); i++) {
                    String valToParse = valuesStr[i].trim();
                    if (valToParse.equals("[NoData]") || valToParse.equals("[DataErr]") || valToParse.equalsIgnoreCase("NULL")) {
                        parsedValues.add(null);
                        continue;
                    }
                    try {
                        parsedValues.add(TypeParser.parse(valToParse, importedColumns.get(i).getType()));
                    } catch (DatabaseOperationException e) {
                        System.out.println("WARNING: Line " + lineNumber + ", Col " + (i + 1) + " ('" + importedColumns.get(i).getName() + "'): Parse error for value '" + valToParse + "' as " + importedColumns.get(i).getType() + ". Using NULL. Error: " + e.getMessage());
                        parsedValues.add(null);
                    }
                }
                importedRows.add(new Row(parsedValues));
            }
            return new Table(actualTableName, importedColumns, importedRows);
        } catch (FileNotFoundException e) {
            throw new DatabaseOperationException("ERROR: Table file not found '" + filename + "'", e);
        } catch (IOException e) {
            throw new DatabaseOperationException("ERROR: reading table file '" + filename + "': " + e.getMessage(), e);
        }
    }


    /**
     * Writes a table's data to a specified file.
     * Formats the output with table name, column headers (name and type),
     * a separator line, and then pipe-delimited row data.
     * Calculates column widths for pretty printing.
     * @param table The {@link Table} object to write.
     * @param filename The path to the file where the table data will be written.
     * @throws DatabaseOperationException If an I/O error occurs.
     */
    public static void writeTableToFile(Table table, String filename) throws DatabaseOperationException {
        List<Column> columns = table.getColumns();
        List<Row> rows = table.getRows();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(TABLE_NAME_PREFIX + table.getName());

            if (columns.isEmpty()) {
                writer.println("| (Table has no columns) |"); // Special placeholder if no columns
                return;
            }

            List<String> combinedColumnHeaders = new ArrayList<>();
            Map<Integer, Integer> columnWidths = new HashMap<>();

            // Calculate optimal column widths for formatting.
            // Width is based on header text length and the length of the longest data value in that column.
            for (int i = 0; i < columns.size(); i++) {
                Column col = columns.get(i);
                String headerText = col.getName() + " - " + col.getType().name();
                combinedColumnHeaders.add(headerText);
                int maxWidth = headerText.length();
                for (Row row : rows) {
                    if (i < row.size()) { // Check if row has this column
                        try {
                            // Use formatValueAsString to get the string representation for width calculation
                            maxWidth = Math.max(maxWidth, formatValueAsString(row.getValue(i)).length());
                        } catch (IndexOutOfBoundsException e) {
                        }
                    }
                }
                columnWidths.put(i, Math.max(maxWidth, 5));
            }

            StringBuilder headerLine = new StringBuilder("|");
            StringBuilder separatorLine = new StringBuilder("|");
            for (int i = 0; i < columns.size(); i++) {
                String paddedHeader = padRight(combinedColumnHeaders.get(i), columnWidths.get(i));
                headerLine.append(paddedHeader);
                separatorLine.append(repeatChar('-', columnWidths.get(i)));
                if (i < columns.size() - 1) {
                    headerLine.append(DELIMITER_WRITE);
                    separatorLine.append(HEADER_SEPARATOR_DELIMITER_WRITE);
                }
            }
            headerLine.append("|");
            separatorLine.append("|");
            writer.println(headerLine.toString());
            writer.println(separatorLine.toString());

            if (rows.isEmpty()) {
                StringBuilder emptyRowLine = new StringBuilder("(Table has no rows)");
                int targetLength = separatorLine.length() - 3;
                while(emptyRowLine.length() < targetLength -1){
                    emptyRowLine.insert(0," ");
                    if(emptyRowLine.length() < targetLength -1) emptyRowLine.append(" ");
                }
                writer.println("| " + padRight(emptyRowLine.toString(), targetLength-1)+ "|");
            } else {
                for (Row row : rows) {
                    writer.print("|");
                    List<Object> values = row.getValues();
                    for (int j = 0; j < columns.size(); j++) {
                        String valStr = "[NoData]";
                        if (j < values.size()) {
                            try {
                                valStr = formatValueForSave(values.get(j));
                            } catch (IndexOutOfBoundsException e) {
                                valStr = "[DataErr]";
                            }
                        }
                        writer.print(padRight(valStr, columnWidths.get(j)));
                        if (j < columns.size() - 1) {
                            writer.print(DELIMITER_WRITE);
                        }
                    }
                    writer.println(" |");
                }
            }
        } catch (IOException e) {
            throw new DatabaseOperationException("ERROR: Writing table file '" + filename + "': " + e.getMessage(), e);
        }
    }

    /**
     * Saves the entire database, including its catalog and all modified tables.
     * It iterates through tables marked as modified (or all loaded tables if global changes exist)
     * and writes them to their respective files. Then, it writes the catalog file.
     * @param db The {@link Database} instance to save.
     * @param catalogFilePath The path where the main catalog file should be saved.
     * @throws DatabaseOperationException If the database is not open or any I/O error occurs during saving.
     */
    public static void saveCatalogAndTables(Database db, String catalogFilePath) throws DatabaseOperationException {
        if (!db.isCatalogOpen()) {
            throw new DatabaseOperationException("ERROR: No database is open to save.");
        }

        // Get the names of tables that need saving.
        Set<String> tablesToSaveNames = db.getModifiedLoadedTableNames();
        Map<String, String> registry = db.getTableRegistry(); // Get the current table name to file path mappings.

        System.out.println("Saving tables...");
        for (String tableName : tablesToSaveNames) {
            Table tableToSave = db.getLoadedTable(tableName); // Get the Table object from memory.
            String tableFilePath = registry.get(tableName);   // Get its designated file path.
            if (tableToSave != null && tableFilePath != null) {
                try {
                    System.out.println("Saving table '" + tableName + "' to " + tableFilePath + "...");
                    writeTableToFile(tableToSave, tableFilePath);
                } catch (DatabaseOperationException e) {
                    throw new DatabaseOperationException("ERROR: Failed to save table '" + tableName + "': " + e.getMessage(), e);
                }
            } else {
                System.out.println("WARNING: Could not save table '" + tableName + "' - missing data or path information.");
            }
        }

        if (tablesToSaveNames.isEmpty() && db.hasUnsavedChanges()) {
            System.out.println("No specific tables marked for save, but catalog might have changed or other general changes occurred.");
        }

        // Always save the database file itself, as it might have changed.
        System.out.println("Saving database to " + catalogFilePath + "...");
        writeCatalog(registry, catalogFilePath);

        System.out.println("Database and all relevant tables saved successfully.");
    }

    /**
     * Formats a value for saving to a file.
     * NULL values are represented as "NULL". Strings are enclosed in double quotes,
     * with internal backslashes and double quotes escaped.
     * Other types are converted using their toString() method.
     * @param value The object value to format.
     * @return The string representation of the value for file storage.
     */
    private static String formatValueForSave(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String) {
            String s = (String) value;
            // Escape backslashes and double quotes within the string.
            String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
            return "\"" + escaped + "\"";
        }
        return value.toString();
    }

    /**
     * Formats a value as a string for display purposes (e.g., printing to console).
     * NULL values are represented as "NULL". Other types are converted using their toString() method.
     * This version is simpler than {@link #formatValueForSave(Object)} as it doesn't need to handle string escaping for file storage.
     * @param value The object value to format.
     * @return The string representation of the value.
     */
    public static String formatValueAsString(Object value) {
        if (value == null) {
            return "NULL";
        }
        return value.toString();
    }

    /**
     * Pads a string with spaces on the right to reach a specified length.
     * If the string is null, it's treated as an empty string.
     * If the string is already longer than or equal to the target length, it's returned unchanged.
     * @param s The string to pad.
     * @param n The target length.
     * @return The padded string.
     */
    public static String padRight(String s, int n) {
        String str = (s == null) ? "" : s;
        if (str.length() >= n) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < n) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Repeats a character a specified number of times.
     * @param c The character to repeat.
     * @param n The number of times to repeat the character.
     * @return A string consisting of the character repeated n times, or an empty string if n is not positive.
     */
    public static String repeatChar(char c, int n) {
        if (n <= 0) {
            return "";
        }
        char[] chars = new char[n];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    /**
     * Extracts a potential table name from a filename by stripping the extension.
     * For example, "MyTable.txt" would become "MyTable".
     * This is a utility method and might not be robust for all filename conventions.
     * @param filename The filename.
     * @return The filename without the last extension, or the original filename if no dot is found, or "unknown" if filename is null.
     */
    @SuppressWarnings("unused")
    private static String extractTableNameFromFilename(String filename) {
        if (filename == null) return "unknown";
        File f = new File(filename);
        String n = f.getName();
        int d = n.lastIndexOf('.');
        return (d > 0) ? n.substring(0, d) : n;
    }
}