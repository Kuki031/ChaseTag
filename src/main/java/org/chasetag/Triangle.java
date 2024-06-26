package org.chasetag;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public class Triangle {
    private float xPos;
    private float yPos;
    private static int[] playerKeys = Configuration.getInstance().getPlayerKeys();
    private static float velocity = Configuration.getInstance().getVELOCITY();
    private static float max_window = Configuration.getInstance().getMAX_WINDOW();

    public Triangle(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
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
        if (glfwGetKey(window, playerKeys[0]) == GLFW_PRESS) this.setPosition(this.getxPos(), this.getyPos() + velocity);
        if (glfwGetKey(window, playerKeys[1]) == GLFW_PRESS) this.setPosition(this.getxPos(), this.getyPos() -velocity);
        if (glfwGetKey(window, playerKeys[2]) == GLFW_PRESS) this.setPosition(this.getxPos() -velocity, this.getyPos());
        if (glfwGetKey(window, playerKeys[3]) == GLFW_PRESS) this.setPosition(this.getxPos() + velocity, this.getyPos());
    }

    public void wrapAroundEdges() {
        if (this.xPos < -max_window) this.xPos = max_window;
        if (this.xPos > max_window) this.xPos = -max_window;
        if (this.yPos > max_window) this.yPos = -max_window;
        if (this.yPos < -max_window) this.yPos = max_window;
    }

    public void render() {
        GL11.glPushMatrix();
        GL11.glTranslatef(xPos, yPos, 0);
        GL11.glColor3f(0.5f, 1, 1); //boja shipa
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(-0.05f, -0.05f);
        GL11.glVertex2f(0.05f, -0.05f);
        GL11.glVertex2f(0, 0.05f);
        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
