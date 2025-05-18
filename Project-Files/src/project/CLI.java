package project;

import java.util.*;

import project.commands.*;

/**
 * Handles the command line interface for the database application.
 * It parses user input, maps commands to their handlers, and executes them.
 */
public class CLI {
    private final Database database = new Database();
    private final Scanner inputScanner = new Scanner(System.in);
    private final Map<String, CommandHandler> commandMap = new HashMap<>();

    /**
     * Constructs a new CLI and initializes the available commands.
     */
    public CLI() {
        initializeCommands();
    }

    /**
     * Initializes the command map with all available commands and their handlers.
     */
    private void initializeCommands() {
        commandMap.put("open", new OpenCommand(database));
        commandMap.put("close", new CloseCommand(database, inputScanner));
        commandMap.put("save", new SaveCommand(database));
        commandMap.put("saveas", new SaveAsCommand(database));
        commandMap.put("createtable", new CreateTableCommand(database));
        commandMap.put("import", new ImportCommand(database));
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
        commandMap.put("exit", new ExitCommand(database, inputScanner));
    }

    /**
     * Parses the raw input string into a list of arguments.
     * It handles arguments enclosed in double quotes and escape characters.
     * @param input The raw input string from the user.
     * @return A list of parsed arguments.
     */
    private List<String> parseArguments(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;
        // Iterates through each character of the input string.
        // If an escape character '\' is encountered, the next character is appended as is.
        // Double quotes '"' toggle the inQuotes flag, allowing spaces within quoted arguments.
        // Outside of quotes, whitespace characters delimit arguments.
        for (char c : input.toCharArray()) {
            if (escapeNext) {
                currentToken.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (c == '"') {
                inQuotes = !inQuotes;
                currentToken.append(c);
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        if (inQuotes) {
            System.out.println("WARNING: Unclosed quote in input.");
        }
        return tokens;
    }

    /**
     * Starts the command line interface loop.
     * It continuously prompts the user for input, parses it,
     * and executes the corresponding command.
     */
    public void start() {
        System.out.println("Simple Database CLI. Type 'help' for commands. Use 'open <catalog_filepath>' to begin.");
        while (true) {
            System.out.print("> ");
            String input = inputScanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            List<String> tokensList = parseArguments(input);
            if (tokensList.isEmpty()) {
                continue;
            }
            String command = tokensList.get(0).toLowerCase();
            String[] args = tokensList.subList(1, tokensList.size()).toArray(new String[0]);
            CommandHandler handler = commandMap.get(command);
            if (handler != null) {
                // Checks if a command requires an open catalog file.
                // Most commands do, except for 'open', 'help', and 'exit'.
                boolean needsOpen = !(command.equals("open") || command.equals("help") || command.equals("exit"));
                if (needsOpen && !database.isCatalogOpen()) {
                    System.out.println("ERROR: No catalog file open. Please use 'open <filepath>' first.");
                    continue;
                }
                try {
                    handler.execute(args);
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            } else {
                System.out.println("ERROR: Unknown command: '" + command + "'.");
            }
        }
    }
}