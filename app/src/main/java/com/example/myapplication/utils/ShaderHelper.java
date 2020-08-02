package com.example.myapplication.utils;

import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public class ShaderHelper {
    public static int compileVertexShader(String code) {
        return compileShader(GL_VERTEX_SHADER, code, "vertex");
    }

    public static int compileFragmentShader(String code) {
        return compileShader(GL_FRAGMENT_SHADER, code, "fragment");
    }

    public static int compileShader(int type, String source, String which) {
        // allocate shader
        final int shaderObjectId = glCreateShader(type);

        // put source there
        glShaderSource(shaderObjectId, source);

        // compile shader
        glCompileShader(shaderObjectId);

        final int[] compileStatus = new int[1];

        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.w("Shader", "Shader couldn't compile " + which);
        }
        return shaderObjectId;
    }

    public static int createProgram(int vertexShaderId, int fragmentShaderId) {
        int programObjectId = glCreateProgram();

        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        glLinkProgram(programObjectId);

        return programObjectId;
    }

    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource,
                                   String fragmentShaderSource) {
        int program;

        // Compile the shaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link them into a shader program.
        program = createProgram(vertexShader, fragmentShader);

        validateProgram(program);

        return program;
    }


}
