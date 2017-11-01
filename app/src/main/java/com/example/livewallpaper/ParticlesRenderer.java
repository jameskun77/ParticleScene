package com.example.livewallpaper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView.Renderer;
import android.support.v4.content.ContextCompat;

import com.example.livewallpaper.objects.Heightmap;
import com.example.livewallpaper.objects.ParticleShooter;
import com.example.livewallpaper.objects.ParticleSystem;
import com.example.livewallpaper.objects.Skybox;
import com.example.livewallpaper.programs.HeightmapShaderProgram;
import com.example.livewallpaper.programs.ParticleShaderProgram;
import com.example.livewallpaper.programs.SkyboxShaderProgram;
import com.example.livewallpaper.util.Geometry.Point;
import com.example.livewallpaper.util.Geometry.Vector;
import com.example.livewallpaper.util.MatrixHelper;
import com.example.livewallpaper.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BITS;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Jameskun on 2017/10/26.
 */

public class ParticlesRenderer implements Renderer {
    private final Context context;

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewMatrixForSkybox = new float[16];
    private final float[] projectionMatrix = new float[16];

    private final float[] tempMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private HeightmapShaderProgram heightmapProgram;
    private Heightmap heightmap;

    private SkyboxShaderProgram skyboxProgram;
    private Skybox skybox;

    private ParticleShaderProgram particleProgram;
    private ParticleSystem particleSystem;
    private ParticleShooter redParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter blueParticleShooter;

    private long globalStartTime;
    private int particletexture;
    private int skyboxTexture;

    private float xRotation, yRotation;

    public ParticlesRenderer(Context context) {
        this.context = context;
    }

    public void handleTouchDrag(float deltaX, float deltaY) {
        xRotation += deltaX / 16f;
        yRotation += deltaY / 16f;

        if (yRotation < -90) {
            yRotation = -90;
        } else if (yRotation > 90) {
            yRotation = 90;
        }

        // Setup view matrix
        updateViewMatrices();
    }

    private void updateViewMatrices() {
        setIdentityM(viewMatrix, 0);
        rotateM(viewMatrix, 0, -yRotation, 1f, 0f, 0f);
        rotateM(viewMatrix, 0, -xRotation, 0f, 1f, 0f);
        System.arraycopy(viewMatrix, 0, viewMatrixForSkybox, 0, viewMatrix.length);

        translateM(viewMatrix, 0, 0, -1.5f, -5f);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 1.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        heightmapProgram = new HeightmapShaderProgram(context);
        heightmap = new Heightmap(((BitmapDrawable) ContextCompat
                .getDrawable(context,R.drawable.heightmap)).getBitmap());

        skyboxProgram = new SkyboxShaderProgram(context);
        skybox = new Skybox();

        particleProgram = new ParticleShaderProgram(context);
        particleSystem = new ParticleSystem(10000);
        globalStartTime = System.nanoTime();

        final Vector particleDirection = new Vector(0f, 0.5f, 0f);
        final float angleVarianceInDegrees = 5f;
        final float speedVariance = 1f;


        redParticleShooter = new ParticleShooter(
                new Point(-1f, 0f, 0f),
                particleDirection,
                Color.rgb(255, 50, 5),
                angleVarianceInDegrees,
                speedVariance);

        greenParticleShooter = new ParticleShooter(
                new Point(0f, 0f, 0f),
                particleDirection,
                Color.rgb(25, 255, 25),
                angleVarianceInDegrees,
                speedVariance);

        blueParticleShooter = new ParticleShooter(
                new Point(1f, 0f, 0f),
                particleDirection,
                Color.rgb(5, 50, 255),
                angleVarianceInDegrees,
                speedVariance);


        particletexture = TextureHelper.loadTexture(context, R.drawable.particle_texture);

        skyboxTexture = TextureHelper.loadCubeMap(context,
                new int[] { R.drawable.left, R.drawable.right,
                        R.drawable.bottom, R.drawable.top,
                        R.drawable.front, R.drawable.back});
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 10f);

        updateViewMatrices();
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        drawHeightmap();
        drawSkybox();
        drawParticles();
    }

    private void drawHeightmap() {
        setIdentityM(modelMatrix, 0);

        scaleM(modelMatrix, 0, 100f, 10f, 100f);
        updateMvpMatrix();
        heightmapProgram.useProgram();
        heightmapProgram.setUniforms(modelViewProjectionMatrix);
        heightmap.bindData(heightmapProgram);
        heightmap.draw();
    }

    private void drawSkybox() {
        setIdentityM(modelMatrix, 0);
        updateMvpMatrixForSkybox();

        glDepthFunc(GL_LEQUAL);
        skyboxProgram.useProgram();
        skyboxProgram.setUniforms(modelViewProjectionMatrix, skyboxTexture);
        skybox.bindData(skyboxProgram);
        skybox.draw();
        glDepthFunc(GL_LESS);
    }

    private void drawParticles() {
        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;

        redParticleShooter.addParticles(particleSystem, currentTime, 1);
        greenParticleShooter.addParticles(particleSystem, currentTime, 1);
        blueParticleShooter.addParticles(particleSystem, currentTime, 1);

        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();

        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        particleProgram.useProgram();
        particleProgram.setUniforms(modelViewProjectionMatrix, currentTime, particletexture);
        particleSystem.bindData(particleProgram);
        particleSystem.draw();

        glDisable(GL_BLEND);
        glDepthMask(true);
    }

    private void updateMvpMatrix() {
        multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }
    private void updateMvpMatrixForSkybox() {
        multiplyMM(tempMatrix, 0, viewMatrixForSkybox, 0, modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
    }
}
