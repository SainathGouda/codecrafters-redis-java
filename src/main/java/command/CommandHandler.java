package command;

import constant.ResponseConstants;
import data.Storage;
import util.RespParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class CommandHandler {
    Storage storage;

    public CommandHandler(Storage storage) {
        this.storage = storage;
    }

    public void handlePing(BufferedWriter outputStream) throws IOException {
        RespParser.writeSimpleString(ResponseConstants.PONG, outputStream);
    }

    public void handleEcho(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        List<String> messages = commandWithArgs.getArguments();
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
            RespParser.writeErrorString(outputStream);
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

    public void handleBLPop(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String key = commandWithArgs.getKey();
        long timeoutValue = Long.parseLong(commandWithArgs.getValue());

        List<String> popped = storage.removeFromList(key, timeoutValue);
        if (popped.isEmpty()) RespParser.writeNullBulkString(outputStream);
        else RespParser.writeArray(popped.size(), popped, outputStream);
    }
}
