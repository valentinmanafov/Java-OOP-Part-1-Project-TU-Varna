import java.io.*;
import java.util.*;

public class FileHandler {

    public static void writeTableToFile(Table table, String filename) {
        String outputFilename = filename.toLowerCase().endsWith(".txt") ? filename : filename + ".txt";
        List<Column> columns = table.getColumns();
        List<Row> rows = table.getRows();

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilename))) {
            if (columns.isEmpty()) {
                writer.println("(Empty Table)");
                System.out.println("Warning: Exported empty table '" + table.getName() + "'.");
                return;
            }

            Map<Integer, Integer> columnWidths = new HashMap<>();
            int basicPadding = 15;

            for (int i = 0; i < columns.size(); i++) {
                int maxWidth = columns.get(i).getName().length();
                for (Row row : rows) {
                    if (i < row.size()) {
                        try {
                            maxWidth = Math.max(maxWidth, formatValueAsString(row.getValue(i)).length());
                        } catch (IndexOutOfBoundsException e) {
                        }
                    }
                }
                columnWidths.put(i, Math.max(maxWidth, basicPadding));
            }


            for (int i = 0; i < columns.size(); i++) {
                writer.print(padRight(columns.get(i).getName(), columnWidths.get(i)));
                if (i < columns.size() - 1) writer.print(" | ");
            }
            writer.println();

            for (int i = 0; i < columns.size(); i++) {
                writer.print(repeatChar('-', columnWidths.get(i)));
                if (i < columns.size() - 1) writer.print("-+-");
            }
            writer.println();

            if (rows.isEmpty()) {
                writer.println("(Table has no rows)");
            } else {
                for (Row row : rows) {
                    for (int j = 0; j < columns.size(); j++) {
                        String valStr = "";
                        if (j < row.size()) {
                            try {
                                valStr = formatValueAsString(row.getValue(j));
                            } catch (IndexOutOfBoundsException e) {
                                valStr = "[Data Error]";
                            }
                        } else {
                            valStr = "[Missing Col]";
                            System.out.println("Warning: Row found with fewer columns than definition during export.");
                        }
                        writer.print(padRight(valStr, columnWidths.get(j)));
                        if (j < columns.size() - 1) writer.print(" | ");
                    }
                    writer.println();
                }
            }
            System.out.println("Table '" + table.getName() + "' exported successfully to '" + outputFilename + "'.");

        } catch (IOException e) {
            System.out.println("Error writing file '" + outputFilename + "': " + e.getMessage());
        }
    }

    static String formatValueAsString(Object value) {
        if (value == null) return "NULL";
        return value.toString();
    }

    static String padRight(String s, int n) {
        String str = (s == null) ? "" : s;
        if (str.length() > n) {
            return str.substring(0, n);
        }
        if (str.length() == n) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < n) {
            sb.append(" ");
        }
        return sb.toString();
    }

    static String repeatChar(char c, int n) {
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