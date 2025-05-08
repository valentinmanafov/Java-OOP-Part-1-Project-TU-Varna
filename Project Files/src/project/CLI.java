package project;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import project.commands.*;

class CLI {
    private final Database database = new Database();
    private final Scanner inputScanner = new Scanner(System.in);
    private final Map<String, CommandHandler> commandMap = new HashMap<>();

    public CLI() {
        initializeCommands();
    }

    private void initializeCommands() {
        commandMap.put("createtable", new CreateTableCommand(database));
        //commandMap.put("import", new ImportCommand(database));
        commandMap.put("showtables", new ShowTablesCommand(database));
        commandMap.put("describe", new DescribeCommand(database));
        commandMap.put("print", new PrintCommand(database, inputScanner));
        //commandMap.put("export", new ExportCommand(database));
        commandMap.put("select", new SelectCommand(database, inputScanner));
        commandMap.put("addcolumn", new AddColumnCommand(database));
        commandMap.put("update", new UpdateCommand(database));
        commandMap.put("delete", new DeleteCommand(database));
        commandMap.put("insert", new InsertCommand(database));
        commandMap.put("innerjoin", new InnerJoinCommand(database));
        commandMap.put("rename", new RenameCommand(database));
        commandMap.put("count", new CountCommand(database));
        commandMap.put("aggregate", new AggregateCommand(database));
        commandMap.put("help", new HelpCommand());
        commandMap.put("exit", new ExitCommand(inputScanner));
    }

    public void start() {
        System.out.println("Simple Database CLI. Type 'help' for commands.");
        while (true) {
            System.out.print("> ");
            String input = inputScanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] tokens = input.split("\\s+");
            String command = tokens[0].toLowerCase();
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
            CommandHandler handler = commandMap.get(command);
            if (handler != null) {
                try {
                    handler.execute(args);
                } catch (Exception e) {
                    System.out.println("An unexpected internal error occurred: " + e.getMessage());
                }
            } else {
                System.out.println("Unknown command: '" + command + "'.");
            }
        }
    }
}