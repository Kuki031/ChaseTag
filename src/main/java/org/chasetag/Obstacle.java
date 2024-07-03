package org.chasetag;

import org.lwjgl.opengl.GL11;

public class Obstacle {
    private float xPos;
    private float yPos;

    public Obstacle() {
        do {
            xPos = (float) (Math.random() * 2 - 1);
            yPos = (float) (Math.random() * 2 - 1);
        } while (Math.abs(xPos - 0.0f) <= 0.5f && Math.abs(yPos - 0.0f) <= 0.5f);
    }

    public Obstacle(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public float getxPos() {
        return xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public void render(String role) {
        if (role.equals(Triangle.possibleRoles[0])) {
            GL11.glColor3f(0.30f, 0.30f, 1.00f);
        } else {
            GL11.glColor3f(0.6f, 0.8f, 0.196078f);
        }
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(this.xPos, this.yPos + 0.05f);
        GL11.glVertex2f(this.xPos - 0.05f, this.yPos - 0.05f);
        GL11.glVertex2f(this.xPos + 0.05f, this.yPos - 0.05f);
        GL11.glEnd();
    }
}
