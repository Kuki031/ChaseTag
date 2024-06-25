package org.chasetag;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MulticastServerThread {
    private static final int TCP_PORT = Configuration.getInstance().getTCP_PORT();
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static List<ClientHandler> getClients() {
        return clients;
    }

    public void startServer() {
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("Server started on port " + TCP_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
