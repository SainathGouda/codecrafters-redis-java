package util;

import command.CommandParser;
import constant.ResponseConstants;
import data.Storage;
import data.StreamCache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class XReadValidation {
    public void isValid(CommandParser.CommandWithArgs commandWithArgs, Storage storage, BufferedWriter outputStream) throws IOException {
        List<String> arguments = commandWithArgs.getArguments();
        int streamsIndex = arguments.indexOf("streams");

        if (streamsIndex == -1) {
            RespParser.writeErrorString(ResponseConstants.INVALID_STREAM, outputStream);
            return;
        }

        // Extract stream keys and entry IDs
        List<String> streamKeys = arguments.subList(streamsIndex + 1, streamsIndex + 1 + (arguments.size() - streamsIndex - 1) / 2);
        List<String> entryIds = arguments.subList(streamsIndex + 1 + streamKeys.size(), arguments.size());

        if (streamKeys.size() != entryIds.size()) {
            RespParser.writeErrorString(ResponseConstants.MISMATCH_STREAM, outputStream);
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