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

    public void handleImport(String[] args) { notImplemented("import"); }
    public void handlePrint(String[] args) { notImplemented("print"); }
    public void handleExport(String[] args) { notImplemented("export"); }
    public void handleSelect(String[] args) { notImplemented("select"); }
    public void handleUpdate(String[] args) { notImplemented("update"); }
    public void handleDelete(String[] args) { notImplemented("delete"); }
    public void handleInsert(String[] args) { notImplemented("insert"); }
    public void handleInnerJoin(String[] args) { notImplemented("innerjoin"); }
    public void handleCount(String[] args) { notImplemented("count"); }
    public void handleAggregate(String[] args) { notImplemented("aggregate"); }

    private void notImplemented(String command) {
        System.out.println(command + " command is not implemented yet");
    }

    private void displayRowsPaginated(String title, List<Column> columns, List<Row> rowsToDisplay) {
        System.out.println("Pagination display for '" + title + "' not implemented yet.");
        if (rowsToDisplay != null && !rowsToDisplay.isEmpty()) {
            for (Row row : rowsToDisplay) { System.out.println(row); }
        }
    }
}