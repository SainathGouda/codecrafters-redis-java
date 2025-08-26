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
        completeHandshakeStepOne();
        completeHandshakeStepTwo();
        completeHandshakeStepThree();
        slave.close();
    }

    public synchronized void completeHandshakeStepOne() throws Exception {
//        ArrayList<String> handshakeMessages = new ArrayList<>();
//        handshakeMessages.add(CommandConstants.PING);
//        RespParser.writeArray(handshakeMessages.size(), handshakeMessages, slave.getOutputStream());
        slave.getOutputStream().write("*1\r\n$4\r\nPING\r\n".getBytes());
        slave.getOutputStream().flush();
        String response = reader.readLine();
        System.out.println(response);
        if (!response.equalsIgnoreCase("+PONG\r\n")) {
            throw new Exception("Handshake stage one failed.");
        }
    }

    public synchronized void completeHandshakeStepTwo() throws Exception {
        slave.getOutputStream().write("*3\\r\\n$8\\r\\nREPLCONF\\r\\n$14\\r\\nlistening-port\\r\\n$4\\r\\n6380\\r\\n".getBytes());
        slave.getOutputStream().flush();
        String response = reader.readLine();
        System.out.println(response);
        if (!response.equalsIgnoreCase("+OK\r\n")) {
            throw new Exception("Handshake stage two failed.");
        }
    }

    public synchronized void completeHandshakeStepThree() throws Exception {
        slave.getOutputStream().write("*3\\r\\n$8\\r\\nREPLCONF\\r\\n$4\\r\\ncapa\\r\\n$6\\r\\npsync2\\r\\n".getBytes());
        slave.getOutputStream().flush();
        String response = reader.readLine();
        System.out.println(response);
        if (!response.equalsIgnoreCase("+OK\r\n")) {
            throw new Exception("Handshake stage three failed.");
        }
    }
}
