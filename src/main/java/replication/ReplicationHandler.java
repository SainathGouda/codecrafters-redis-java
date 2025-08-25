package replication;

import constant.CommandConstants;
import util.RespParser;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ReplicationHandler {
    Socket slave;

    public ReplicationHandler(String masterAddress) throws IOException {
        String[] masterAddressInfo = masterAddress.split(" ");
        String hostAddress = masterAddressInfo[0];
        int portAddress = Integer.parseInt(masterAddressInfo[1]);
        this.slave = new Socket(hostAddress, portAddress);
    }

    public void completeHandShake() throws Exception {
        synchronized (this) {
            completeHandshakeStepOne();
            completeHandshakeStepTwo();
            completeHandshakeStepThree();
        }
        slave.close();
    }

    public void completeHandshakeStepOne() throws Exception {
//        ArrayList<String> handshakeMessages = new ArrayList<>();
//        handshakeMessages.add(CommandConstants.PING);
//        RespParser.writeArray(handshakeMessages.size(), handshakeMessages, slave.getOutputStream());
        slave.getOutputStream().write("*1\r\n$4\r\nPING\r\n".getBytes());
        slave.getOutputStream().flush();
    }

    public void completeHandshakeStepTwo() throws Exception {
        slave.getOutputStream().write("*3\\r\\n$8\\r\\nREPLCONF\\r\\n$14\\r\\nlistening-port\\r\\n$4\\r\\n6380\\r\\n".getBytes());
        slave.getOutputStream().flush();
    }

    public void completeHandshakeStepThree() throws Exception {
        slave.getOutputStream().write("*3\\r\\n$8\\r\\nREPLCONF\\r\\n$4\\r\\ncapa\\r\\n$6\\r\\npsync2\\r\\n".getBytes());
        slave.getOutputStream().flush();
    }
}
