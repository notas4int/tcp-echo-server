package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoServer {
    private static final Logger log = Logger.getLogger(EchoServer.class.getName());
    private static final int PORT = 53;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log.info("Echo server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("Client connected: " + clientSocket.getRemoteSocketAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not start server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            log.info(Thread.currentThread().getName() + " is started");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    log.info("Received: " + inputLine);
                    out.println(inputLine);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Could not close client socket: " + e.getMessage());
                }
                log.info("Client disconnected");
            }
        }
    }
}
