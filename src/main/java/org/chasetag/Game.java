package org.chasetag;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game {
    private long window;
    private MulticastClient socketHandler;
    private Triangle myTriangle;
    private int textureID;
    private Texture texture = Texture.getInstance();
    private int gameOverTextureID;
    private boolean gameEnded = false;
    private int foxTagCount = 0;

    private void displayGameOverImage() {
        gameOverTextureID = loadTexture("src/main/resources/go.jpg");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.1f, 0.0f, 0.2f, 1.0f);
        texture.rendergo(gameOverTextureID);
        glfwSwapBuffers(window);
    }

    private int loadTexture(String path) {
        int width, height;
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            buffer = STBImage.stbi_load(path, w, h, comp, 4);
            if (buffer == null) {
                throw new RuntimeException("Failed to load a texture file!" + System.lineSeparator() + STBImage.stbi_failure_reason());
            }
            width = w.get();
            height = h.get();
        }

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        STBImage.stbi_image_free(buffer);

        return textureID;
    }

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        try {
            socketHandler = new MulticastClient(Configuration.getInstance().getSERVER_ADDRESS(), Configuration.getInstance().getTCP_PORT(), Configuration.getInstance().getUDP_PORT());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (socketHandler.getRole() == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String myRole = socketHandler.getRole();
        myTriangle = new Triangle(0, 0, myRole);

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(Configuration.getInstance().getWIDTH(), Configuration.getInstance().getHEIGHT(), Configuration.getInstance().getGAME_TITLE(), NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetWindowCloseCallback(window, this::onWindowClose);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        textureID = texture.loadTexture("src/main/resources/background.jpg");
        if (textureID == 0) {
            throw new RuntimeException("Failed to load background texture");
        }
    }


    private void loop() {
        while (!glfwWindowShouldClose(window) && !gameEnded) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glClearColor(0.1f, 0.0f, 0.2f, 1.0f);
            glfwPollEvents();
            myTriangle.processInput(window);
            myTriangle.castSpeedBoost(window);
            myTriangle.wrapAroundEdges();
            myTriangle.checkFuel();
            myTriangle.castIgnoreObstacles(window);
            texture.renderBackground(textureID);
            renderObstacles();
            render();
            socketHandler.sendPosition(myTriangle.getxPos(), myTriangle.getyPos());

            boolean hasCollidedWithFox = checkCollisions(Triangle.possibleRoles[0], Triangle.possibleRoles[1]);
            boolean hasCollidedWithHunter = checkCollisions(Triangle.possibleRoles[1], Triangle.possibleRoles[0]);
            boolean hasCollidedWithObstacle = checkCollision();

            boolean hasCollided = hasCollidedWithFox || hasCollidedWithHunter || hasCollidedWithObstacle;
            myTriangle.setHasCollided(hasCollided);

            if (hasCollidedWithHunter && !myTriangle.isHasAlreadyTagged()) {
                foxTagCount++;
                updateWindowTitlefox();
                myTriangle.setHasAlreadyTagged(true);

                if (foxTagCount >= 2) {
                    System.out.println("Fox has been tagged 5 times!");
                    gameEnded = true;
                }
            } else if (!hasCollidedWithHunter) {
                myTriangle.setHasAlreadyTagged(false);
            }

            myTriangle.stopMovingIfCollided();
            glfwSwapBuffers(window);
        }
        if (gameEnded && myTriangle.getRole().equals(Triangle.possibleRoles[1])) {
            displayGameOverImage();
            glfwPollEvents();
            try {
                Thread.sleep(3000); // Display for 3 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cleanup();
            System.exit(0);
        }
    }

    private void updateWindowTitlefox() {
        String title = String.format("ChaseTag | Fox Tag Count: %d", foxTagCount);
        glfwSetWindowTitle(window, title);
    }

    private void render() {
        Map<Integer, Triangle> players = socketHandler.getPlayers();
        for (Triangle triangle : players.values()) {
            triangle.render();
        }
        myTriangle.render();
    }

    private void renderObstacles() {
        for (Obstacle obstacle : socketHandler.getObstacles()) {
            obstacle.render(myTriangle.getRole());
        }
    }

    private void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        socketHandler.disconnect();
    }

    private void onWindowClose(long window) {
        glfwSetWindowShouldClose(window, true);
    }

    private boolean checkCollisions(String myRole, String oppositeRole) {
        Map<Integer, Triangle> players = socketHandler.getPlayers();
        boolean collided = false;
        if (myTriangle.getRole().equals(myRole)) {
            for (Triangle player : players.values()) {
                if (player.getRole().equals(oppositeRole) && myTriangle.checkCollision(player)) {
                    collided = true;
                    break;
                }
            }
        }
        return collided;
    }

    private boolean checkCollision() {
        boolean collided = false;
        for (Obstacle obstacle : socketHandler.getObstacles()) {
            if (myTriangle.checkCollision(obstacle) && !myTriangle.isIgnoringObstacles()) {
                collided = true;
                break;
            }
        }
        return collided;
    }

    private void updateWindowTitle() {
        String title = String.format("ChaseTag | Score (number of tags): " + myTriangle.getNumberOfTags());
        glfwSetWindowTitle(window, title);
    }

    public static void main(String[] args) {
        new Game().run();
    }
}
