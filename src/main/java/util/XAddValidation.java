package util;

import constant.RESPConstants;
import data.Storage;
import data.StreamCache;

import java.io.BufferedWriter;
import java.io.IOException;

public class XAddValidation {
    public String isValid(String streamKey, String entryId, Storage storage, BufferedWriter outputStream) throws IOException {
        StreamCache streamCache = storage.getStreamCache(streamKey);

        String[] xAddId = entryId.split("-");
        int millisecondsTime = Integer.parseInt(xAddId[0]);
        int sequenceNumber;
        if (xAddId[1].equals("*")) {
            int lastSequenceNumber = streamCache.getLastSequenceNumberForMs(millisecondsTime);

            if (lastSequenceNumber >= 0) {
                sequenceNumber = lastSequenceNumber + 1;
            } else {
                sequenceNumber = (millisecondsTime == 0) ? 1 : 0;
            }
        } else {
            sequenceNumber = Integer.parseInt(xAddId[1]);
        }

        if (millisecondsTime <= 0 && sequenceNumber <= 0) {
            outputStream.write(RESPConstants.ERROR_PREFIX+"ERR The ID specified in XADD must be greater than 0-0"+RESPConstants.CRLF);
            return "";
        }
        if (millisecondsTime < streamCache.getLastMillisecondsTime() || millisecondsTime == streamCache.getLastMillisecondsTime() && sequenceNumber <= streamCache.getLastSequenceNumber()) {
            outputStream.write(RESPConstants.ERROR_PREFIX+"ERR The ID specified in XADD is equal or smaller than the target stream top item"+RESPConstants.CRLF);
            return "";
        }

        entryId = millisecondsTime + "-" + sequenceNumber;
        return entryId;
    }
}
