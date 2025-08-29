package command;

import constant.CommandConstants;
import constant.ResponseConstants;
import data.Storage;
import util.RespParser;

import java.io.BufferedWriter;
import java.io.IOException;

public record CommandProcessor(CommandHandler commandHandler, Storage storage) {

    public void processCommand(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String commandName = commandWithArgs.getCommand();

        if (!commandName.equals(CommandConstants.EXEC) && !commandName.equals(CommandConstants.DISCARD) && storage.multiExist()) {
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
            //Lists
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
            //Streams
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
            //Transactions
            case CommandConstants.INCR:
                commandHandler.handleIncr(outputStream, commandWithArgs);
                break;
            case CommandConstants.MULTI:
                commandHandler.handleMulti(outputStream);
                break;
            case CommandConstants.EXEC:
                commandHandler.handleExec(outputStream);
                break;
            case CommandConstants.DISCARD:
                commandHandler.handleDiscard(outputStream);
                break;
            //Replications
            case CommandConstants.INFO:
                commandHandler.handleInfo(outputStream);
                break;
            case CommandConstants.REPLCONF:
                commandHandler.handleReplConf(outputStream);
                break;
            case CommandConstants.PSYNC:
                commandHandler.handlePsync();
                break;
            //Persistence
            case CommandConstants.CONFIG:
                commandHandler.handleConfig(outputStream, commandWithArgs);
                break;
            case CommandConstants.KEYS:
                commandHandler.handleKeys(outputStream);
                break;
            //Sorted Sets
            case CommandConstants.ZADD:
                commandHandler.handleZAdd(outputStream, commandWithArgs);
                break;
            case CommandConstants.ZRANK:
                commandHandler.handleZRank(outputStream, commandWithArgs);
                break;
            case CommandConstants.ZRANGE:
                commandHandler.handleZRange(outputStream, commandWithArgs);
                break;
            case CommandConstants.ZCARD:
                commandHandler.handleZCard(outputStream, commandWithArgs);
                break;
            case CommandConstants.ZSCORE:
                commandHandler.handleZScore(outputStream, commandWithArgs);
                break;
        }
    }
}
