package com.example.myapplication;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.myapplication.utils.ShaderHelper;
import com.example.myapplication.utils.TextResourceReader;
import com.example.myapplication.utils.matrixHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glHint;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static javax.microedition.khronos.opengles.GL10.GL_GEQUAL;
import static javax.microedition.khronos.opengles.GL10.GL_LINE_SMOOTH;
import static javax.microedition.khronos.opengles.GL10.GL_TRIANGLE_FAN;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



public class RendererClass implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer vertexData;
    private final Context context;

    private int program;
    private int aColorLocation;
    private int aPositionLocation;
    private int uPointSizeLocation;
    private int uMatrixLocation;
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] modelProjectionMatrix = new float[16];
    private final float[] temp = new float[16];

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    public RendererClass(Context context) {
        this.context = context;
        float[] tableWithVerticies = {
                // fan
                0f, 0f, 1f, 1f, 1f,
                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                // line
                -0.5f, 0f, 1f, 0f, 0f,
                0.5f, 0f, 1f, 0f, 0f,
                // mallets
                0f, -0.4f, 0f, 0f, 1f,
                0f, 0.4f, 1f, 0f, 0f,
                // border top
                -0.5f, 0.8f, 0f, 0f, 0f,
                0.5f, 0.8f, 0f, 0f, 0f,
                // border left
                -0.5f, -0.8f, 0f, 0f, 0f,
                -0.5f, 0.8f, 0f, 0f, 0f,
                // border right
                0.5f, -0.8f, 0f, 0f, 0f,
                0.5f, 0.8f, 0f, 0f, 0f,
                // border bottom
                -0.5f, -0.8f, 0f, 0f, 0f,
                0.5f, -0.8f, 0f, 0f, 0f,
                // pack
                0f, 0f, 0f, 0f, 0f
        };
        vertexData = ByteBuffer.allocateDirect(tableWithVerticies.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(tableWithVerticies);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(1.0f, 0f, 1.0f, 0.0f);
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(this.context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(this.context, R.raw.simple_fragment_shader);
//        Log.w("TAG", "Vetex " + vertexShaderSource);
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.createProgram(vertexShader, fragmentShader);

        ShaderHelper.validateProgram(program);

        glUseProgram(program);

        aColorLocation = glGetAttribLocation(program, "a_Color");
        aPositionLocation = glGetAttribLocation(program, "a_Position");
        uPointSizeLocation = glGetUniformLocation(program, "u_PointSize");
        uMatrixLocation = glGetUniformLocation(program, "u_Matrix");
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);

        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aColorLocation);


    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);
//        final float aspectRatio = width > height ?
//                (float)width / (float)height :
//                (float)height / (float)width;
//
//        if (width > height) {
//            // landscape
//            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
//        } else {
//            // portrait
//            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
//        }
//        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        matrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -2f);
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, modelProjectionMatrix, 0, temp.length);
        glUniformMatrix4fv(uMatrixLocation, 1, false, modelProjectionMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        glClear(GL_COLOR_BUFFER_BIT);

        rotateM(modelMatrix, 0, 0.1f, 0f, 0f, 1f);

        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, modelProjectionMatrix, 0, temp.length);
        glUniformMatrix4fv(uMatrixLocation, 1, false, modelProjectionMatrix, 0);

        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        glDrawArrays(GL_LINES, 6, 2);

        glUniform1f(uPointSizeLocation, 20.0f);
        glDrawArrays(GL_POINTS, 8, 1);

        glDrawArrays(GL_POINTS, 9, 1);

        glLineWidth(20.0f);
        glEnable(GL_LINE_SMOOTH);

        glDrawArrays(GL_LINES, 10, 8);


        glUniform1f(uPointSizeLocation, 10.0f);
        glDrawArrays(GL_POINTS, 18, 1);

    }

}
