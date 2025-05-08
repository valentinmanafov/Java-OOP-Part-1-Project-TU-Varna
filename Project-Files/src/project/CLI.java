package project;

import java.util.*;

import project.commands.*;

public class CLI {
    private final Database database = new Database();
    private final Scanner inputScanner = new Scanner(System.in);
    private final Map<String, CommandHandler> commandMap = new HashMap<>();

    public CLI() {
        initializeCommands();
    }

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
        if (currentToken.length() > 0) tokens.add(currentToken.toString());
        if (inQuotes) System.out.println("WARNING: Unclosed quote in input.");
        return tokens;
    }

    public void start() {
        System.out.println("Simple Database CLI. Type 'help' for commands. Use 'open <catalog_filepath>' to begin.");
        while (true) {
            System.out.print("> ");
            String input = inputScanner.nextLine().trim();
            if (input.isEmpty()) continue;
            List<String> tokensList = parseArguments(input);
            if (tokensList.isEmpty()) continue;
            String command = tokensList.get(0).toLowerCase();
            String[] args = tokensList.subList(1, tokensList.size()).toArray(new String[0]);
            CommandHandler handler = commandMap.get(command);
            if (handler != null) {
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