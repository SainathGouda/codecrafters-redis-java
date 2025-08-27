package replication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ReplicationHandler {
    Socket slave;
    InputStream inputStream;
    BufferedReader reader;

    public ReplicationHandler(String masterAddress) throws IOException {
        String[] masterAddressInfo = masterAddress.split(" ");
        String hostAddress = masterAddressInfo[0];
        int portAddress = Integer.parseInt(masterAddressInfo[1]);
        this.slave = new Socket(hostAddress, portAddress);
        this.inputStream = slave.getInputStream();
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public void completeHandShake() throws Exception {
        completeFirstHandshakeStepOne();
        completeSecondHandshakeStepOne();
        completeSecondHandshakeStepTwo();
        completeThirdHandshake();
        slave.close();
    }

    public synchronized void completeFirstHandshakeStepOne() throws Exception {
        slave.getOutputStream().write("*1\r\n$4\r\nPING\r\n".getBytes());
        slave.getOutputStream().flush();
        String response = reader.readLine();
        if (response==null || !response.equalsIgnoreCase("+PONG")) {
            throw new Exception("First handshake failed.");
        }
    }

    public synchronized void completeSecondHandshakeStepOne() throws Exception {
        slave.getOutputStream().write("*3\r\n$8\r\nREPLCONF\r\n$14\r\nlistening-port\r\n$4\r\n6380\r\n".getBytes());
        slave.getOutputStream().flush();
        String response = reader.readLine();
        if (response==null || !response.equalsIgnoreCase("+OK")) {
            throw new Exception("Second handshake stage one failed.");
        }
    }

    public synchronized void completeSecondHandshakeStepTwo() throws Exception {
        slave.getOutputStream().write("*3\r\n$8\r\nREPLCONF\r\n$4\r\ncapa\r\n$6\r\npsync2\r\n".getBytes());
        slave.getOutputStream().flush();
        String response = reader.readLine();
        if (response==null || !response.equalsIgnoreCase("+OK")) {
            throw new Exception("Second handshake stage one failed.");
        }
    }

    public synchronized void completeThirdHandshake() throws Exception {
        slave.getOutputStream().write("*3\r\n$5\r\nPSYNC\r\n$1\r\n?\r\n$2\r\n-1\r\n".getBytes());
        slave.getOutputStream().flush();
        String response = reader.readLine();
        int dbFileLength = Integer.parseInt(reader.readLine().replace('$', '0'));
        reader.skip(dbFileLength);
        while (true) {
            System.out.println(reader.readLine());
        }
//        if (response==null || !response.startsWith("+FULLRESYNC")) {
//            throw new Exception("Third handshake failed.");
//        }
    }
}
