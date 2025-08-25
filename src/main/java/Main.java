import client.ClientHandler;
import command.CommandHandler;
import command.CommandProcessor;
import data.Storage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    System.out.println("Logs from your program will appear here!");
        Storage storage = new Storage();
        int port = 6379;
        String role = "master";
        String masterAddress = "";
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

        ServerSocket serverSocket;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

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
