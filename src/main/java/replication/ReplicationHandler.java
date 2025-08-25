package replication;

import constant.CommandConstants;
import util.RespParser;

import java.net.Socket;
import java.util.ArrayList;

public class ReplicationHandler {
    public void completeHandshakeStepOne(String masterAddress) throws Exception {
        String[] masterAddressInfo = masterAddress.split(" ");
        String hostAddress = masterAddressInfo[0];
        int portAddress = Integer.parseInt(masterAddressInfo[1]);
        Socket slave = new Socket(hostAddress, portAddress);
        ArrayList<String> handshakeMessages = new ArrayList<>();
        handshakeMessages.add(CommandConstants.PING);
        RespParser.writeArray(handshakeMessages.size(), handshakeMessages, slave.getOutputStream());
        slave.getOutputStream().flush();
        slave.close();
    }

    public void completeHandshakeStepTwo(String masterAddress) throws Exception {
        String[] masterAddressInfo = masterAddress.split(" ");
        String hostAddress = masterAddressInfo[0];
        int portAddress = Integer.parseInt(masterAddressInfo[1]);
        Socket slave = new Socket(hostAddress, portAddress);
        slave.getOutputStream().write("*3\\r\\n$8\\r\\nREPLCONF\\r\\n$14\\r\\nlistening-port\\r\\n$4\\r\\n6380\\r\\n".getBytes());
        slave.getOutputStream().flush();
        slave.close();
    }

    public void completeHandshakeStepThree(String masterAddress) throws Exception {
        String[] masterAddressInfo = masterAddress.split(" ");
        String hostAddress = masterAddressInfo[0];
        int portAddress = Integer.parseInt(masterAddressInfo[1]);
        Socket slave = new Socket(hostAddress, portAddress);
        slave.getOutputStream().write("*3\\r\\n$8\\r\\nREPLCONF\\r\\n$4\\r\\ncapa\\r\\n$6\\r\\npsync2\\r\\n".getBytes());
        slave.getOutputStream().flush();
        slave.close();
    }
}
