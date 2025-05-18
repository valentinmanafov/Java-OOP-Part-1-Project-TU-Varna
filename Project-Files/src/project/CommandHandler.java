package project;

/**
 * An interface for command handlers.
 * Implementations of this interface define how specific commands are executed.
 */
public interface CommandHandler {
    /**
     * Executes the command with the given arguments.
     * @param args The arguments for the command.
     */
    void execute(String[] args);
}