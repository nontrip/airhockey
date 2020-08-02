package com.example.myapplication.data;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;

public class Table {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final float[] tableWithVerticies = {
            // X, Y, S, T,
            0.0f, 0.0f, 0.5f, 0.5f
            -0.5f, -0.8f, 0.0f, 0.9f,
            0.5f, -0.8f, 1.0f, 0.9f,
            0.5f, 0.8f, 1.0f, 0.1f,
            -0.5f, 0.8f, 0.0f, 0.1f,
            -0.5f, -0.8f, 0.0f, 0.9f
    };

    VertexArray vertexArray;
    public Table() {
        VertexArray vertexArray = new VertexArray(tableWithVerticies);
    }

    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }

}
