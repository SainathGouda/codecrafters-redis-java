package util;

import command.CommandParser;
import constant.RESPConstants;
import constant.ResponseConstants;
import data.Storage;
import data.StreamCache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class XReadValidation {
    public void isValid(CommandParser.CommandWithArgs commandWithArgs, Storage storage, BufferedWriter outputStream) throws IOException {
        List<String> arguments = commandWithArgs.getArguments();
        int blockIndex = arguments.indexOf("block");
        int streamsIndex = arguments.indexOf("streams");

        long blockTimeoutMillis = 0;

        if (blockIndex != -1 && blockIndex + 1 < arguments.size()) {
            try {
                blockTimeoutMillis = Long.parseLong(arguments.get(blockIndex + 1));
            } catch (NumberFormatException e) {
                outputStream.write("-ERR Invalid block timeout value\r\n");
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

        Map<String, TreeMap<String, List<String>>> result = new HashMap<>();

        long startTime = System.currentTimeMillis();
        while (true) {
            boolean dataFound = false;
            result.clear();

            for (int i = 0; i < streamKeys.size(); i++) {
                String streamKey = streamKeys.get(i);
                String entryId = entryIds.get(i);
                StreamCache streamCache = storage.getStreamCache(streamKey);

                if (streamCache == null) continue;

                entryId = entryId.contains("-") ? entryId : entryId + "-0";
                TreeMap<String, List<String>> entries = new TreeMap<>(streamCache.getEntries().tailMap(entryId, false));

                if (!entries.isEmpty()) {
                    result.put(streamKey, entries);
                    dataFound = true;
                }
            }

            if (dataFound || blockTimeoutMillis == 0) {
                break;
            }

            long timeElapsed = System.currentTimeMillis() - startTime;
            long timeRemaining = blockTimeoutMillis - timeElapsed;

            if (timeRemaining <= 0) {
                break;
            }

            synchronized (this) {
                try {
                    this.wait(timeRemaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (result.isEmpty()) {
            RespParser.writeNullBulkString(outputStream);
        } else {
            RespParser.writeArrayLength(result.size(), outputStream);
            for (Map.Entry<String, TreeMap<String, List<String>>> entry : result.entrySet()) {
                RespParser.writeArrayLength(2, outputStream);
                RespParser.writeBulkString(entry.getKey(), outputStream);
                RespParser.writeXEntries(outputStream, entry.getValue());
            }
        }
    }

    public void isValidWithoutBlock(CommandParser.CommandWithArgs commandWithArgs, Storage storage, BufferedWriter outputStream) throws IOException {
        List<String> arguments = commandWithArgs.getArguments();
        int streamsIndex = arguments.indexOf("streams");

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

        // Start constructing the RESP response
        RespParser.writeArrayLength(streamKeys.size(), outputStream);

        for (int i = 0; i < streamKeys.size(); i++) {
            String streamKey = streamKeys.get(i);
            String entryId = entryIds.get(i);
            StreamCache streamCache = storage.getStreamCache(streamKey);

            if (streamCache == null) {
                RespParser.writeArrayLength(2, outputStream);
                RespParser.writeBulkString(streamKey, outputStream);
                RespParser.writeArray(0, new ArrayList<>(), outputStream);
                return;
            }

            entryId = entryId.contains("-") ? entryId : entryId + "-0";

            // Get entries with IDs greater than the specified entry ID
            TreeMap<String, List<String>> entries = new TreeMap<>(streamCache.getEntries().tailMap(entryId, false));

            RespParser.writeArrayLength(2, outputStream); // Stream key and its entries
            RespParser.writeBulkString(streamKey, outputStream);
            RespParser.writeXEntries(outputStream, entries);
        }
    }
}