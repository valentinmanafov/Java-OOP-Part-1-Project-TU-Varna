package project;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
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
        commandMap.put("export", new ExportCommand(database));
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

    private List<String> parseArguments(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;

        for (char c : input.toCharArray()) {
            if (escapeNext) {
                currentToken.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
                // Append backslash only if not escaping quote/backslash for TypeParser later
            } else if (c == '"') {
                inQuotes = !inQuotes;
                // Include quotes in the token for TypeParser to handle later
                currentToken.append(c);
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0); // Reset token
                }
                // Ignore whitespace between tokens
            } else {
                currentToken.append(c);
            }
        }
        // Add the last token if any
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        // Basic handling if quotes are unclosed
        if (inQuotes) {
            System.out.println("Warning: Unclosed quote in input.");
        }

        return tokens;
    }


    public void start() {
        System.out.println("Simple Database CLI. Type 'help' for commands.");
        while (true) {
            System.out.print("> ");
            String input = inputScanner.nextLine().trim();
            if (input.isEmpty()) continue;

            List<String> tokensList = parseArguments(input);

            if (tokensList.isEmpty()) continue;

            String command = tokensList.get(0).toLowerCase();
            // Convert remaining tokens to String array for command handlers
            String[] args = tokensList.subList(1, tokensList.size()).toArray(new String[0]);

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