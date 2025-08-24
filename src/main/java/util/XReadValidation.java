package util;

import data.Storage;
import data.StreamCache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class XReadValidation {
    public void isValid(String streamKey, String entryId, Storage storage, BufferedWriter outputStream) throws IOException {
        StreamCache streamCache = storage.getStreamCache(streamKey);

        if (streamCache == null) {
            RespParser.writeArray(0, new ArrayList<>(), outputStream);
            return;
        }

        entryId = entryId.contains("-") ? entryId : entryId + "-0";

        // Get entries with IDs greater than the specified entry ID
        TreeMap<String, List<String>> entries = new TreeMap<>(streamCache.getEntries().tailMap(entryId, false));

        RespParser.writeArrayLength(1, outputStream);
        RespParser.writeArrayLength(2, outputStream); // Stream key and its entries
        RespParser.writeBulkString(streamKey, outputStream);
        RespParser.writeXEntries(outputStream, entries);
    }
}
