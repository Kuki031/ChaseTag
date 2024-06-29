package org.chasetag;


import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;


public class Texture {
    private float textureOffsetX = 0.0f;
    private float textureOffsetY = 0.0f;
    private final float textureSpeed = 0.0003f;

    private static Texture instance;

    private Texture() {

    }

    public static Texture getInstance() {

        if (instance == null) {
            instance = new Texture();
        }
        return instance;
    }

    public int loadTexture(String path) {
        int width, height;
        ByteBuffer imageBuffer;
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            imageBuffer = STBImage.stbi_load(path, w, h, comp, 4);
            if (imageBuffer == null) {
                throw new RuntimeException("Failed to load texture file!"
                        + System.lineSeparator() + STBImage.stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
            System.out.println("Image loaded: width = " + width + ", height = " + height);
        }

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        STBImage.stbi_image_free(imageBuffer);

        return textureID;
    }

    public void renderBackground(int textureID) {
        textureOffsetX += textureSpeed;
        textureOffsetY += textureSpeed;
        if (textureOffsetX > 1.0f) {
            textureOffsetX -= 1.0f;
        }
        if (textureOffsetY > 1.0f) {
            textureOffsetY -= 1.0f;
        }
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);

        glBegin(GL_QUADS);
        glTexCoord2f(textureOffsetX, textureOffsetY);
        glVertex2f(-1, -1);
        glTexCoord2f(textureOffsetX + 1, textureOffsetY);
        glVertex2f(1, -1);
        glTexCoord2f(textureOffsetX + 1, textureOffsetY + 1);
        glVertex2f(1, 1);
        glTexCoord2f(textureOffsetX, textureOffsetY + 1);
        glVertex2f(-1, 1);
        glEnd();
        glDisable(GL_TEXTURE_2D);
    }
}
