package client;

import command.CommandProcessor;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private final CommandProcessor  commandProcessor;
    public ClientHandler(Socket clientSocket,  CommandProcessor commandProcessor) {
        this.clientSocket = clientSocket;
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void run(){
        try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {

            String line;
            while((line = inputStream.readLine()) != null){
                String command = line;
                if (!command.isEmpty()){
                    commandProcessor.processCommand(command, outputStream, inputStream);
                }
                //To send the data immediately instead of waiting to be filled
                outputStream.flush();
            }
        } catch (IOException e) {
            System.out.println("Error while handling client: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error while closing client: " + e.getMessage());
            }
        }
    }
}
