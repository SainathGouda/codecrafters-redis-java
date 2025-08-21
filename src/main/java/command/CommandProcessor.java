package command;

import constant.CommandConstants;

import java.io.BufferedWriter;
import java.io.IOException;

public class CommandProcessor {
    public final CommandHandler commandHandler;

    public CommandProcessor(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public void processCommand(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String commandName = commandWithArgs.getCommand();

        switch (commandName) {
            case CommandConstants.PING:
                commandHandler.handlePing(outputStream);
                break;
            case CommandConstants.ECHO:
                commandHandler.handleEcho(outputStream, commandWithArgs);
                break;
            case CommandConstants.SET:
                commandHandler.handleSet(outputStream, commandWithArgs);
                break;
            case CommandConstants.GET:
                commandHandler.handleGet(outputStream, commandWithArgs);
                break;
            case CommandConstants.RPUSH:
                commandHandler.handleRPush(outputStream, commandWithArgs);
                break;
            case CommandConstants.LRANGE:
                commandHandler.handleLRange(outputStream, commandWithArgs);
                break;
            case CommandConstants.LPUSH:
                commandHandler.handleLPush(outputStream, commandWithArgs);
                break;
        }
    }
}
