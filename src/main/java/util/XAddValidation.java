package util;

import java.io.BufferedWriter;
import java.io.IOException;

public class XAddValidation {
    public boolean isValid(String entryId, String xaddIdTop, BufferedWriter outputStream) throws IOException {
        String[] xAddId = entryId.split("-");
        int millisecondsTime = Integer.parseInt(xAddId[0]);
        int sequenceNumber = Integer.parseInt(xAddId[1]);
        String[] xAddIdT = xaddIdTop.split("-");
        int millisecondsTimeTop = Integer.parseInt(xAddIdT[0]);
        int sequenceNumberTop = Integer.parseInt(xAddIdT[1]);

        if (millisecondsTime <= 0 && sequenceNumber <= 0) {
            outputStream.write("-ERR The ID specified in XADD must be greater than 0-0\r\n");
            return false;
        } else if (millisecondsTime < millisecondsTimeTop) {
            outputStream.write(
                    "-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n");
            return false;
        } else if (millisecondsTime == millisecondsTimeTop &&
                sequenceNumber <= sequenceNumberTop) {
            outputStream.write(
                    "-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n");
            return false;
        }

        return true;
    }
}
