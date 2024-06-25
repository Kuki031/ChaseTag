package org.chasetag;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class MulticastServer {
    private static final int UDP_PORT = Configuration.getInstance().getUDP_PORT();
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        MulticastServer server = new MulticastServer();
        server.start();
    }

    public void start() {
        MulticastServerThread serverSocketHandler = new MulticastServerThread();

        new Thread(this::startUdpBroadcast).start();

        serverSocketHandler.startServer();
    }

    private void startUdpBroadcast() {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            while (true) {
                List<ClientHandler> clients = MulticastServerThread.getClients();
                byte[] buffer = new byte[BUFFER_SIZE];
                int pos = 0;
                for (ClientHandler client : clients) {
                    System.arraycopy(client.getPositionData(), 0, buffer, pos, 12);
                    pos += 12;
                }
                DatagramPacket packet = new DatagramPacket(buffer, pos, InetAddress.getByName(Configuration.getInstance().getMULTICAST_SERVER()), UDP_PORT);
                udpSocket.send(packet);
                Thread.sleep(16);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
