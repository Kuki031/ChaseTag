package org.chasetag;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MulticastClient {
    private Socket socket;
    private DataOutputStream out;
    private MulticastSocket udpSocket;
    private InetAddress group;
    private Map<Integer, Triangle> players = new ConcurrentHashMap<>();
    private int localPort;
    private boolean running = true;

    public MulticastClient(String serverAddress, int tcpPort, int udpPort) throws IOException {
        socket = new Socket(serverAddress, tcpPort);
        out = new DataOutputStream(socket.getOutputStream());

        group = InetAddress.getByName(Configuration.getInstance().getMULTICAST_GROUP());
        udpSocket = new MulticastSocket(udpPort);
        udpSocket.joinGroup(group);
        localPort = socket.getLocalPort();

        new Thread(this::receiveUDP).start();
    }

    public Map<Integer, Triangle> getPlayers() {
        return players;
    }

    public void sendPosition(float x, float y) {
        try {
            out.writeUTF(x + "," + y);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        running = false;
        try {
            out.writeUTF("disconnect");
            socket.close();
            udpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveUDP() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running) {
            try {
                udpSocket.receive(packet);
                ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
                if (byteBuffer.remaining() == 4) {
                    // This is a disconnection packet
                    int port = byteBuffer.getInt();
                    players.remove(port);
                } else {
                    // This is a position update packet
                    while (byteBuffer.remaining() >= 20) {
                        float x = byteBuffer.getFloat();
                        float y = byteBuffer.getFloat();
                        int port = byteBuffer.getInt();
                        byte[] roleBytes = new byte[8];
                        byteBuffer.get(roleBytes);
                        String role = new String(roleBytes).trim();
                        players.computeIfAbsent(port, k -> new Triangle(0, 0, role)).setPosition(x, y);
                    }
                }
            } catch (IOException e) {
                System.out.println("UDP socket closed.");
            }
        }
    }
}
