package util;

import constant.RESPConstants;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class RespParser {
    public static void writeSimpleString(String message, BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.SIMPLE_STRING_PREFIX+message+RESPConstants.CRLF);
    }

    public static void writeErrorString(String error, BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.ERROR_PREFIX+error+RESPConstants.CRLF);
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

    public static void writeArray(int length, List<String> array, OutputStream outputStream) throws IOException {
        outputStream.write((RESPConstants.ARRAY_PREFIX + length + RESPConstants.CRLF).getBytes());
        for (String element : array) {
            outputStream.write((RESPConstants.BULK_STRING_PREFIX+element.length()+RESPConstants.CRLF+element+RESPConstants.CRLF).getBytes());
        }
    }

    public static void writeArrayLength(int length, BufferedWriter outputStream) throws IOException {
        outputStream.write((RESPConstants.ARRAY_PREFIX + length + RESPConstants.CRLF));
    }

    public static void writeIntegerString(int message, BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.INTEGER_PREFIX+message+RESPConstants.CRLF);
    }

    public static void writeXEntries(BufferedWriter outputStream, TreeMap<String, List<String>> entries) throws IOException {
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

    public static void writeRDBFile(byte[] dbFile, BufferedWriter outputStream) throws IOException {
        String myString = new String(dbFile, StandardCharsets.UTF_8);
        System.out.println(dbFile.length);
        System.out.println(myString);
        outputStream.write(RESPConstants.BULK_STRING_PREFIX+dbFile.length+RESPConstants.CRLF);
        outputStream.write(myString);
    }
}
