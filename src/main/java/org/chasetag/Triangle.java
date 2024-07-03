package org.chasetag;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.GLFW.*;

public class Triangle implements Castable {
    private float xPos;
    private float yPos;
    private String role;
    private static int[] playerKeys = Configuration.getInstance().getPlayerKeys();
    private static float max_window = Configuration.getInstance().getMAX_WINDOW();
    private float xVelocity = 0;
    private float yVelocity = 0;
    private float acceleration = 0.0001f;
    private float maxVelocity = 0.02f;
    private boolean isSpacePressed = false;
    private int countOfUsedSpeedAbility = 0;
    private boolean isMoving = false;
    private float cooldownSeconds = 10.00f;
    private boolean hasCollided;
    private int hasMovedFor = 0;
    private int litersOfFuel = 500;
    private boolean shouldRunOutOfFuel = false;

    public void setHasCollided(boolean hasCollided) {
        this.hasCollided = hasCollided;
    }

    public Triangle(float xPos, float yPos, String role) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.role = role;
    }

    public void setPosition(float x, float y) {
        this.xPos = x;
        this.yPos = y;
    }

    public String setRole(String role) {
        this.role = role;
        return role;
    }

    public float getxPos() {
        return xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public String getRole() {
        return role;
    }

    public void processInput(long window) {
        if (glfwGetKey(window, playerKeys[0]) == GLFW_PRESS && !shouldRunOutOfFuel) {
            yVelocity = Math.min(yVelocity + acceleration, maxVelocity);
            isMoving = true;
        } else if (yVelocity > 0) {
            yVelocity = Math.max(yVelocity - acceleration, 0);
            isMoving = false;
        }

        if (glfwGetKey(window, playerKeys[1]) == GLFW_PRESS && !shouldRunOutOfFuel) {
            yVelocity = Math.max(yVelocity - acceleration, -maxVelocity);
            isMoving = true;
        } else if (yVelocity < 0) {
            yVelocity = Math.min(yVelocity + acceleration, 0);
            isMoving = false;
        }

        if (glfwGetKey(window, playerKeys[2]) == GLFW_PRESS && !shouldRunOutOfFuel) {
            xVelocity = Math.max(xVelocity - acceleration, -maxVelocity);
            isMoving = true;
        } else if (xVelocity < 0) {
            xVelocity = Math.min(xVelocity + acceleration, 0);
            isMoving = false;
        }

        if (glfwGetKey(window, playerKeys[3]) == GLFW_PRESS && !shouldRunOutOfFuel) {
            xVelocity = Math.min(xVelocity + acceleration, maxVelocity);
            isMoving = true;
        } else if (xVelocity > 0) {
            xVelocity = Math.max(xVelocity - acceleration, 0);
            isMoving = false;
        }
        if (isMoving) hasMovedFor++;
        if (hasMovedFor != 0 && shouldRunOutOfFuel) hasMovedFor--;
    }

    public void wrapAroundEdges() {
        if (this.xPos < -max_window) this.xPos = max_window;
        if (this.xPos > max_window) this.xPos = -max_window;
        if (this.yPos < -max_window) this.yPos = max_window;
        if (this.yPos > max_window) this.yPos = -max_window;
    }

    public void render() {
        if (this.role.equals("Hunter")) {
            if (this.hasCollided) {
                GL11.glColor3f(0.0f, 0.5f, 0.0f);
            } else {
                GL11.glColor3f(1.0f, 0.0f, 1.0f);
            }
            if (shouldRunOutOfFuel) {
                GL11.glColor3f(1.0f, 0.0f, 0.0f);
            }
        }
        else if (this.role.equals("Fox")) {
            if (this.hasCollided) {
                GL11.glColor3f(1.0f, 0.0f, 0.0f);
            } else {
                GL11.glColor3f(1.0f, 0.5f, 0.0f);
            }
        }
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(this.xPos, this.yPos + 0.05f);
        GL11.glVertex2f(this.xPos - 0.05f, this.yPos - 0.05f);
        GL11.glVertex2f(this.xPos + 0.05f, this.yPos - 0.05f);
        GL11.glEnd();
    }

    public void checkFuel() {
        if (isMoving && hasMovedFor >= litersOfFuel && this.role.equals("Hunter")) {
            shouldRunOutOfFuel = true;
        } else if (!isMoving && hasMovedFor == 0 && this.role.equals("Hunter")) {
            shouldRunOutOfFuel = false;
        }
    }

    public boolean checkCollision(Triangle hunter) {
        float distanceX = this.xPos - hunter.xPos;
        float distanceY = this.yPos - hunter.yPos;
        float distanceSquared = distanceX * distanceX + distanceY * distanceY;
        float collisionDistance = 0.1f;
        return distanceSquared < (collisionDistance * collisionDistance);
    }

    public void castSpeedBost(long window) {
        if (glfwGetKey(window, playerKeys[4]) == GLFW_PRESS && countOfUsedSpeedAbility != 3 && isMoving) {
            if (!isSpacePressed && this.role.equals("Fox")) {
                acceleration = 0.001f;
                isSpacePressed = true;
                if (isSpacePressed) countOfUsedSpeedAbility++;
            }
        } else if (glfwGetKey(window, playerKeys[4]) == GLFW_RELEASE) {
            isSpacePressed = false;
            acceleration = 0.0001f;
        }
        if (!isSpacePressed) this.setPosition(this.getxPos() + xVelocity, this.getyPos() + yVelocity);
        if (countOfUsedSpeedAbility == 3) {
            cooldownSeconds -= 0.01f;
            if (cooldownSeconds == 0.00f || cooldownSeconds < 0) {
                countOfUsedSpeedAbility = 0;
                cooldownSeconds = 10.00f;
            }
        }
    }
}
