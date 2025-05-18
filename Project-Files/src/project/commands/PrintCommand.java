package project.commands;

import project.*;

import java.util.*;

/**
 * Command handler for displaying the contents of a table with pagination.
 */
public class PrintCommand implements CommandHandler {

    private final Database database;
    private final Scanner inputScanner;
    private static final int PAGE_SIZE = 10;

    /**
     * Constructs a PrintCommand.
     * @param database The database instance from which to retrieve the table.
     * @param scanner  The scanner for handling pagination input (Next, Previous, Exit).
     */
    public PrintCommand(Database database, Scanner scanner) {
        this.database = database;
        this.inputScanner = scanner;
    }

    /**
     * Executes the print command.
     * Retrieves the specified table and displays its rows in a paginated format.
     * Calculates column widths for a clean display.
     * Usage: print &lt;table&gt;
     * @param args Command arguments: table name.
     */
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
                System.out.println("WARNING: Table '" + tableName + "' has no columns to display.");
                return;
            }
            if (rows.isEmpty()) {
                System.out.println("WARNING: Table '" + tableName + "' has no rows to display.");

                return;
            }
            displayRowsPaginated(tableName, columns, rows);
            System.out.println("Finished displaying table '" + tableName + "'.");
        } catch (DatabaseOperationException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR: An unexpected error occurred during print: " + e.getMessage());
        }
    }

    /**
     * Displays rows of a table in a paginated manner.
     * Calculates column widths based on header and data content for better readability.
     * Allows user to navigate through pages or exit the view.
     * @param title The title to display for the current view (e.g., table name).
     * @param columns The list of {@link Column} objects for header display.
     * @param rowsToDisplay The list of {@link Row} objects to be displayed.
     */
    private void displayRowsPaginated(String title, List<Column> columns, List<Row> rowsToDisplay) {
        if (rowsToDisplay == null) {
            System.out.println("No rows to display.");
            return;
        }
        if (columns == null || columns.isEmpty()) {
            System.out.println("No columns to display headers for.");
            return;
        }

        int totalRows = rowsToDisplay.size();
        if (totalRows == 0) {
            System.out.println("Table '" + title + "' is empty.");
            StringBuilder headerLineEmpty = new StringBuilder("|");
            StringBuilder separatorLineEmpty = new StringBuilder("|");
            Map<Integer, Integer> emptyColumnWidths = new HashMap<>();
            for(int i = 0; i< columns.size(); i++){
                String headerText = columns.get(i).getName() + " - " + columns.get(i).getType().name();
                emptyColumnWidths.put(i, Math.max(headerText.length(),5));
                String paddedHeader = FileHandler.padRight(headerText, emptyColumnWidths.get(i));
                headerLineEmpty.append(paddedHeader);
                separatorLineEmpty.append(FileHandler.repeatChar('-', emptyColumnWidths.get(i)));
                if (i < columns.size() - 1) {
                    headerLineEmpty.append(" | ");
                    separatorLineEmpty.append("-+-");
                }
            }
            headerLineEmpty.append(" |");
            separatorLineEmpty.append(" |");
            System.out.println("\n--- " + title + " ---");
            System.out.println(headerLineEmpty.toString());
            System.out.println(separatorLineEmpty.toString());
            System.out.println("| " + FileHandler.padRight("(Table has no rows)", separatorLineEmpty.length() -4 ) + " |");
            System.out.println(separatorLineEmpty.toString().replace('-', '~'));
            return;
        }

        int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);
        int currentPage = 1;
        Map<Integer, Integer> columnWidths = new HashMap<>();

        // Calculate optimal column widths for display.
        // Width is based on the maximum length of the column header (Name - TYPE) or any data value in that column.
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
            System.out.println("ERROR: Calculating display widths: " + e.getMessage());
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
                        if (j < columns.size() - 1) {
                            System.out.print(" | ");
                        }
                    }
                    System.out.println(" |");
                }
                System.out.println("--- Rows " + (start + 1) + "-" + end + " of " + totalRows + " ---");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("ERROR: Displaying row data: " + e.getMessage());
                break;
            }

            if (totalPages <= 1) break;

            System.out.print("Options: [N]ext, [P]revious, [E]xit > ");
            String choice;
            try {
                choice = inputScanner.nextLine().trim().toUpperCase();
            } catch (Exception e) {
                System.out.println("Input error. Exiting pagination.");
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
                System.out.println("Invalid option. Please use N, P, or E.");
            }
        }
    }
}