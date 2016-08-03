package com.dramacow.noccube;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Quad {
    public final float modelM[] = new float[16];

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colourBuffer;
    private final FloatBuffer texCoordBuffer;
    private final ShortBuffer indexBuffer;

    private int mvpMatrixHandle, texUniformHandle, alphaUniformHandle;
    private int positionHandle, colourHandle, texCoordHandle;
    private int texDataHandle;

    public static float vertices[] = {
        // Front face
        -1.0f, -1.0f, 0.0f,// Bottom Left
        1.0f, -1.0f, 0.0f, // Bottom Right
        1.0f, 1.0f, 0.0f,  // Top Right
        -1.0f, 1.0f, 0.0f, // Top left
    };

    public static float colours[] = {
        // Front face
        0.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 1.0f, 1.0f,
    };

    public static float[] texCoords = {
        0.0f, 0.0f,     // Bottom Left
        1.0f, 0.0f,     // Bottom Right
        1.0f, -1.0f,     // Top Right
        0.0f, -1.0f,      // Top left
    };

    public static short indices[] = {
        0,1,2, 0,2,3,       //front
    };

    public Quad(final int texDataHandle) {
        // Texture data/offsets
        this.texDataHandle = texDataHandle;

        // Buffers
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(colours.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        colourBuffer = byteBuf.asFloatBuffer();
        colourBuffer.put(colours);
        colourBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(texCoords.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        texCoordBuffer = byteBuf.asFloatBuffer();
        texCoordBuffer.put(texCoords);
        texCoordBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(indices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuf.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        // Model matrix
        Matrix.setIdentityM(modelM, 0);
    }

    public void draw(final float[] mvpMatrix, final int program, final float alpha) {
        //GLES20.glUseProgram(program); // TODO: initialise at program compilation

        // Set program handles
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        texUniformHandle = GLES20.glGetUniformLocation(program, "uTex");
        alphaUniformHandle = GLES20.glGetUniformLocation(program, "uAlpha");
        // ---
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        colourHandle = GLES20.glGetAttribLocation(program, "aColour");
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");

        GLES20.glEnableVertexAttribArray(positionHandle);   // Enable a handle to the triangle vertices
        GLES20.glVertexAttribPointer(                       // Prepare the triangle coordinate data
            positionHandle, 3,
            GLES20.GL_FLOAT, false,
            12, vertexBuffer
        );

        GLES20.glEnableVertexAttribArray(colourHandle);
        GLES20.glVertexAttribPointer(
            colourHandle, 4,
            GLES20.GL_FLOAT, false,
            16, colourBuffer
        );

        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2,
            GLES20.GL_FLOAT, false,
            8, texCoordBuffer
        );

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glUniform1f(alphaUniformHandle, alpha);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texDataHandle);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colourHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
}
