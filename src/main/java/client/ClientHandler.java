package client;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){
        try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {

            String line;
            while((line = inputStream.readLine()) != null){
                if("ping".equalsIgnoreCase(line)){
                    outputStream.write("+PONG\r\n");
                }
                else if("echo".equalsIgnoreCase(line)){
                    inputStream.readLine();
                    String message = inputStream.readLine();
                    outputStream.write(String.format("$%d\r\n%s\r\n", message.length(), message));
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
