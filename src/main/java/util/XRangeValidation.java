package util;

import data.Storage;
import data.StreamCache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class XRangeValidation {
    public void isValid(String streamKey, String startId, String endId, Storage storage, BufferedWriter outputStream) throws IOException {
        StreamCache streamCache = storage.getStreamCache(streamKey);

        if (streamCache == null) {
            RespParser.writeArray(0, new ArrayList<>(), outputStream);
            return;
        }

        TreeMap<String, List<String>> entries;
        startId = startId.contains("-") ? startId : startId + "-0";
        if (endId.equals("+")) {
            entries = new TreeMap<>(streamCache.getEntries().tailMap(startId, true));
        } else {
            endId = endId.contains("-") ? endId : endId + "-*";

            if (endId.endsWith("-*")) {
                long endTimeMs = Long.parseLong(endId.split("-")[0]);
                int lastSequenceNumber = streamCache.getLastSequenceNumberForMs(endTimeMs);
                endId = endTimeMs + "-" + lastSequenceNumber;
            }

            entries = new TreeMap<>(streamCache.getEntries().subMap(startId, true, endId, true)); // boolean for including end boundaries
        }

        outputStream.write("*" + entries.size() + "\r\n");
        for (var entry : entries.entrySet()) {
            outputStream.write("*2\r\n$" + entry.getKey().length() + "\r\n" + entry.getKey() + "\r\n");
            outputStream.write("*" + entry.getValue().size() + "\r\n");
            for (String value : entry.getValue()) {
                outputStream.write("$" + value.length() + "\r\n" + value + "\r\n");
            }
        }
    }
}
