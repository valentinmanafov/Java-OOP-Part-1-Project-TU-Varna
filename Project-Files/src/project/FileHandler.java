package project;

import java.io.*;
import java.util.*;

public class FileHandler {

    private static final String TABLE_NAME_PREFIX = "Table: ";
    private static final String DELIMITER_WRITE = " | ";
    private static final String HEADER_SEPARATOR_DELIMITER_WRITE = "-+-";
    private static final String DELIMITER_PATTERN_READ = "\\s*\\|\\s*";

    public static Map<String, String> readCatalog(String catalogFilePath) throws DatabaseOperationException {
        Map<String, String> registry = new LinkedHashMap<>();
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
                if (line.isEmpty() || line.startsWith("#")) continue;
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
                    continue;
                }
                registry.put(tableName, filePath);
            }
        } catch (IOException e) {
            throw new DatabaseOperationException("ERROR: Reading database file '" + catalogFilePath + "': " + e.getMessage(), e);
        }
        return registry;
    }

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

    public static void writeTableToFile(Table table, String filename) throws DatabaseOperationException {
        List<Column> columns = table.getColumns();
        List<Row> rows = table.getRows();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(TABLE_NAME_PREFIX + table.getName());
            if (columns.isEmpty()) {
                writer.println("| (Table has no columns) |");
                return;
            }
            List<String> combinedColumnHeaders = new ArrayList<>();
            Map<Integer, Integer> columnWidths = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                Column col = columns.get(i);
                String headerText = col.getName() + " - " + col.getType().name();
                combinedColumnHeaders.add(headerText);
                int maxWidth = headerText.length();
                for (Row row : rows) {
                    if (i < row.size()) {
                        try {
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
                StringBuilder emptyRowLine = new StringBuilder("| (Table has no rows)");
                int targetLength = separatorLine.length() - 2;
                while (emptyRowLine.length() < targetLength) {
                    emptyRowLine.append(" ");
                }
                if (emptyRowLine.length() > targetLength) {
                    emptyRowLine.setLength(targetLength);
                }
                writer.println("|" + emptyRowLine.toString() + "|");
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

            String[] colDefsArray = trimmedHeaderLine.substring(1, trimmedHeaderLine.length() - 1).split(DELIMITER_PATTERN_READ);
            if (colDefsArray.length == 1 && colDefsArray[0].trim().equalsIgnoreCase("(Table has no columns)")) {
            } else {
                for (String colDef : colDefsArray) {
                    String[] parts = colDef.split("\\s*-\\s*");
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
            if (trimmedSeparatorLine == null || !trimmedSeparatorLine.startsWith("|") || !trimmedSeparatorLine.endsWith("|") || !trimmedSeparatorLine.contains("-"))
                throw new DatabaseOperationException("ERROR: Missing or invalid header separator line (Line 3) in " + filename);

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedDataLine = line.trim();
                if (trimmedDataLine.startsWith("| (Table has no rows)")) continue;

                if (importedColumns.isEmpty()) {
                    if (!trimmedDataLine.equals("|") && !trimmedDataLine.equals("||") && !trimmedDataLine.equals("| |")) {
                        System.out.println("WARNING: Line " + lineNumber + " has data but no columns defined. Skipping: " + line);
                    }
                    continue;
                }

                if (!trimmedDataLine.startsWith("|") || !trimmedDataLine.endsWith("|")) {
                    System.out.println("WARNING: Line " + lineNumber + " has invalid row format (must start and end with '|'). Skipping: " + line);
                    continue;
                }

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

    public static void saveCatalogAndTables(Database db, String catalogFilePath) throws DatabaseOperationException {
        if (!db.isCatalogOpen()) throw new DatabaseOperationException("ERROR: No database is open to save.");
        Set<String> modifiedTables = db.getModifiedLoadedTableNames();
        Map<String, String> registry = db.getTableRegistry();

        System.out.println("Saving modified tables...");
        for (String tableName : modifiedTables) {
            Table tableToSave = db.getLoadedTable(tableName);
            String tableFilePath = registry.get(tableName);
            if (tableToSave != null && tableFilePath != null) {
                try {
                    System.out.println("Saving " + tableName + " to " + tableFilePath + "...");
                    writeTableToFile(tableToSave, tableFilePath);
                } catch (DatabaseOperationException e) {
                    throw new DatabaseOperationException("ERROR: Failed to save table '" + tableName + "': " + e.getMessage(), e);
                }
            } else {
                System.out.println("WARNING: Could not save table '" + tableName + "' - missing data or path.");
            }
        }
        System.out.println("Saving database file to " + catalogFilePath + "...");
        writeCatalog(registry, catalogFilePath);
        System.out.println("Database and modified tables saved successfully.");
    }

    private static String formatValueForSave(Object value) {
        if (value == null) return "NULL";
        if (value instanceof String) {
            String s = (String) value;
            String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
            return "\"" + escaped + "\"";
        }
        return value.toString();
    }

    public static String formatValueAsString(Object value) {
        if (value == null) return "NULL";
        return value.toString();
    }

    public static String padRight(String s, int n) {
        String str = (s == null) ? "" : s;
        if (str.length() >= n) return str;
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < n) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String repeatChar(char c, int n) {
        if (n <= 0) return "";
        char[] chars = new char[n];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    private static String extractTableNameFromFilename(String filename) {
        if (filename == null) return "unknown";
        File f = new File(filename);
        String n = f.getName();
        int d = n.lastIndexOf('.');
        return (d > 0) ? n.substring(0, d) : n;
    }
}