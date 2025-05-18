package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for selecting and displaying rows from a table that match a specific criterion.
 * The display is paginated.
 */
public class SelectCommand implements CommandHandler {

    private final Database database;
    private final Scanner inputScanner;
    private static final int PAGE_SIZE = 10;

    /**
     * Constructs a SelectCommand.
     * @param database The database instance from which to select data.
     * @param scanner  The scanner for handling pagination input.
     */
    public SelectCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    /**
     * Executes the select command.
     * Finds rows in the specified table where the value in a given column (by index)
     * matches a specified search value. The matching rows are then displayed in a paginated format.
     * Usage: select &lt;table&gt; &lt;column_index&gt; &lt;value_to_match&gt;
     * @param args Command arguments: table name, column index for search, value to match.
     */
    @Override
    public void execute(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: select <table> <column_index> <value>");
                return;
            }
            String tableName = args[0];
            String columnNStr = args[1];
            String searchValue = args[2];

            Table table = database.getTable(tableName);
            int columnIndex;
            try {
                columnIndex = Integer.parseInt(columnNStr);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid column index '" + columnNStr + "'. Index must be a number.");
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

                throw new DatabaseOperationException("ERROR: During select - internal error accessing row data by index.", e);
            }

            if (matchingRows.isEmpty()) {
                System.out.println("WARNING: No rows found in table '" + tableName + "' matching the criteria " +
                        "(column '" + searchColumn.getName() + "' == '" + searchValue + "').");
                return;
            }

            System.out.println("Selected rows from '" + tableName + "' where column " + columnIndex +
                    " ('" + searchColumn.getName() + "') == '" + searchValue + "':");

            displayRowsPaginated(tableName + " (Selected Results)", table.getColumns(), matchingRows);
            System.out.println("Finished displaying selected rows.");

        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {

            System.out.println("ERROR: An unexpected error occurred during select display: " + e.getMessage());
        }
    }

    /**
     * Displays rows in a paginated manner. Shared with PrintCommand.
     * Calculates column widths based on header and data content for better readability.
     * Allows user to navigate through pages or exit the view.
     * @param title The title to display for the current view.
     * @param columns The list of {@link Column} objects for header display.
     * @param rowsToDisplay The list of {@link Row} objects to be displayed.
     */
    private void displayRowsPaginated(String title, List<Column> columns, List<Row> rowsToDisplay) {

        if (rowsToDisplay == null || rowsToDisplay.isEmpty()) {
            System.out.println("No rows to display for: " + title);
            return;
        }
        if (columns == null || columns.isEmpty()) {
            System.out.println("No columns defined to display for: " + title);
            return;
        }

        int totalRows = rowsToDisplay.size();
        int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);
        int currentPage = 1;
        Map<Integer, Integer> columnWidths = new HashMap<>();

        try {
            for (int i = 0; i < columns.size(); i++) {
                String headerText = columns.get(i).getName() + " - " + columns.get(i).getType().name();
                int maxWidth = headerText.length();
                for (Row row : rowsToDisplay) {
                    if (i < row.size()) {
                        maxWidth = Math.max(maxWidth, FileHandler.formatValueAsString(row.getValue(i)).length());
                    }
                }
                columnWidths.put(i, Math.max(maxWidth, 5));
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("ERROR: Calculating display widths for '" + title + "': " + e.getMessage());
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
                        String valStr;
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
                System.out.println("ERROR: Displaying row data for '" + title + "': " + e.getMessage());
                break;
            }

            if (totalPages <= 1) break;
            System.out.print("Options: [N]ext, [P]revious, [E]xit > ");
            String choice;
            try {
                choice = inputScanner.nextLine().trim().toUpperCase();
            } catch (Exception e) {
                System.out.println("ERROR: Incorrect input. Exiting pagination for '" + title + "'.");
                break;
            }
            if (choice.equals("N")) {
                if (currentPage < totalPages) currentPage++;
                else System.out.println("Already on the last page.");
            } else if (choice.equals("P")) {
                if (currentPage > 1) currentPage--;
                else System.out.println("Already on the first page.");
            } else if (choice.equals("E")) {
                break;
            } else {
                System.out.println("ERROR: Invalid option. Please use N, P, or E.");
            }
        }
    }
}