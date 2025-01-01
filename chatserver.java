import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final ExecutorService pool = Executors.newFixedThreadPool(10); // Thread pool for handling clients

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients to connect...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a new thread for each client connection
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                pool.submit(clientHandler); // Submit client handler to the thread pool
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all clients
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendMessage(message);
            }
        }
    }

    // Remove client handler when client disconnects
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    // Client Handler class to handle communication with each client
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                // Set up input and output streams
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Ask for client's name
                out.println("Enter your name:");
                clientName = in.readLine();
                out.println("Welcome, " + clientName + "! You can start chatting now.");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(clientName + ": " + message);
                    ChatServer.broadcast(clientName + ": " + message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    ChatServer.removeClient(this);
                    clientSocket.close();
                    System.out.println(clientName + " has disconnected.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Send a message to the client
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}



