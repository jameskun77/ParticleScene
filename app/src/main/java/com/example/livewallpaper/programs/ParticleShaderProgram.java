package com.example.livewallpaper.programs;

import android.content.Context;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Jameskun on 2017/10/26.
 */

public class ParticleShaderProgram extends ShaderProgram {
    // Uniform
    private final int uMatrixLocation;
    private final int uTimeLocation;

    // Attribute
    private final int aPositionLocation;
    private final int aColorLocation;
    private final int aDirectionVectorLocation;
    private final int aParticleStartTimeLocation;
    private final int uTextureUnitLocation;

    public ParticleShaderProgram(Context context){
        super(context,"particle_vertex_shader.glsl","particle_fragment_shader.glsl");

        uMatrixLocation = glGetUniformLocation(program,U_MATRIX);
        uTimeLocation = glGetUniformLocation(program,U_TIME);
        uTextureUnitLocation  = glGetUniformLocation(program,U_TEXTURE_UNIT);

        aPositionLocation = glGetAttribLocation(program,A_POSITION);
        aColorLocation = glGetAttribLocation(program,A_COLOR);
        aDirectionVectorLocation = glGetAttribLocation(program,A_DIRECTION_VECTOR);
        aParticleStartTimeLocation = glGetAttribLocation(program,A_PARTICLE_START_TIME);
    }

    public void setUniforms(float[] matrix, float elapsedTime, int textureId) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glUniform1f(uTimeLocation, elapsedTime);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }
    public int getColorAttributeLocation() {
        return aColorLocation;
    }
    public int getDirectionVectorAttributeLocation() {
        return aDirectionVectorLocation;
    }
    public int getParticleStartTimeAttributeLocation() {
        return aParticleStartTimeLocation;
    }
}
