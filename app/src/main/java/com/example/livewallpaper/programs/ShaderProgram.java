package com.example.livewallpaper.programs;

import android.content.Context;

import com.example.livewallpaper.util.ShaderHelper;
import com.example.livewallpaper.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Jameskun on 2017/10/26.
 */

public class ShaderProgram {
    //uniform
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_COLOR = "u_Color";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_TIME = "u_Time";

    //attribute
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    protected static final String A_DIRECTION_VECTOR = "a_DirectionVector";
    protected static final String A_PARTICLE_START_TIME = "a_ParticleStartTime";

    protected final int program;
    protected ShaderProgram(Context context, String vertexShaderPath, String fragmentShaderPath){
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context,vertexShaderPath),
                TextResourceReader.readTextFileFromResource(context,fragmentShaderPath));
    }

    public void useProgram(){
        glUseProgram(program);
    }
}
