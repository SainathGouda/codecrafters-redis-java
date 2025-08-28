package util;

import data.Storage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class KeysValidation {
    InputStream inputStream;
    BufferedWriter outputStream;
    Storage storage;

    public KeysValidation(Storage storage, BufferedWriter outputStream) throws IOException {
        this.storage = storage;
        this.inputStream = storage.getClientSocket().getInputStream();
        this.outputStream = outputStream;
    }

    public void handleKeysCommand() throws IOException {
        // Get the directory and dbfilename from config
        String dir = storage.getRdbFileConfig("dir");
        String dbfilename = storage.getRdbFileConfig("dbfilename");

        Path dbPath = Path.of(dir, dbfilename);
        File dbfile = new File(dbPath.toString());

        if (!dbfile.exists()) {
            outputStream.write("-ERR no such file\r\n");
            return;
        }

        try (InputStream inputStream = new FileInputStream(dbfile)) {
            int read;
            while ((read = inputStream.read()) != -1) {
                if (read == 0xFB) { // Start of database section
                    getLen(); // Skip hash table size info
                    getLen(); // Skip expires size info
                    break;
                }
            }

            int type = inputStream.read(); // Read the type (should be a valid type byte)
            int len = getLen(); // Get the key length
            byte[] key_bytes = new byte[len];
            inputStream.read(key_bytes); // Read the key bytes
            String parsed_key = new String(key_bytes, StandardCharsets.UTF_8);

            // Respond with the key in the format expected by Redis
            outputStream.write("*1\r\n$" + parsed_key.length() + "\r\n" + parsed_key + "\r\n");
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error reading RDB file: " + e.getMessage());
            outputStream.write("-ERR error reading database file\r\n");
        }
    }

    private int getLen() throws IOException {
        int read = inputStream.read();
        int len_encoding_bit = (read & 0b11000000) >> 6;
        int len = 0;

        if (len_encoding_bit == 0) {
            len = read & 0b00111111;
        } else if (len_encoding_bit == 1) {
            int extra_len = inputStream.read();
            len = ((read & 0b00111111) << 8) + extra_len;
        } else if (len_encoding_bit == 2) {
            byte[] extra_len = new byte[4];
            inputStream.read(extra_len);
            len = ByteBuffer.wrap(extra_len).getInt();
        }
        return len;
    }
}
