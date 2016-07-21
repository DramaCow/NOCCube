package com.dramacow.noccube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

// Cube and cube instance
class Cube {

    public final float modelM[] = new float[16];

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colourBuffer;
    private final FloatBuffer texCoordBuffer;
    private final ShortBuffer indexBuffer;

    private int mvpMatrixHandle, texUniformHandle;
    private int positionHandle, colourHandle, texCoordHandle;
    private int texDataHandle[];

    public static float vertices[] = {
            // Front face
            -1.0f, -1.0f, 1.0f,// Bottom Left
            1.0f, -1.0f, 1.0f, // Bottom Right
            1.0f, 1.0f, 1.0f,  // Top Right
            -1.0f, 1.0f, 1.0f, // Top left

            // Back face
            1.0f, -1.0f, -1.0f,// Bottom Left
            -1.0f, -1.0f, -1.0f, // Bottom Right
            -1.0f, 1.0f, -1.0f,  // Top Right
            1.0f, 1.0f, -1.0f, // Top left

            // Left face
            -1.0f, -1.0f, -1.0f,// Bottom Left
            -1.0f, -1.0f, 1.0f, // Bottom Right
            -1.0f, 1.0f, 1.0f,  // Top Right
            -1.0f, 1.0f, -1.0f, // Top left

            // Right face
            1.0f, -1.0f, 1.0f,// Bottom Left
            1.0f, -1.0f, -1.0f, // Bottom Right
            1.0f, 1.0f, -1.0f,  // Top Right
            1.0f, 1.0f, 1.0f, // Top left

            // Top face
            -1.0f, 1.0f, 1.0f,// Bottom Left
            1.0f, 1.0f, 1.0f, // Bottom Right
            1.0f, 1.0f, -1.0f,  // Top Right
            -1.0f, 1.0f, -1.0f, // Top left

            // Bottom face
            -1.0f, -1.0f, -1.0f,// Bottom Left
            1.0f, -1.0f, -1.0f, // Bottom Right
            1.0f, -1.0f, 1.0f,  // Top Right
            -1.0f, -1.0f, 1.0f // Top left
    };

    public static float colours[] = {
            // Front face
            0.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Back face
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            // Left face
            0.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            // Top face
            0.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Bottom face
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    public static float[] fullFaceTexCoords = {
            0.0f, 0.0f,     // Bottom Left
            1.0f, 0.0f,     // Bottom Right
            1.0f, -1.0f,     // Top Right
            0.0f, -1.0f,      // Top left
    };

    public static float[] faceTexCoords(final float frac, final int x, final int y) {
        final float[] faceTexCoords = {
                x * frac, (y+1) * frac,
                (x+1) * frac, (y+1) * frac,
                (x+1) * frac, y * frac,
                x * frac, y * frac
        };
        return faceTexCoords;
    }

    public static short indices[] = {
            0,1,2, 0,2,3,       //front
            4,5,6, 4,6,7,       //back

            8,9,10, 8,10,11,    //left
            12,13,14, 12,14,15, //right

            16,17,18, 16,18,19, //top
            20,21,22, 20,22,23  //bottom
    };

    public Cube(final int[] texDataHandle, final float[][] faceTexCoords) {
        // Texture data/offsets
        this.texDataHandle = texDataHandle;

        // Get texture coordinates from individual coordinates for faces
        final float texCoords[] = new float[48];
        for (int i = 0; i < faceTexCoords.length; i++) {
            for (int j = 0; j < faceTexCoords[i].length; j++) {
                texCoords[(8*i)+j] = faceTexCoords[i][j];
            }
        }

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

    public void draw (final float[] mvpMatrix, int program) {
        GLES20.glUseProgram(program);

        // Set program handles
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        texUniformHandle = GLES20.glGetUniformLocation(program, "uTex");
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

        for (int i = 0; i < 6; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texDataHandle[i]);
            indexBuffer.position(6*i); // Move the start of the buffer (6 since 2 triangles per face)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colourHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
}