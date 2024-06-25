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

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getPositionData() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putInt(socket.getPort());
        return buffer.array();
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

    public void disconnect() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}