package command;

import constant.CommandConstants;
import constant.ResponseConstants;
import data.Storage;
import util.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HexFormat;
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

        for (OutputStream slaveOutputStream : storage.getSlaveOutputStreams()) {
            RespParser.writeSETCommand(slaveOutputStream, key, value);
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

    //Lists
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

    //Streams
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

    //Transactions
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

    //Replications
    public void handleInfo(BufferedWriter outputStream) throws IOException {
        RespParser.writeBulkString("role:"+storage.getRole()
                +"master_replid:"+storage.getMasterReplId()
                +"master_repl_offset:"+storage.getMasterReplOffset(), outputStream);
    }

    public void handleReplConf(BufferedWriter outputStream) throws IOException {
        RespParser.writeSimpleString(ResponseConstants.OK, outputStream);
    }

    public void handlePsync() throws IOException {
        OutputStream clientOutputStream = storage.getClientSocket().getOutputStream();
        String response = ResponseConstants.FULLRESYNC+" "+storage.getMasterReplId()+" "+storage.getMasterReplOffset();
        RespParser.writeSimpleString(response, clientOutputStream);

        //Dummy redisDB file
        byte[] dbFile = HexFormat.of().parseHex("524544495330303131fa0972656469732d76657205372e322e30fa0a72656469732d62697473c040fa056374696d65c26d08bc65fa08757365642d6d656dc2b0c41000fa08616f662d62617365c000fff06e3bfec0ff5aa2");
        RespParser.writeRDBFile(dbFile, clientOutputStream);

        //Add the slave stream
        storage.setSlaveOutputStream(clientOutputStream);
    }

    //Persistence
    public void handleConfig(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        List<String> arguments = commandWithArgs.arguments();
        String configCommand = arguments.getFirst();
        if(CommandConstants.GET.equalsIgnoreCase(configCommand)){
            String arg = arguments.get(1);
            String argValue = storage.getRdbFileConfig(arg);
            List<String> fileConfig = new ArrayList<>();
            fileConfig.add(arg);
            fileConfig.add(argValue);
            RespParser.writeArray(fileConfig.size(), fileConfig, outputStream);
        }
    }

    public void handleKeys(BufferedWriter outputStream) throws IOException {
        RespParser.writeArrayLength(storage.getValueSize(), outputStream);
        for (String key : storage.getValueKeySet().keySet()) {
            RespParser.writeBulkString(key, outputStream);
        }
    }

    //Sorted Sets
    public void handleZAdd(BufferedWriter outputStream, CommandParser.CommandWithArgs commandWithArgs) throws IOException {
        String zSetKey = commandWithArgs.getKey();
        List<String> arguments = commandWithArgs.getArgumentsWithoutKey();
        double zSetScore = Double.parseDouble(arguments.get(0));
        String zSetMember = arguments.get(1);

        storage.addMember(zSetKey, zSetMember, zSetScore);
        RespParser.writeIntegerString(1, outputStream);
    }
}
