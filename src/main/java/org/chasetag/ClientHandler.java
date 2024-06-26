package org.chasetag;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private float x = 0, y = 0;
    private boolean running = true;
    private static String[] roles = {"Hunter", "Fox", "Hunter", "Fox", "Hunter", "Fox"};
    private String role;
    // counteri kolko ima foxova kolko huntera
    // ako je counterFox == 1, svaki sljedeci role samo moze bit hunter
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            int rng = (int) Math.floor(Math.random() * roles.length);
            role = roles[rng];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getPositionData() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putInt(socket.getPort());
        // Convert role to bytes and store in buffer
        byte[] roleBytes = role.getBytes();
        buffer.put(roleBytes, 0, Math.min(roleBytes.length, 8)); // 8 bytes for role
        return buffer.array();
    }

    public String getRole() {
        return role;
    }

    @Override
    public void run() {
        try {
            while (running) {
                String message = in.readUTF();
                if (message.equals("disconnect")) {
                    break;
                }
                String[] parts = message.split(",");
                x = Float.parseFloat(parts[0]);
                y = Float.parseFloat(parts[1]);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            disconnect();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void disconnect() {
        running = false;
        try {
            socket.close();
            MulticastServerThread.removeClient(this); // Notify the server to remove this client
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
