package org.chasetag;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MulticastServerThread {
    private static final int TCP_PORT = Configuration.getInstance().getTCP_PORT();
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static int numberOfFoxes = 0;
    private static int numberOfHunters = 0;

    public static int getNumberOfHunters() {
        return numberOfHunters;
    }

    public static int getNumberOfFoxes() {
        return numberOfFoxes;
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }

    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        if (clientHandler.getRole().equals("Hunter")) numberOfHunters--;
        else if (clientHandler.getRole().equals("Fox")) numberOfFoxes--;
        notifyClientDisconnection(clientHandler);
    }

    private static void notifyClientDisconnection(ClientHandler clientHandler) {
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(clientHandler.getSocket().getPort());
            byte[] data = buffer.array();
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    InetAddress.getByName(Configuration.getInstance().getMULTICAST_SERVER()),
                    Configuration.getInstance().getUDP_PORT());
            udpSocket.send(packet);
            udpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("Server started on port " + TCP_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                if (clientHandler.getRole().equals("Hunter")) numberOfHunters++;
                else if (clientHandler.getRole().equals(("Fox"))) numberOfFoxes++;

                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
