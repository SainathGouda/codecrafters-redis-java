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

    public void processCommand(BufferedWriter outputStream, BufferedReader inputStream) throws IOException {
        CommandParser.RedisCommandParser redisCommandParser = new CommandParser.RedisCommandParser(inputStream);
        CommandParser.CommandWithArgs commandWithArgs = redisCommandParser.parseCommand();
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
        }
    }
}
