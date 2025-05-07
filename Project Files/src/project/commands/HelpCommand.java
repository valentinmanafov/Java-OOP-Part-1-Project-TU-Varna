package project.commands;
import project.CommandHandler;

public class HelpCommand implements CommandHandler {

    @Override
    public void execute(String[] args) {
        System.out.println("Available project.commands:");
        System.out.println("createtable <name> <c1> <t1>... - Create a new empty table");
        System.out.println("import <file.txt>      - Import table from TXT file");
        System.out.println("showtables             - List all tables");
        System.out.println("describe <table>       - Show table structure");
        System.out.println("print <table>          - Display table contents");
        System.out.println("export <table> <file.txt> - Export table to TXT file");
        System.out.println("select <col> <val> <table> - Select rows with value");
        System.out.println("addcolumn <table> <name> <type> - Add new column");
        System.out.println("update <table> <sCol> <sVal> <tCol> <tVal> - Update rows");
        System.out.println("delete <table> <col> <val> - Delete matching rows");
        System.out.println("insert <table> <values...> - Insert new row");
        System.out.println("innerjoin <t1> <c1_idx> <t2> <c2_idx> - Join two tables (auto-names new table)");
        System.out.println("rename <old> <new>     - Rename table");
        System.out.println("count <table> <col> <val> - Count matching rows");
        System.out.println("aggregate <table> <sCol> <sVal> <tCol> <op> - Perform aggregation");
        System.out.println("exit                   - Exit program");
        System.out.println("help                   - Show this help");
    }
}