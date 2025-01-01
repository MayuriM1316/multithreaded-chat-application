import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static BufferedReader userInput;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            userInput = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read welcome message from server
            String serverMessage = in.readLine();
            System.out.println(serverMessage);

            // Ask user for their name
            String name = userInput.readLine();
            out.println(name);

            // Start a thread to listen for messages from the server
            new Thread(new ServerListener()).start();

            // Send messages to the server
            String message;
            while (true) {
                message = userInput.readLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(message); // Send message to server
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Listener thread that listens for messages from the server
    private static class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println(serverMessage); // Print messages from server
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
