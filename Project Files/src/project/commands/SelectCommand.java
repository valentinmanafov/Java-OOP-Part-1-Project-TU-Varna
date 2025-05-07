package project.commands;

import project.*;
import java.util.*;

public class SelectCommand implements CommandHandler {

    private final Database database;
    private final Scanner inputScanner;
    private static final int PAGE_SIZE = 15;

    public SelectCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: select <col_idx> <value> <table_name>");
                return;
            }
            String columnNStr = args[0];
            String searchValue = args[1];
            String tableName = args[2];
            Table table = database.getTable(tableName);
            int columnIndex;
            try {
                columnIndex = Integer.parseInt(columnNStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(columnIndex);
            List<Row> matchingRows = new ArrayList<>();
            try {
                for (Row row : table.getRows()) {
                    if (TypeParser.looselyEquals(row.getValue(columnIndex), searchValue, searchColumn.getType())) {
                        matchingRows.add(row);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("Internal error during select", e);
            }

            if (matchingRows.isEmpty()) {
                System.out.println("No rows found matching criteria.");
                return;
            }

            System.out.println("Selected rows from '" + tableName + "' where column " + columnIndex + " ('" + searchColumn.getName() + "') == '" + searchValue + "':");
            displayRowsPaginated(tableName + " (Selected)", table.getColumns(), matchingRows);
            System.out.println("Finished displaying selected rows.");

        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during select display: " + e.getMessage());
        }
    }

    private void displayRowsPaginated(String title, List<Column> columns, List<Row> rowsToDisplay) {
        if (rowsToDisplay == null || rowsToDisplay.isEmpty()) { System.out.println("No rows."); return; }
        if (columns == null || columns.isEmpty()) { System.out.println("No columns."); return; }

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
                    for(int j=0; j<columns.size(); j++){
                        String valStr = "";
                        if (j < row.size()){
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
            if (choice.equals("N")) { if (currentPage < totalPages) currentPage++; else System.out.println("Last page."); }
            else if (choice.equals("P")) { if (currentPage > 1) currentPage--; else System.out.println("First page."); }
            else if (choice.equals("E")) break;
            else System.out.println("Invalid option.");
        }
    }
}