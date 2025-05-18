package project.commands;

import project.CommandHandler;

public class HelpCommand implements CommandHandler {
    @Override
    public void execute(String[] args) {
        System.out.println("Available commands:");
        System.out.println("open <filepath>        - Opens database file");
        System.out.println("close                  - Closes the currently open database file");
        System.out.println("save                   - Saves changes to the current database and table files");
        System.out.println("saveas <file>          - Saves changes to a new databasee <file> and associated table files");
        System.out.println("help                   - Prints this information");
        System.out.println("exit                   - Exits the program");
        System.out.println("--------------------------------------------------");
        System.out.println("createtable <table name> <column name> <column type>... - Create a new empty table");
        System.out.println("import <file.txt>      - Import table from TXT file and add to database");
        System.out.println("showtables             - List all tables registered in the database");
        System.out.println("describe <table>       - Show table structure");
        System.out.println("print <table>          - Display table contents (loads if needed)");
        System.out.println("export <table> <file.txt> - Export specific table to a TXT file");
        System.out.println("select <table> <column index> <value> - Select rows with value");
        System.out.println("addcolumn <table> <column name> <column type> - Add new column");
        System.out.println("update <table> <search column index> <search value> <target column index> <target value> - Update rows value");
        System.out.println("delete <table> <column index> <value> - Delete rows by matching rules");
        System.out.println("insert <table> <values...> - Insert new row (provide values for all columns)");
        System.out.println("innerjoin <table1> <column1 index> <table2> <column2> - Join two tables (auto-names new table)");
        System.out.println("rename <old> <new>     - Rename table (also renames associated file)");
        System.out.println("count <table> <column> <value> - Count matching rows");
        System.out.println("aggregate <table> <search column> <search value> <target column> <operation> - Perform aggregation");
    }
}