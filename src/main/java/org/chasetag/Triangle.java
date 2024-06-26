package org.chasetag;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;

public class Triangle {
    private float xPos;
    private float yPos;
    private String role;
    private static int[] playerKeys = Configuration.getInstance().getPlayerKeys();
    private static float max_window = Configuration.getInstance().getMAX_WINDOW();
    private float xVelocity = 0;
    private float yVelocity = 0;
    private float acceleration = 0.0001f;
    private float maxVelocity = 0.02f;

    public Triangle(float xPos, float yPos, String role) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.role = role;
    }

    public void setPosition(float x, float y) {
        this.xPos = x;
        this.yPos = y;
    }

    public float getxPos() {
        return xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public void processInput(long window) {
        if (glfwGetKey(window, playerKeys[0]) == GLFW_PRESS) {
            // Check if yVelocity + acceleration < maxVelocity => cap velocity at maxVelocity
            yVelocity = Math.min(yVelocity + acceleration, maxVelocity);
        } else if (yVelocity > 0) {
            // If yVelocity is > 0, decelerate until you reach 0 (cap velocity at 0)
            yVelocity = Math.max(yVelocity - acceleration, 0);
        }

        if (glfwGetKey(window, playerKeys[1]) == GLFW_PRESS) {
            yVelocity = Math.max(yVelocity - acceleration, -maxVelocity);
        } else if (yVelocity < 0) {
            yVelocity = Math.min(yVelocity + acceleration, 0);
        }

        if (glfwGetKey(window, playerKeys[2]) == GLFW_PRESS) {
            xVelocity = Math.max(xVelocity - acceleration, -maxVelocity);
        } else if (xVelocity < 0) {
            xVelocity = Math.min(xVelocity + acceleration, 0);
        }

        if (glfwGetKey(window, playerKeys[3]) == GLFW_PRESS) {
            xVelocity = Math.min(xVelocity + acceleration, maxVelocity);
        } else if (xVelocity > 0) {
            xVelocity = Math.max(xVelocity - acceleration, 0);
        }

        this.setPosition(this.getxPos() + xVelocity, this.getyPos() + yVelocity);
    }

    public void wrapAroundEdges() {
        if (this.xPos < -max_window) this.xPos = max_window;
        if (this.xPos > max_window) this.xPos = -max_window;
        if (this.yPos < -max_window) this.yPos = max_window;
        if (this.yPos > max_window) this.yPos = -max_window;
    }

    public void render() {
        if ("Hunter".equals(role)) {
            GL11.glColor3f(0.0f, 1.0f, 0.0f); // Green for Hunter
        } else if ("Fox".equals(role)) {
            GL11.glColor3f(1.0f, 0.5f, 0.0f); // Orange for Fox
        }

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(this.xPos, this.yPos + 0.05f);
        GL11.glVertex2f(this.xPos - 0.05f, this.yPos - 0.05f);
        GL11.glVertex2f(this.xPos + 0.05f, this.yPos - 0.05f);
        GL11.glEnd();
    }
}
