// MulticastClient.java
package org.chasetag;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MulticastClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private MulticastSocket udpSocket;
    private InetAddress group;
    private Map<Integer, Triangle> players = new ConcurrentHashMap<>();
    private int localPort;
    private boolean running = true;
    private String role;
    private List<Obstacle> obstacles = new ArrayList<>();
    private int numberOfObstacles = 20;

    public String getRole() {
        return role;
    }

    public MulticastClient(String serverAddress, int tcpPort, int udpPort) throws IOException {
        socket = new Socket(serverAddress, tcpPort);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        group = InetAddress.getByName(Configuration.getInstance().getMULTICAST_GROUP());
        udpSocket = new MulticastSocket(udpPort);
        udpSocket.joinGroup(group);
        localPort = socket.getLocalPort();
        receiveRoleFromServer();
        receiveObstaclesFromServer();

        new Thread(this::receiveUDP).start();
    }

    private void receiveRoleFromServer() throws IOException {
        role = in.readUTF();
    }

    private void receiveObstaclesFromServer() throws IOException {
        for (int i = 0; i < numberOfObstacles; i++) {
            float x = in.readFloat();
            float y = in.readFloat();
            obstacles.add(new Obstacle(x,y));
        }
    }

    public Map<Integer, Triangle> getPlayers() {
        return players;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
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
                    int port = byteBuffer.getInt();
                    players.remove(port);
                } else {
                    while (byteBuffer.remaining() >= 20) {
                        float x = byteBuffer.getFloat();
                        float y = byteBuffer.getFloat();
                        int port = byteBuffer.getInt();
                        byte[] roleBytes = new byte[8];
                        byteBuffer.get(roleBytes);
                        String receivedRole = new String(roleBytes).trim();
                        players.computeIfAbsent(port, k -> new Triangle(0, 0, receivedRole)).setPosition(x, y);
                    }
                }
            } catch (IOException e) {
                System.out.println("UDP socket closed.");
            }
        }
    }
}
