package project.commands;

import project.*;

import java.util.*;

public class PrintCommand implements CommandHandler {

    private final Database database;
    private final Scanner inputScanner;
    private static final int PAGE_SIZE = 10;

    public PrintCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: print <table>");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName);
            List<Column> columns = table.getColumns();
            List<Row> rows = table.getRows();

            if (columns.isEmpty()) {
                System.out.println("WARNING: Table '" + tableName + "' has no columns.");
                return;
            }
            if (rows.isEmpty()) {
                System.out.println("WARNING: Table '" + tableName + "' has no rows.");
                return;
            }
            displayRowsPaginated(tableName, columns, rows);
            System.out.println("Finished displaying table '" + tableName + "'.");
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: During print: " + e.getMessage());
        }
    }

    private void displayRowsPaginated(String title, List<Column> columns, List<Row> rowsToDisplay) {
        if (rowsToDisplay == null || rowsToDisplay.isEmpty()) {
            System.out.println("No rows.");
            return;
        }
        if (columns == null || columns.isEmpty()) {
            System.out.println("No columns.");
            return;
        }

        int totalRows = rowsToDisplay.size();
        int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);
        int currentPage = 1;
        Map<Integer, Integer> columnWidths = new HashMap<>();

        try {
            for (int i = 0; i < columns.size(); i++) {
                int maxWidth = columns.get(i).getName().length();
                maxWidth = Math.max(maxWidth, (columns.get(i).getName() + " - " + columns.get(i).getType().name()).length());
                for (Row row : rowsToDisplay) {
                    if (i < row.size()) {
                        maxWidth = Math.max(maxWidth, FileHandler.formatValueAsString(row.getValue(i)).length());
                    }
                }
                columnWidths.put(i, Math.max(maxWidth, 5));
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error calculating display widths: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n--- " + title + " (Page " + currentPage + "/" + totalPages + ") ---");
            try {
                StringBuilder headerLine = new StringBuilder("|");
                StringBuilder separatorLine = new StringBuilder("|");
                for (int i = 0; i < columns.size(); i++) {
                    String headerText = columns.get(i).getName() + " - " + columns.get(i).getType().name();
                    String paddedHeader = FileHandler.padRight(headerText, columnWidths.get(i));
                    headerLine.append(paddedHeader);
                    separatorLine.append(FileHandler.repeatChar('-', columnWidths.get(i)));
                    if (i < columns.size() - 1) {
                        headerLine.append(" | ");
                        separatorLine.append("-+-");
                    }
                }
                headerLine.append(" |");
                separatorLine.append(" |");
                System.out.println(headerLine.toString());
                System.out.println(separatorLine.toString());

                int start = (currentPage - 1) * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, totalRows);
                for (int i = start; i < end; i++) {
                    Row row = rowsToDisplay.get(i);
                    System.out.print("|");
                    for (int j = 0; j < columns.size(); j++) {
                        String valStr = "";
                        if (j < row.size()) {
                            valStr = FileHandler.formatValueAsString(row.getValue(j));
                        } else {
                            valStr = "[NoData]";
                        }
                        System.out.print(FileHandler.padRight(valStr, columnWidths.get(j)));
                        if (j < columns.size() - 1) System.out.print(" | ");
                    }
                    System.out.println(" |");
                }
                System.out.println("--- Rows " + (start + 1) + "-" + end + " of " + totalRows + " ---");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Error displaying row data: " + e.getMessage());
                break;
            }

            if (totalPages <= 1) break;
            System.out.print("Options: [N]ext, [P]revious, [E]xit > ");
            String choice = "";
            try {
                choice = inputScanner.nextLine().trim().toUpperCase();
            } catch (Exception e) {
                System.out.println("Input error.");
                break;
            }
            if (choice.equals("N")) {
                if (currentPage < totalPages) currentPage++;
                else System.out.println("Last page.");
            } else if (choice.equals("P")) {
                if (currentPage > 1) currentPage--;
                else System.out.println("First page.");
            } else if (choice.equals("E")) break;
            else System.out.println("Invalid option.");
        }
    }
}