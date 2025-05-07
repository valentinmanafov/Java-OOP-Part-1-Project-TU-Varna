package project.commands;
import project.CommandHandler;

import java.util.Scanner;

public class ExitCommand implements CommandHandler {

    private Scanner inputScanner;

    public ExitCommand(Scanner scanner) {
        this.inputScanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        System.out.println("Exiting the program...");
        if (inputScanner != null) {
            inputScanner.close();
        }
        System.exit(0);
    }
}