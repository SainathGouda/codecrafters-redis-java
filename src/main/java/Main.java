import client.ClientHandler;
import command.CommandHandler;
import command.CommandProcessor;
import constant.CommandConstants;
import data.Storage;
import util.RespParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
  public static void main(String[] args){
    System.out.println("Logs from your program will appear here!");
        Storage storage = new Storage();
        int port = 6379;
        String role = "master";
        String masterAddress = "";
        String masterReplId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
        String masterReplOffset = "0";
        for(int i = 0; i < args.length; i++){
            if(args[i].equals("--port")){ port = Integer.parseInt(args[i+1]); }
            if (args[i].equals("--replicaof")){
                role = "slave";
                masterAddress = args[i+1];
            }
        }
        storage.setPort(port);
        storage.setRole(role);
        storage.setMasterAddress(masterAddress);
        storage.setMasterReplId(masterReplId);
        storage.setMasterReplOffset(masterReplOffset);

        ServerSocket serverSocket;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            if (!masterAddress.isEmpty()) {
                String[] masterAddressInfo = masterAddress.split(" ");
                String hostAddress = masterAddressInfo[0];
                int portAddress = Integer.parseInt(masterAddressInfo[1]);
                Socket slave = new Socket(hostAddress, portAddress);
//                slave.getOutputStream().write("*1\r\n$4\r\nPING\r\n".getBytes());
                ArrayList<String> handshakeMessages = new ArrayList<>();
                handshakeMessages.add(CommandConstants.PING);
                RespParser.writeArray(handshakeMessages.size(), handshakeMessages, slave.getOutputStream());
                slave.getOutputStream().flush();
                slave.close();
            }

            while (true) {
                // Wait for connection from client.
                clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, new CommandProcessor(new CommandHandler(storage), storage))).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
  }
}
