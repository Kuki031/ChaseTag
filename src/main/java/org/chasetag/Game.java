package org.chasetag;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {
    private long window;
    private MulticastClient socketHandler;
    private Triangle myTriangle;

    public static void main(String[] args) {
        new Game().run();
    }

    public void run() {
        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        try {
            socketHandler = new MulticastClient(Configuration.getInstance().getSERVER_ADDRESS(), Configuration.getInstance().getTCP_PORT(), Configuration.getInstance().getUDP_PORT());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        myTriangle = new Triangle(0, 0);

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(Configuration.getInstance().getWIDTH(), Configuration.getInstance().getHEIGHT(), Configuration.getInstance().getGAME_TITLE(), NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            myTriangle.processInput(window);
            render();
            socketHandler.sendPosition(myTriangle.getxPos(), myTriangle.getyPos());
            glfwSwapBuffers(window);
        }
    }


    private void render() {
        glClearColor(0.1f, 0.0f, 0.2f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        Map<Integer, Triangle> players = socketHandler.getPlayers();
        for (Triangle triangle : players.values()) {
            triangle.render();
        }

        myTriangle.render();
    }
}
