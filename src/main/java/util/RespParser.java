package util;

import constant.RESPConstants;
import constant.ResponseConstants;

import java.io.BufferedWriter;
import java.io.IOException;

public class RespParser {
    public static void writeSimpleString(String message, BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.SIMPLE_STRING_PREFIX+message+RESPConstants.CRLF);
    }

    public static void writeErrorString(BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.ERROR_PREFIX+ResponseConstants.ERROR+RESPConstants.CRLF);
    }

    public static void writeBulkString(String message, BufferedWriter outputStream) throws IOException {
        if (message == null) {
            writeNullBulkString(outputStream);
        } else {
            outputStream.write(RESPConstants.BULK_STRING_PREFIX+message.length()+RESPConstants.CRLF+message+RESPConstants.CRLF);
        }
    }

    public static void writeNullBulkString(BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.NULL_BULK_STRING);
    }

    public static void writeIntegerString(int message, BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.INTEGER_PREFIX+message+RESPConstants.CRLF);
    }
}
