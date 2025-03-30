import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;


class CLI {
    private final CLICommands commandHandler = new CLICommands();
    private final Map<String, CommandHandler> commandMap = new HashMap<>();

    public CLI() {
        initializeCommands();
    }

    interface CommandHandler {
        void execute(String[] args);
    }

    private void initializeCommands() {
        commandMap.put("import", args -> commandHandler.handleImport(args));
        commandMap.put("showtables", args -> commandHandler.handleShowTables(args));
        commandMap.put("describe", args -> commandHandler.handleDescribe(args));
        commandMap.put("print", args -> commandHandler.handlePrint(args));
        commandMap.put("export", args -> commandHandler.handleExport(args));
        commandMap.put("select", args -> commandHandler.handleSelect(args));
        commandMap.put("addcolumn", args -> commandHandler.handleAddColumn(args));
        commandMap.put("update", args -> commandHandler.handleUpdate(args));
        commandMap.put("delete", args -> commandHandler.handleDelete(args));
        commandMap.put("insert", args -> commandHandler.handleInsert(args));
        commandMap.put("innerjoin", args -> commandHandler.handleInnerJoin(args));
        commandMap.put("rename", args -> commandHandler.handleRename(args));
        commandMap.put("count", args -> commandHandler.handleCount(args));
        commandMap.put("aggregate", args -> commandHandler.handleAggregate(args));
        commandMap.put("help", args -> commandHandler.handleHelp(args));
        commandMap.put("exit", args -> commandHandler.handleExit(args));
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Database CLI. Type 'help' for commands.");

        while(true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if(input.isEmpty()) continue;

            String[] tokens = input.split("\\s+");
            String command = tokens[0].toLowerCase();
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            if(commandMap.containsKey(command)) {
                commandMap.get(command).execute(args);
            } else {
                System.out.println("Unknown command: " + command);
            }
        }
    }
}