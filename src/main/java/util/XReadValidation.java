package util;

import command.CommandParser;
import constant.RESPConstants;
import constant.ResponseConstants;
import data.Storage;
import data.StreamCache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class XReadValidation {
    public void isValids(CommandParser.CommandWithArgs commandWithArgs, Storage storage, BufferedWriter outputStream) throws IOException {
        List<String> arguments = commandWithArgs.getArguments();
        int blockIndex = arguments.indexOf("block");
        int streamsIndex = arguments.indexOf("streams");

        long blockTimeout = 0;
        boolean isBlocking = false;

        if (blockIndex != -1 && blockIndex + 1 < arguments.size()) {
            try {
                blockTimeout = Long.parseLong(arguments.get(blockIndex + 1));
                isBlocking = true;
            } catch (NumberFormatException e) {
                outputStream.write(RESPConstants.ERROR_PREFIX+ResponseConstants.INVALID_BLOCK_TIMEOUT+RESPConstants.CRLF);
                outputStream.flush();
                return;
            }
        }

        if (streamsIndex == -1) {
            outputStream.write(RESPConstants.ERROR_PREFIX+ResponseConstants.INVALID_STREAM+RESPConstants.CRLF);
            return;
        }

        // Extract stream keys and entry IDs
        List<String> streamKeys = arguments.subList(streamsIndex + 1, streamsIndex + 1 + (arguments.size() - streamsIndex - 1) / 2);
        List<String> entryIds = arguments.subList(streamsIndex + 1 + streamKeys.size(), arguments.size());

        if (streamKeys.size() != entryIds.size()) {
            outputStream.write(RESPConstants.ERROR_PREFIX+ResponseConstants.MISMATCH_STREAM+RESPConstants.CRLF);
            return;
        }

        long endTime = System.currentTimeMillis() + blockTimeout;
        System.out.println("BlockTimeout: "+blockTimeout);
        System.out.println("endTime: "+endTime);

        while (true) {
            boolean hasData = false;
            // Start constructing the RESP response
            StringBuilder response = new StringBuilder();
            response.append("*").append(streamKeys.size()).append("\r\n");

            for (int i = 0; i < streamKeys.size(); i++) {
                String streamKey = streamKeys.get(i);
                String entryId = entryIds.get(i);
                StreamCache streamCache = storage.getStreamCache(streamKey);

                entryId = entryId.contains("-") ? entryId : entryId + "-0";

                // Get entries with IDs greater than the specified entry ID
                TreeMap<String, List<String>> entries = new TreeMap<>(streamCache.getEntries().tailMap(entryId, false));

                if (!entries.isEmpty()) {
                    hasData = true;
                    response.append("*2\r\n")
                            .append("$").append(streamKey.length()).append("\r\n").append(streamKey).append("\r\n")
                            .append("*").append(entries.size()).append("\r\n");

                    for (var entry : entries.entrySet()) {
                        response.append("*2\r\n")
                                .append("$").append(entry.getKey().length()).append("\r\n").append(entry.getKey()).append("\r\n")
                                .append("*").append(entry.getValue().size()).append("\r\n");
                        for (String value : entry.getValue()) {
                            response.append("$").append(value.length()).append("\r\n").append(value).append("\r\n");
                        }
                    }
                }
                else {
                    response.append("*2\r\n")
                            .append("$").append(streamKey.length()).append("\r\n").append(streamKey).append("\r\n")
                            .append("*0\r\n");
                }

                if (hasData) {
                    outputStream.write(response.toString());
                    return;
                }

                if (!isBlocking || blockTimeout <= 0) {
                    System.out.println("Null: blocking");
                    RespParser.writeNullBulkString(outputStream);
                    return;
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime >= endTime) {
                    System.out.println("Null: time 1. current:"+currentTime+" 2.end:"+endTime);
                    RespParser.writeNullBulkString(outputStream);
                    return;
                }

                try {
                    //This ensures that while one thread is in the synchronized block, no other thread can execute any synchronized code on the same object
                    synchronized (this) {
                        System.out.println("Waiting called");
                        this.wait(endTime - currentTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    RespParser.writeNullBulkString(outputStream);
                    return;
                }
            }
        }
    }

    public void isValid(CommandParser.CommandWithArgs commandWithArgs, Storage storage, BufferedWriter outputStream) throws IOException {
        List<String> arguments = commandWithArgs.getArguments();
        int blockIndex = arguments.indexOf("block");
        int streamsIndex = arguments.indexOf("streams");

        long blockTimeout = 0;
        boolean isBlocking = false;

        if (blockIndex != -1 && blockIndex + 1 < arguments.size()) {
            try {
                blockTimeout = Long.parseLong(arguments.get(blockIndex + 1));
                isBlocking = true;
            } catch (NumberFormatException e) {
                outputStream.write("-ERR Invalid block timeout value\r\n");
                outputStream.flush();
                return;
            }
        }

        if (streamsIndex == -1 || streamsIndex + 1 >= arguments.size()) {
            outputStream.write("-ERR Missing or invalid streams argument\r\n");
            outputStream.flush();
            return;
        }

        // Extract stream keys and entry IDs
        List<String> streamKeys = arguments.subList(streamsIndex + 1, streamsIndex + 1 + (arguments.size() - streamsIndex - 1) / 2);
        List<String> entryIds = arguments.subList(streamsIndex + 1 + streamKeys.size(), arguments.size());

        if (streamKeys.size() != entryIds.size()) {
            outputStream.write("-ERR Mismatched number of streams and IDs\r\n");
            outputStream.flush();
            return;
        }

        long endTime = System.currentTimeMillis() + blockTimeout;
        System.out.println("current: "+System.currentTimeMillis());
        System.out.println("endTime: "+endTime);

        while(true){
            boolean hasData = false;
            StringBuilder response = new StringBuilder();
            response.append("*").append(streamKeys.size()).append("\r\n");

            for (int i = 0; i < streamKeys.size(); i++) {
                String streamKey = streamKeys.get(i);
                String entryId = entryIds.get(i);

                StreamCache streamCache = storage.getStreamCache(streamKey);
                if (streamCache == null) {
                    outputStream.write("*2\r\n");
                    outputStream.write("$" + streamKey.length() + "\r\n" + streamKey + "\r\n");
                    outputStream.write("*0\r\n");
                    continue;
                }

                entryId = entryId.contains("-") ? entryId : entryId + "-0";

                // Get entries with IDs greater than the specified entry ID
                TreeMap<String, List<String>> entries = new TreeMap<>(streamCache.getEntries().tailMap(entryId, false));

                if (!entries.isEmpty()) {
                    hasData = true;
                    response.append("*2\r\n")
                            .append("$").append(streamKey.length()).append("\r\n").append(streamKey).append("\r\n")
                            .append("*").append(entries.size()).append("\r\n");

                    for (var entry : entries.entrySet()) {
                        response.append("*2\r\n")
                                .append("$").append(entry.getKey().length()).append("\r\n").append(entry.getKey()).append("\r\n")
                                .append("*").append(entry.getValue().size()).append("\r\n");
                        for (String value : entry.getValue()) {
                            response.append("$").append(value.length()).append("\r\n").append(value).append("\r\n");
                        }
                    }
                } else {
                    response.append("*2\r\n")
                            .append("$").append(streamKey.length()).append("\r\n").append(streamKey).append("\r\n")
                            .append("*0\r\n");
                }
            }

            if (hasData) {
                outputStream.write(response.toString());
                outputStream.flush();
                return;
            }

            if (!isBlocking || blockTimeout <= 0) {
                System.out.println("Null: blocking");
                outputStream.write("$-1\r\n");
                outputStream.flush();
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime >= endTime) {
                System.out.println("Null: time 1. current:"+System.currentTimeMillis()+" 2.end:"+endTime);
                outputStream.write("$-1\r\n");
                outputStream.flush();
                return;
            }

            try {
                //This ensures that while one thread is in the synchronized block, no other thread can execute any synchronized code on the same object
                synchronized (this) {
                    System.out.println("Waiting called");
                    this.wait(endTime - currentTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                outputStream.write("$-1\r\n");
                outputStream.flush();
                return;
            }
        }
    }
}
