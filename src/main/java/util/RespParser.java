package util;

import constant.RESPConstants;
import constant.ResponseConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public class RespParser {
    public static void writeSimpleString(String message, BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.SIMPLE_STRING_PREFIX+message+RESPConstants.CRLF);
    }

    public static void writeErrorString(BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.ERROR_PREFIX+ResponseConstants.ERROR+RESPConstants.CRLF);
    }

    public static void writeNumberErrorString(BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.ERROR_PREFIX+ResponseConstants.NUMBER_FORMAT_EXCEPTION+RESPConstants.CRLF);
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

    public static void writeArray(int length, List<String> array, BufferedWriter outputStream) throws IOException {
        writeArrayLength(length, outputStream);
        for (String element : array) {
            outputStream.write(RESPConstants.BULK_STRING_PREFIX+element.length()+RESPConstants.CRLF+element+RESPConstants.CRLF);
        }
    }

    public static void writeArrayLength(int length, BufferedWriter outputStream) throws IOException {
        outputStream.write((RESPConstants.ARRAY_PREFIX + length + RESPConstants.CRLF));
    }

    public static void writeIntegerString(int message, BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.INTEGER_PREFIX+message+RESPConstants.CRLF);
    }

    static void writeXEntries(BufferedWriter outputStream, TreeMap<String, List<String>> entries) throws IOException {
        outputStream.write(RESPConstants.ARRAY_PREFIX+ entries.size()+RESPConstants.CRLF);

        for (var entry : entries.entrySet()) {
            writeArrayLength(2, outputStream);
            writeBulkString(entry.getKey(), outputStream);
            writeArrayLength(entry.getValue().size(), outputStream);
            for (String value : entry.getValue()) {
                writeBulkString(value, outputStream);
            }
        }
    }
}
