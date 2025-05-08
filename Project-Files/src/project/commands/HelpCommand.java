package project.commands;

import project.CommandHandler;

public class HelpCommand implements CommandHandler {
    @Override
    public void execute(String[] args) {
        System.out.println("Available commands:");
        System.out.println("open <file>            - Opens catalog file <file>");
        System.out.println("close                  - Closes the currently open catalog file");
        System.out.println("save                   - Saves changes to the current catalog and table files");
        System.out.println("saveas <file>          - Saves changes to a new catalog <file> and associated table files");
        System.out.println("help                   - Prints this information");
        System.out.println("exit                   - Exits the program");
        System.out.println("--------------------------------------------------");
        System.out.println("createtable <name> <c1> <t1>... - Create a new empty table");
        System.out.println("import <file.txt>      - Import table from TXT file and add to catalog");
        System.out.println("showtables             - List all tables registered in the catalog");
        System.out.println("describe <table>       - Show table structure");
        System.out.println("print <table>          - Display table contents (loads if needed)");
        System.out.println("export <table> <file.txt> - Export specific table to a TXT file");
        System.out.println("select <col> <val> <table> - Select rows with value");
        System.out.println("addcolumn <table> <name> <type> - Add new column");
        System.out.println("update <table> <sCol> <sVal> <tCol> <tVal> - Update rows");
        System.out.println("delete <table> <col> <val> - Delete matching rows");
        System.out.println("insert <table> <values...> - Insert new row (provide values for all columns)");
        System.out.println("innerjoin <t1> <c1_idx> <t2> <c2_idx> - Join two tables (auto-names new table)");
        System.out.println("rename <old> <new>     - Rename table (also renames associated file)");
        System.out.println("count <table> <col> <val> - Count matching rows");
        System.out.println("aggregate <table> <sCol> <sVal> <tCol> <op> - Perform aggregation");
    }
}