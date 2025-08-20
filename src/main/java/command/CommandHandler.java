package command;

import constant.RESPConstants;
import constant.ResponseConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler {
    private static ConcurrentHashMap<String, String> cMap = new ConcurrentHashMap<>(); // Thread safe

    public void handlePing(BufferedWriter outputStream) throws IOException {
        outputStream.write(RESPConstants.SIMPLE_STRING_PREFIX+ResponseConstants.PONG+RESPConstants.CRLF);
    }

    public void handleEcho(BufferedWriter outputStream, BufferedReader inputStream) throws IOException {
        inputStream.readLine();
        String message = inputStream.readLine();
        outputStream.write(RESPConstants.BULK_STRING_PREFIX+message.length()+RESPConstants.CRLF+message+RESPConstants.CRLF);
    }

    public void handleSet(BufferedWriter outputStream, BufferedReader inputStream) throws IOException {
        inputStream.readLine();
        String key = inputStream.readLine();
        inputStream.readLine();
        String value = inputStream.readLine();
        cMap.put(key, value);
        outputStream.write(RESPConstants.SIMPLE_STRING_PREFIX+ResponseConstants.OK+RESPConstants.CRLF);
    }

    public void handleGet(BufferedWriter outputStream, BufferedReader inputStream) throws IOException {
        inputStream.readLine();
        String key = inputStream.readLine();
        String value = cMap.get(key);
        if (value != null) {
            outputStream.write(RESPConstants.BULK_STRING_PREFIX+value.length()+RESPConstants.CRLF+value+RESPConstants.CRLF);
        } else {
            outputStream.write(RESPConstants.NULL_BULK_STRING);
        }
    }
}
