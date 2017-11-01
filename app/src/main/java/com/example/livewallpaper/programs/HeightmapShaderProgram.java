package com.example.livewallpaper.programs;

import android.content.Context;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static com.example.livewallpaper.programs.ShaderProgram.A_POSITION;
import static com.example.livewallpaper.programs.ShaderProgram.U_MATRIX;

/**
 * Created by Jameskun on 2017/11/1.
 */

public class HeightmapShaderProgram extends ShaderProgram {
    private final int uMatrixLocation;
    private final int aPositionLocation;

    public HeightmapShaderProgram(Context context) {
        super(context,"heightmap_vertex_shader.glsl","heightmap_fragment_shader.glsl");

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
    }

    public void setUniforms(float[] matrix) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }
}
