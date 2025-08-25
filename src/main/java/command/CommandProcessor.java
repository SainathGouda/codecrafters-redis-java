package command;

import constant.CommandConstants;
import constant.ResponseConstants;
import data.Storage;
import util.RespParser;

import java.io.BufferedWriter;
import java.io.IOException;

public class CommandProcessor {
    public final CommandHandler commandHandler;
    public final Storage storage;

    public CommandProcessor(CommandHandler commandHandler, Storage storage) {
        this.commandHandler = commandHandler;
        this.storage = storage;
    }

    public void processCommand(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String commandName = commandWithArgs.getCommand();

        if (!commandName.equals(CommandConstants.EXEC) && storage.multiExist()){
            System.out.println("Entering queue: "+Thread.currentThread());
            storage.addTransaction(commandWithArgs);
            RespParser.writeSimpleString(ResponseConstants.QUEUED, outputStream);
            return;
        }
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
            case CommandConstants.LLEN:
                commandHandler.handleLLen(outputStream, commandWithArgs);
                break;
            case CommandConstants.LPOP:
                commandHandler.handleLPop(outputStream, commandWithArgs);
                break;
            case CommandConstants.TYPE:
                commandHandler.handleType(outputStream, commandWithArgs);
                break;
            case CommandConstants.XADD:
                commandHandler.handleXAdd(outputStream, commandWithArgs);
                break;
            case CommandConstants.XRANGE:
                commandHandler.handleXRange(outputStream, commandWithArgs);
                break;
            case CommandConstants.XREAD:
                commandHandler.handleXRead(outputStream, commandWithArgs);
                break;
            case CommandConstants.INCR:
                commandHandler.handleIncr(outputStream, commandWithArgs);
                break;
            case CommandConstants.MULTI:
                commandHandler.handleMulti(outputStream, commandWithArgs);
                break;
            case CommandConstants.EXEC:
                commandHandler.handleExec(outputStream, commandWithArgs);
                break;
        }
    }
}
