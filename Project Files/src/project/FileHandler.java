package project;

import java.io.*;
import java.util.*;


public class FileHandler {

    private static final String COLUMN_DATA_DELIMITER_REGEX = "\\s*\\|\\s*";
    private static final String COLUMN_DATA_DELIMITER_WRITE = " | ";
    private static final String HEADER_SEPARATOR_DELIMITER_WRITE = "-+-";

    public static void writeTableToFile(Table table, String filename) throws DatabaseOperationException {
        String outputFilename = filename.toLowerCase().endsWith(".txt") ? filename : filename + ".txt";
        List<Column> columns = table.getColumns();
        List<Row> rows = table.getRows();

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilename))) {
            writer.println("Table: " + table.getName());

            if (columns.isEmpty()) {
                writer.println("| (Table has no columns) |");
                System.out.println("Warning: Exported table '" + table.getName() + "' with no columns.");
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
                    headerLine.append(COLUMN_DATA_DELIMITER_WRITE);
                    separatorLine.append(HEADER_SEPARATOR_DELIMITER_WRITE);
                }
            }
            headerLine.append(" |");
            separatorLine.append(" |");
            writer.println(headerLine.toString());
            writer.println(separatorLine.toString());

            if (rows.isEmpty()) {
                StringBuilder emptyRowLine = new StringBuilder("| (Table has no rows)");
                int currentLength = emptyRowLine.length();
                int targetLength = separatorLine.length() -1;
                while(emptyRowLine.length() < targetLength ) emptyRowLine.append(" ");
                emptyRowLine.append("|");
                writer.println(emptyRowLine.toString());
            } else {
                for (Row row : rows) {
                    writer.print("|");
                    for (int j = 0; j < columns.size(); j++) {
                        String valStr = "[NoData]";
                        if (j < row.size()) {
                            try {
                                valStr = formatValueAsString(row.getValue(j));
                            } catch (IndexOutOfBoundsException e) {
                                valStr = "[DataErr]";
                            }
                        }
                        writer.print(padRight(valStr, columnWidths.get(j)));
                        if (j < columns.size() - 1) {
                            writer.print(COLUMN_DATA_DELIMITER_WRITE);
                        }
                    }
                    writer.println(" | ");
                }
            }
            System.out.println("Table '" + table.getName() + "' exported successfully to '" + outputFilename + "'.");
        } catch (IOException e) {
            throw new DatabaseOperationException("Error writing file '" + outputFilename + "': " + e.getMessage(), e);
        }
    }

    public static Table readTableFromFile(String filename) throws DatabaseOperationException {
        String importedTableName = null;
        List<Column> importedColumns = new ArrayList<>();
        List<Row> importedRows = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            line = reader.readLine(); lineNumber++;
            if (line == null || !line.startsWith("Table: ")) throw new DatabaseOperationException("File format error: Missing 'Table: <name>' on Line 1.");
            importedTableName = line.substring("Table: ".length()).trim();
            if (importedTableName.isEmpty()) throw new DatabaseOperationException("Table name cannot be empty (Line 1).");

            line = reader.readLine(); lineNumber++;
            if (line == null || !line.startsWith("|") || !line.endsWith("|")) throw new DatabaseOperationException("File format error: Invalid column header format (Line 2).");
            String[] colDefsArray = line.substring(1, line.length() - 1).split(COLUMN_DATA_DELIMITER_REGEX);

            if (colDefsArray.length == 1 && colDefsArray[0].trim().equalsIgnoreCase("(Table has no columns)")) {
            } else {
                for (String colDef : colDefsArray) {
                    String[] parts = colDef.split("\\s*-\\s*");
                    if (parts.length != 2) throw new DatabaseOperationException("File format error: Invalid column definition '" + colDef + "' (Line 2). Expected 'Name - TYPE'.");
                    String colName = parts[0].trim();
                    String typeName = parts[1].trim();
                    if (colName.isEmpty()) throw new DatabaseOperationException("File format error: Column name cannot be empty (Line 2).");
                    try {
                        importedColumns.add(new Column(colName, DataType.valueOf(typeName.toUpperCase())));
                    } catch (IllegalArgumentException e) {
                        throw new DatabaseOperationException("File format error: Unknown data type '" + typeName + "' for column '" + colName + "' (Line 2).");
                    }
                }
            }

            line = reader.readLine(); lineNumber++;
            if (line == null || !line.startsWith("|") || !line.endsWith("|") || !line.contains("-")) throw new DatabaseOperationException("File format error: Missing or invalid header separator line (Line 3).");

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().startsWith("| (Table has no rows)")) continue;
                if (importedColumns.isEmpty()) {
                    if (!line.trim().equals("|") && !line.trim().equals("| |")) System.out.println("Warning: Line " + lineNumber + " has data but no columns defined. Skipping: " + line);
                    continue;
                }

                if (!line.startsWith("|") || !line.endsWith("|")) {
                    System.out.println("Warning: Line " + lineNumber + " has invalid row format. Skipping: " + line);
                    continue;
                }
                String[] valuesStr = line.substring(1, line.length() - 1).split(COLUMN_DATA_DELIMITER_REGEX, -1);

                if (valuesStr.length != importedColumns.size()) {
                    System.out.println("Warning: Line " + lineNumber + " value count mismatch. Expected " + importedColumns.size() + ", got " + valuesStr.length + ". Skipping: " + line);
                    continue;
                }

                List<Object> parsedValues = new ArrayList<>();
                for (int i = 0; i < importedColumns.size(); i++) {
                    String valToParse = valuesStr[i].trim();
                    if (valToParse.equals("[NoData]") || valToParse.equals("[DataErr]")) {
                        parsedValues.add(null);
                        continue;
                    }
                    try {
                        parsedValues.add(TypeParser.parse(valToParse, importedColumns.get(i).getType()));
                    } catch (DatabaseOperationException e) {
                        System.out.println("Warning: Line " + lineNumber + ", Col " + (i+1) + " ('" + importedColumns.get(i).getName() + "'): Parse error for '" + valToParse + "'. Using NULL.");
                        parsedValues.add(null);
                    }
                }
                importedRows.add(new Row(parsedValues));
            }
            return new Table(importedTableName, importedColumns, importedRows);

        } catch (FileNotFoundException e) {
            throw new DatabaseOperationException("Import failed: File not found '" + filename + "'", e);
        } catch (IOException e) {
            throw new DatabaseOperationException("Error reading file '" + filename + "': " + e.getMessage(), e);
        }
    }

    public static String formatValueAsString(Object value) { // Made public
        if (value == null) return "NULL";
        return value.toString();
    }

    public static String padRight(String s, int n) { // Made public
        String str = (s == null) ? "" : s;
        if (str.length() > n) return str.substring(0, n);
        if (str.length() == n) return str;
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < n) sb.append(" ");
        return sb.toString();
    }

    public static String repeatChar(char c, int n) { // Made public
        if (n <= 0) return "";
        char[] chars = new char[n];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    private static String extractTableNameFromFilename(String filename) {
        if (filename == null) return "unknown_table_name";
        File f = new File(filename);
        String n = f.getName();
        int d = n.lastIndexOf('.');
        return (d > 0) ? n.substring(0, d) : n;
    }
}