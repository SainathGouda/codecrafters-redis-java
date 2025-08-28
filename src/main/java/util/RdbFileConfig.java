package util;

import constant.ResponseConstants;
import data.Storage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RdbFileConfig {
    Storage storage;

    public RdbFileConfig(Storage storage) throws IOException {
        this.storage = storage;
    }

    public void handleKeysCommand(BufferedWriter outputStream) throws IOException {
        // Get the directory and dbFileName from config
        String dir = storage.getRdbFileConfig("dir");
        String dbFileName = storage.getRdbFileConfig("db_file_name");

        Path dbPath = Path.of(dir, dbFileName);
        File dbfile = new File(dbPath.toString());

        if (!dbfile.exists()) {
            RespParser.writeErrorString(ResponseConstants.NO_SUCH_FILE, outputStream);
            return;
        }

        try (InputStream fileInputStream = new FileInputStream(dbfile)) {
            int read;
            while ((read = fileInputStream.read()) != -1) {
                if (read == 0xFB) { // Start of database section
                    getLen(fileInputStream); // Skip hash table size info
                    getLen(fileInputStream); // Skip expires size info
                    break;
                }

                int type = fileInputStream.read(); // Read the type (should be a valid type byte)
                int keyLen = getLen(fileInputStream); // Get the key length
                byte[] keyBytes = new byte[keyLen];
                fileInputStream.read(keyBytes); // Read the key bytes
                String parsedKey = new String(keyBytes, StandardCharsets.UTF_8);

                List<String> keys = new ArrayList<>();
                keys.add(parsedKey);

                // Respond with the key in the format expected by Redis
                RespParser.writeArray(keys.size(), keys, outputStream);
            }
        } catch (IOException e) {
            RespParser.writeErrorString(ResponseConstants.CANNOT_READ_DB_FILE, outputStream);
        }
    }

    public void loadDbFile() throws IOException {
        // Get the directory and dbFileName from config
        String dir = storage.getRdbFileConfig("dir");
        String dbFileName = storage.getRdbFileConfig("db_file_name");

        Path dbPath = Path.of(dir, dbFileName);
        File dbfile = new File(dbPath.toString());

        try (InputStream fileInputStream = new FileInputStream(dbfile)) {
            int read;
            while ((read = fileInputStream.read()) != -1) {
                if (read == 0xFB) { // Start of database section
                    getLen(fileInputStream); // Skip hash table size info
                    getLen(fileInputStream); // Skip expires size info
                    break;
                }
            }

            while ((read = fileInputStream.read()) != -1) {
                int type = read; // Read the type (should be a valid type byte)
                if (type == 0xFF) {
                    break;
                }

                int keyLen = getLen(fileInputStream); // Get the key length
                byte[] keyBytes = new byte[keyLen];
                fileInputStream.read(keyBytes); // Read the key bytes
                String parsedKey = new String(keyBytes, StandardCharsets.UTF_8);

                int valueLen = getLen(fileInputStream);
                byte[] valueBytes = new byte[valueLen];
                fileInputStream.read(valueBytes);
                String parsedValue = new String(valueBytes, StandardCharsets.UTF_8);

                long ttl = -1;
                storage.setData(parsedKey, parsedValue, ttl);
            }
        } catch (IOException _) {

        }
    }

    private int getLen(InputStream fileInputStream) throws IOException {
        int read = fileInputStream.read();
        int len_encoding_bit = (read & 0b11000000) >> 6;
        int len = 0;

        if (len_encoding_bit == 0) {
            len = read & 0b00111111;
        } else if (len_encoding_bit == 1) {
            int extra_len = fileInputStream.read();
            len = ((read & 0b00111111) << 8) + extra_len;
        } else if (len_encoding_bit == 2) {
            byte[] extra_len = new byte[4];
            fileInputStream.read(extra_len);
            len = ByteBuffer.wrap(extra_len).getInt();
        }
        return len;
    }
}
