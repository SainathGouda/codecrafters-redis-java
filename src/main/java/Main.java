import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();
            BufferedReader inputStream = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter outputStream = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()));

            String input;
            while((input = inputStream.readLine()) != null) {
                if ("ping".equalsIgnoreCase(input)) {
                    outputStream.write("+PONG\r\n");
                    // To send the data immediately instead of waiting to be filled
                    outputStream.flush();
                }
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
