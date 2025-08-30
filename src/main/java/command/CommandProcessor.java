package command;

import constant.CommandConstants;
import constant.ResponseConstants;
import data.Storage;
import util.RespParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record CommandProcessor(CommandHandler commandHandler, Storage storage) {
    private boolean isSubscribed(String commandName, BufferedWriter outputStream) throws IOException {
        if (storage.isSubscribed()) {
            outputStream.write("-ERR Can't execute '"+commandName+"': only (P|S)SUBSCRIBE / (P|S)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context\r\n");
            return true;
        }
        else { return false; }
    }

    public void processCommand(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String commandName = commandWithArgs.getCommand();

        if (!commandName.equals(CommandConstants.EXEC) && !commandName.equals(CommandConstants.DISCARD) && storage.multiExist()) {
            storage.addTransaction(commandWithArgs);
            RespParser.writeSimpleString(ResponseConstants.QUEUED, outputStream);
            return;
        }
        switch (commandName) {
            case CommandConstants.PING:
                if(storage.isSubscribed()) {
                    List<String> respond = new ArrayList<>();
                    respond.add(ResponseConstants.PONG.toLowerCase(Locale.ROOT));
                    respond.add(ResponseConstants.BLANK);
                    RespParser.writeArray(respond.size(),respond, outputStream);
                    return;
                }
                commandHandler.handlePing(outputStream);
                break;
            case CommandConstants.ECHO:
                if (isSubscribed(commandName, outputStream)) return;
                commandHandler.handleEcho(outputStream, commandWithArgs);
                break;
            case CommandConstants.SET:
                if (isSubscribed(commandName, outputStream)) return;
                commandHandler.handleSet(outputStream, commandWithArgs);
                break;
            case CommandConstants.GET:
                if (isSubscribed(commandName, outputStream)) return;
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
            case CommandConstants.ZREM:
                commandHandler.handleZRem(outputStream, commandWithArgs);
                break;
            //Geospatial
            case CommandConstants.GEOADD:
                commandHandler.handleGeoAdd(outputStream, commandWithArgs);
                break;
            case CommandConstants.GEOPOS:
                commandHandler.handleGeoPos(outputStream, commandWithArgs);
                break;
            //Pub/Sub
            case CommandConstants.SUBSCRIBE:
                commandHandler.handleSubscribe(outputStream, commandWithArgs);
                break;
        }
    }
}
