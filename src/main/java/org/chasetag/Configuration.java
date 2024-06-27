package org.chasetag;

import static org.lwjgl.glfw.GLFW.*;

public class Configuration {
    private static Configuration instance;

    private Configuration() {

    }

    public static Configuration getInstance() {

        if (instance == null) {
            instance = new Configuration();
        }

        return instance;
    }

    private final int TCP_PORT = 4445;
    private final int UDP_PORT = 4446;
    private final String SERVER_ADDRESS = "localhost";
    private final String MULTICAST_SERVER = "255.255.255.255";
    private final String MULTICAST_GROUP = "230.0.0.1";
    private final int WIDTH = 1024;
    private final int HEIGHT = 720;
    private final float MAX_WINDOW = 1.0f;
    private final String GAME_TITLE = "ChaseTag";
    private final int[] playerKeys = {GLFW_KEY_UP, GLFW_KEY_DOWN, GLFW_KEY_LEFT, GLFW_KEY_RIGHT};


    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public float getMAX_WINDOW() {
        return MAX_WINDOW;
    }

    public int[] getPlayerKeys() {
        return playerKeys;
    }

    public String getGAME_TITLE() {
        return GAME_TITLE;
    }

    public int getUDP_PORT() {
        return UDP_PORT;
    }

    public int getTCP_PORT() {
        return TCP_PORT;
    }

    public String getSERVER_ADDRESS() {
        return SERVER_ADDRESS;
    }
    public String getMULTICAST_SERVER() {
        return MULTICAST_SERVER;
    }

    public String getMULTICAST_GROUP() {
        return MULTICAST_GROUP;
    }
}