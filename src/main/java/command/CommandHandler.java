package command;

import constant.ResponseConstants;
import data.Storage;
import util.RespParser;
import util.XAddValidation;
import util.XRangeValidation;
import util.XReadValidation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class CommandHandler {
    Storage storage;
    XAddValidation xAddValidation = new XAddValidation();
    XRangeValidation xRangeValidation = new XRangeValidation();
    XReadValidation xReadValidation = new XReadValidation();

    public CommandHandler(Storage storage) {
        this.storage = storage;
    }

    public void handlePing(BufferedWriter outputStream) throws IOException {
        RespParser.writeSimpleString(ResponseConstants.PONG, outputStream);
    }

    public void handleEcho(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        List<String> messages = commandWithArgs.arguments();
        String message = messages.getFirst();
        RespParser.writeBulkString(message, outputStream);
    }

    public void handleSet(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        String value = commandWithArgs.getValue();
        long ttl = commandWithArgs.getTTL() == -1 ? -1 : System.currentTimeMillis() + commandWithArgs.getTTL();

        if (key != null && value != null) {
            storage.setData(key, value, ttl);
            RespParser.writeSimpleString(ResponseConstants.OK, outputStream);
        } else {
            RespParser.writeErrorString(ResponseConstants.ERROR, outputStream);
        }
    }

    public void handleGet(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        String value = storage.getValue(key);

        if (storage.hasKey(key)) {
            RespParser.writeBulkString(value, outputStream);
        } else {
            RespParser.writeNullBulkString(outputStream);
        }
    }

    public void handleRPush(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        List<String> values = commandWithArgs.getArgumentsWithoutKey();

        storage.setList(key, values);
        int listLength = storage.getListLength(key);
        RespParser.writeIntegerString(listLength, outputStream);
    }

    public void handleLRange(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        int listStartIndex = commandWithArgs.listStartIndex();
        int listEndIndex = commandWithArgs.listEndIndex();

        List<String> array = storage.getList(key, listStartIndex, listEndIndex);
        int arrLength = array.size();
        RespParser.writeArray(arrLength, array, outputStream);
    }

    public void handleLPush(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        List<String> values = commandWithArgs.getArgumentsWithoutKey();

        storage.setListLeft(key, values);
        int listLength = storage.getListLength(key);
        RespParser.writeIntegerString(listLength, outputStream);
    }

    public void handleLLen(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        int listLength = storage.getListLength(key);

        RespParser.writeIntegerString(listLength, outputStream);
    }

    public void handleLPop(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        if (storage.getListLength(key) == 0) {
            RespParser.writeNullBulkString(outputStream);
        }
        int removeCount = Integer.parseInt(commandWithArgs.getRemoveCount());

        if (removeCount == 0) {
            String popped = storage.removeFromList(key);
            RespParser.writeBulkString(popped, outputStream);
        }
        else {
            List<String> popped = storage.removeFromList(key, removeCount);
            RespParser.writeArray(popped.size(), popped, outputStream);
        }
    }

    public void handleType(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        String dataType = storage.getStoredType(key);

        RespParser.writeSimpleString(dataType, outputStream);
    }

    public void handleXAdd(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String streamKey = commandWithArgs.getKey();
        String entryId = commandWithArgs.getStreamEntryId();
        List<String> streamEntries = commandWithArgs.getStreamEntries();

        entryId = xAddValidation.isValid(streamKey, entryId, storage, outputStream);
        if (entryId.isEmpty()) { return; }

        storage.addStreamEntries(streamKey, entryId, streamEntries);

        RespParser.writeBulkString(entryId, outputStream);
    }

    public void handleXRange(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String streamKey = commandWithArgs.getKey();
        String startId = commandWithArgs.getXStartId();
        String endId = commandWithArgs.getXEndId();

        xRangeValidation.isValid(streamKey, startId, endId, storage, outputStream);
    }

    public void handleXRead(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        xReadValidation.isValid(commandWithArgs, storage, outputStream);
    }

    public void handleIncr(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();

        int value = storage.incr(key);
        if (value == -1) {
            RespParser.writeErrorString(ResponseConstants.NUMBER_FORMAT_EXCEPTION, outputStream);
            return;
        }
        RespParser.writeIntegerString(value, outputStream);
    }

    public void handleMulti(BufferedWriter outputStream) throws IOException {
        storage.multi();
        RespParser.writeSimpleString(ResponseConstants.OK, outputStream);
    }

    public void handleExec(BufferedWriter outputStream) throws IOException {
        if (!storage.multiExist()){
            RespParser.writeErrorString(ResponseConstants.EXEC_WITHOUT_MULTI, outputStream);
            return;
        }

        List<CommandParser.CommandWithArgs> queue = storage.execute();
        CommandProcessor commandProcessor = new CommandProcessor(this, storage);
        RespParser.writeArrayLength(queue.size(), outputStream);
        for (CommandParser.CommandWithArgs command : queue) {
            commandProcessor.processCommand(outputStream, command);
        }
    }

    public void handleDiscard(BufferedWriter outputStream) throws IOException {
        if (!storage.multiExist()) {
            RespParser.writeErrorString(ResponseConstants.DISCARD_WITHOUT_MULTI, outputStream);
        } else {
            storage.discardTransactions();
            RespParser.writeSimpleString(ResponseConstants.OK, outputStream);
        }
    }

    public void handleInfo(BufferedWriter outputStream) throws IOException {
        RespParser.writeBulkString("role:master", outputStream);
    }
}
