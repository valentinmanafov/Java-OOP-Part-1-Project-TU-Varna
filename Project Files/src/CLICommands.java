import java.util.*;

public class CLICommands {
    private final Database database;
    private final Scanner inputScanner = new Scanner(System.in);
    private static final int PAGE_SIZE = 15;

    public CLICommands(Database database) {
        this.database = database;
    }

    public void handleHelp(String[] args) { notImplemented("help"); }
    public void handleExit(String[] args) { notImplemented("exit"); }
    public void handleImport(String[] args) { notImplemented("import"); }
    public void handleShowTables(String[] args) { notImplemented("showtables"); }
    public void handleDescribe(String[] args) { notImplemented("describe"); }
    public void handlePrint(String[] args) { notImplemented("print"); }
    public void handleExport(String[] args) { notImplemented("export"); }
    public void handleSelect(String[] args) { notImplemented("select"); }
    public void handleAddColumn(String[] args) { notImplemented("addcolumn"); }
    public void handleUpdate(String[] args) { notImplemented("update"); }
    public void handleDelete(String[] args) { notImplemented("delete"); }
    public void handleInsert(String[] args) { notImplemented("insert"); }
    public void handleInnerJoin(String[] args) { notImplemented("innerjoin"); }
    public void handleRename(String[] args) { notImplemented("rename"); }
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