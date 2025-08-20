package command;

import constant.CommandConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class CommandProcessor {
    public final CommandHandler commandHandler;

    public CommandProcessor(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public void processCommand(String command, BufferedWriter outputStream, BufferedReader inputStream) throws IOException {
        String commandName = command.toUpperCase();

        switch (commandName) {
            case CommandConstants.PING:
                commandHandler.handlePing(outputStream);
                break;
            case CommandConstants.ECHO:
                commandHandler.handleEcho(outputStream, inputStream);
                break;
            case CommandConstants.SET:
                commandHandler.handleSet(outputStream, inputStream);
                break;
            case CommandConstants.GET:
                commandHandler.handleGet(outputStream, inputStream);
                break;
        }
    }
}
