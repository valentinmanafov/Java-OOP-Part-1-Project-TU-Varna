import java.util.*;

public class CLICommands {
    private final Database database;
    private final Scanner inputScanner = new Scanner(System.in);
    private static final int PAGE_SIZE = 15;

    public CLICommands(Database database) {
        this.database = database;
    }

    public void handleHelp(String[] args) {
        System.out.println("Available commands:");
        System.out.println("import <file>          - Import table from file");
        System.out.println("showtables             - List all tables");
        System.out.println("describe <table>       - Show table structure");
        System.out.println("print <table>          - Display table contents");
        System.out.println("export <table> <file>  - Export table to file");
        System.out.println("select <col> <val> <table> - Select rows with value");
        System.out.println("addcolumn <table> <name> <type> - Add new column");
        System.out.println("update <table> <sCol> <sVal> <tCol> <tVal> - Update rows");
        System.out.println("delete <table> <col> <val> - Delete matching rows");
        System.out.println("insert <table> <values...> - Insert new row");
        System.out.println("innerjoin <t1> <c1> <t2> <c2> - Join two tables");
        System.out.println("rename <old> <new>     - Rename table");
        System.out.println("count <table> <col> <val> - Count matching rows");
        System.out.println("aggregate <table> <sCol> <sVal> <tCol> <op> - Perform aggregation");
        System.out.println("exit                   - Exit program");
        System.out.println("help                   - Show this help");
    }

    public void handleExit(String[] args) {
        System.out.println("Exiting the program...");
        inputScanner.close();
        System.exit(0);
    }

    public void handleShowTables(String[] args) {
        try {
            Set<String> tableNamesSet = database.getTableNames();
            if (tableNamesSet.isEmpty()) {
                System.out.println("No tables loaded.");
            } else {
                List<String> tableNamesList = new ArrayList<>(tableNamesSet);
                Collections.sort(tableNamesList);
                System.out.println("Loaded Tables:");
                for (String name : tableNamesList) {
                    System.out.println("  - " + name);
                }
            }
        } catch (Exception e) {
            System.out.println("Error listing tables: " + e.getMessage());
        }
    }

    public void handleDescribe(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: describe <table_name>");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName);
            System.out.println("Structure for Table: '" + table.getName() + "'");
            List<Column> columns = table.getColumns();
            if (columns.isEmpty()) {
                System.out.println("  (No columns defined)");
            } else {
                System.out.println("Columns:");
                for (int i = 0; i < columns.size(); i++) {
                    Column col = columns.get(i);
                    System.out.printf("  [%d] %s (%s)\n", i, col.getName(), col.getType());
                }
            }
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleRename(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: rename <old_table_name> <new_table_name>");
                return;
            }
            String oldName = args[0];
            String newName = args[1];
            database.renameTable(oldName, newName);
        } catch (DatabaseOperationException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleAddColumn(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: addcolumn <table_name> <col_name> <type>");
                System.out.println("Types: Integer, Double, String");
                return;
            }
            String tableName = args[0];
            String colName = args[1];
            String typeName = args[2];
            Table table = database.getTable(tableName);
            DataType colType;
            try {
                colType = DataType.valueOf(typeName.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type: " + typeName);
                return;
            }
            table.addColumn(new Column(colName, colType));
            System.out.println("Column '" + colName + "' added to '" + tableName + "'.");
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleInsert(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Usage: insert <table_name> <value1> ...");
                return;
            }
            String tableName = args[0];
            Table table = database.getTable(tableName);
            List<Column> columns = table.getColumns();
            int expectedValues = columns.size();
            int providedValues = args.length - 1;
            if (providedValues != expectedValues) {
                System.out.println("Error: Expected " + expectedValues + " values, got " + providedValues);
                return;
            }
            List<Object> parsedValues = new ArrayList<>();
            for (int i = 0; i < expectedValues; i++) {
                String inputValue = args[i + 1];
                Column column = columns.get(i);
                try {
                    parsedValues.add(TypeParser.parse(inputValue, column.getType()));
                } catch (DatabaseOperationException e) {
                    System.out.println("Error parsing value for column '" + column.getName() + "': " + e.getMessage());
                    return;
                }
            }
            table.addRow(new Row(parsedValues));
            System.out.println("Row inserted into '" + tableName + "'.");
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleDelete(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: delete <table_name> <col_idx> <value>");
                return;
            }
            String tableName = args[0];
            String searchColNStr = args[1];
            String searchValue = args[2];
            Table table = database.getTable(tableName);
            int searchColIndex;
            try {
                searchColIndex = Integer.parseInt(searchColNStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(searchColIndex);
            List<Row> remainingRows = new ArrayList<>();
            List<Row> currentRows = table.getRows();
            int originalRowCount = currentRows.size();
            try {
                for(Row row : currentRows) {
                    if (!TypeParser.looselyEquals(row.getValue(searchColIndex), searchValue, searchColumn.getType())) {
                        remainingRows.add(row);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("Internal error during delete", e);
            }
            int deletedCount = originalRowCount - remainingRows.size();
            if (deletedCount > 0) {
                table.setRows(remainingRows);
                System.out.println("Deleted " + deletedCount + " row(s).");
            } else {
                System.out.println("No rows matched criteria.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleUpdate(String[] args) {
        try {
            if (args.length != 5) {
                System.out.println("Usage: update <tbl> <s_idx> <s_val> <t_idx> <t_val>");
                return;
            }
            String tableName = args[0];
            String searchColNStr = args[1];
            String searchVal = args[2];
            String targetColNStr = args[3];
            String targetValStr = args[4];
            Table table = database.getTable(tableName);
            int searchColIndex, targetColIndex;
            try {
                searchColIndex = Integer.parseInt(searchColNStr);
                targetColIndex = Integer.parseInt(targetColNStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(searchColIndex);
            Column targetColumn = table.getColumn(targetColIndex);
            Object targetValueObject;
            try {
                targetValueObject = TypeParser.parse(targetValStr, targetColumn.getType());
            } catch (DatabaseOperationException e) {
                System.out.println("Error parsing target value: " + e.getMessage());
                return;
            }
            int updatedCount = 0;
            try {
                int rowCount = table.getRows().size();
                for (int i = 0; i < rowCount; i++) {
                    Row row = table.getRow(i);
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchVal, searchColumn.getType())) {
                        row.setValue(targetColIndex, targetValueObject);
                        updatedCount++;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("Internal error during update", e);
            }
            if (updatedCount > 0) {
                System.out.println("Updated " + updatedCount + " row(s).");
            } else {
                System.out.println("No rows matched criteria.");
            }
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleCount(String[] args) {
        try {
            if (args.length != 3) {
                System.out.println("Usage: count <table_name> <col_idx> <value>");
                return;
            }
            String tableName = args[0];
            String searchColNStr = args[1];
            String searchValue = args[2];
            Table table = database.getTable(tableName);
            int searchColIndex;
            try {
                searchColIndex = Integer.parseInt(searchColNStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid index.");
                return;
            }
            Column searchColumn = table.getColumn(searchColIndex);
            int count = 0;
            try {
                for (Row row : table.getRows()) {
                    if (TypeParser.looselyEquals(row.getValue(searchColIndex), searchValue, searchColumn.getType())) {
                        count++;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new DatabaseOperationException("Internal error during count", e);
            }
            System.out.println("Count: " + count);
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleSelect(String[] args) {
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
            System.out.println("Selected rows:");
            displayRowsPaginated(tableName + " (Selected)", table.getColumns(), matchingRows);
            System.out.println("Finished display. (Pagination not yet integrated)");
        } catch (DatabaseOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void handleImport(String[] args) { notImplemented("import"); }
    public void handlePrint(String[] args) { notImplemented("print"); }
    public void handleExport(String[] args) { notImplemented("export"); }
    public void handleInnerJoin(String[] args) { notImplemented("innerjoin"); }
    public void handleAggregate(String[] args) { notImplemented("aggregate"); }

    private void notImplemented(String command) {
        System.out.println(command + " command is not implemented yet");
    }

    private void displayRowsPaginated(String title, List<Column> columns, List<Row> rowsToDisplay) {
        System.out.println("\n--- Displaying: " + title + " ---");
        if (rowsToDisplay == null || rowsToDisplay.isEmpty()) {
            System.out.println("No rows.");
            return;
        }
        try {
            for (Row row : rowsToDisplay) {
                System.out.println(row.toString());
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error displaying row data: " + e.getMessage());
        }
        System.out.println("--- End of Display ---");
    }
}