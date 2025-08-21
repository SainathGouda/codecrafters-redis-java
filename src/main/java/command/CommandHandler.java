package command;

import constant.ResponseConstants;
import data.Storage;
import util.RespParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class CommandHandler {
    Storage storage = new Storage();

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
}
