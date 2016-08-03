package com.dramacow.noccube;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRendererEx implements GLSurfaceView.Renderer {

    private Context context;
    private float time;

    public NOCCube noccube;
    public NOCCubeRenderer noccubeRenderer;
    public Cube displayCube;

    private Button btnBegin;

    // Global rotation variables
    private float latitude, longitude;
    private float eyeX, eyeY, eyeZ;
    private float radius = 10.0f;

    // Local rotation variables
    public int face0; // initial face
    public float point0[]; // Initial touch point

    // Matrices
    private final float viewMatrix[]           // Matrix represents where the "camera" is/is facing
            = new float[16];
    private final float projectionMatrix[]     // Matrix used to project eye coords to clip coords
            = new float[16];
    private final float vpMatrix[]             // viewM * projectionM
            = new float[16];

    private final float guiMatrix[]
            = new float[16];

    // Shader constants/variables
    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;"                 +
        "attribute vec4 aPosition;"                +
        "attribute vec4 aColour;"                  +
        "varying vec4 vColour;"                    +
        "attribute vec2 aTexCoord;"                +
        "varying vec2 vTexCoord;"                  +
        "void main() {"                            +
        "   vColour = aColour;"                    +
        "   vTexCoord = aTexCoord;"                +
        "   gl_Position = uMVPMatrix * aPosition;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;"                      +
        "varying vec4 vColour;"                         +
        "uniform sampler2D uTex;"                       +
        "varying vec2 vTexCoord;"                       +
        "uniform float uAlpha;"                         +
        "void main() {"                                 +
        "   gl_FragColor = texture2D(uTex, vTexCoord);" +
        "   gl_FragColor.a *= uAlpha;"                  +
        "}";

    private int program;

    public GLRendererEx(final Context context) {
        this.context = context;

        // NOTE: Do NOT put any OpenGL code in this constructor
        // since the context has not yet been made.
        // Instead, use onSurfaceCreated
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set clock
        time = SystemClock.uptimeMillis() / 1000.0f;

        // Set view matrix
        setViewMatrix(latitude, longitude);

        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Necessary for maintaining correct alpha
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Enable blending (for alpha)
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Set up shader
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);

        GLES20.glUseProgram(program);

        // Load resources
        final int inside = loadTexture(context, R.drawable.inside);
        final int textures[] = {
            loadTexture(context, R.drawable.blue),
            loadTexture(context, R.drawable.green),
            loadTexture(context, R.drawable.red),
            loadTexture(context, R.drawable.orange),
            loadTexture(context, R.drawable.yellow),
            loadTexture(context, R.drawable.purple)
        };
        final int begin = loadTexture(context, R.drawable.begin);

        // Prevent restarting the Noccube when re-initialising the screen (e.g after putting the screen to sleep)
        if (noccube == null) {
            noccube = new NOCCube(4);
            noccubeRenderer = new NOCCubeRenderer(noccube, inside, textures);
        }

        // GUI setup
        // =========

        // All important display cube
        displayCube = new Cube(
            new int[] {
                textures[0], textures[1], textures[2],
                textures[3], textures[4], textures[5],
            },
            new float[][] {
                Cube.fullFaceTexCoords, Cube.fullFaceTexCoords, Cube.fullFaceTexCoords,
                Cube.fullFaceTexCoords, Cube.fullFaceTexCoords, Cube.fullFaceTexCoords
            }
        );

        Matrix.setIdentityM(guiMatrix, 0);
        btnBegin = new Button(begin) {
            @Override
            public void onClick() {
                Log.d("SAM", "CLICKED");
            }
        };
    }

    public void onDrawFrame(GL10 unused) {
        // Get delta time
        float dt = SystemClock.uptimeMillis() / 1000.0f - time;
        time = SystemClock.uptimeMillis() / 1000.0f;

        // Redraw background colour
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // View-Projection
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        //noccubeRenderer.draw(vpMatrix, program, dt);
        displayCube.draw(vpMatrix, program, 1.0f);
        btnBegin.draw(guiMatrix, program);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // For taking into account the non-square aspect ratio of the device's display
        float ratio = (float) width / height;

        // Set projection matrix, to be used on eye co-ordinates on frame draw
        Matrix.frustumM(projectionMatrix, 0, ratio * -1, ratio * 1, -1, 1, 3, 32);

        // ----------------

        /*float tmpMatrix[] = new float[16];
        Matrix.orthoM(guiMatrix, 0, ratio * -1, ratio * 1, -1, 1, 3, 32);
        Matrix.setLookAtM(tmpMatrix, 0, eyeX, eyeY, eyeZ, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(guiMatrix, 0, guiMatrix.clone(), 0, tmpMatrix, 0);*/

        btnBegin.set(0.0f, -0.625f, 0.625f, 0.1f);
    }

    // TEMP TEST FUNCTION
    public void permute(final int axis, final int slice,  final boolean clockwise) {
        if (noccubeRenderer.getAnimationState() == NOCCubeRenderer.IDLE && noccubeRenderer.noccube !=  null) {
            final int cubeHandles[] = noccube.rotate(axis, slice, clockwise);
            if (cubeHandles == null || cubeHandles.length == 0) return;
            noccubeRenderer.rotate(cubeHandles, axis, clockwise);
        }
    }

    public void adjustViewAngle(float latAdj, float longAdj) {
        latitude += latAdj; longitude += longAdj;
        setViewMatrix(latitude, longitude);
    }

    public void setViewMatrix(float latitude, float longitude) {
        // Sphere rotation (eye co-ordinates)
        eyeX = (float) (-radius * Math.cos(latitude) * Math.sin(longitude)); // Since equation considers east positive, we must negate
        eyeY = (float) (radius * Math.sin(latitude));
        eyeZ = (float) (radius * Math.cos(latitude) * Math.cos(longitude));

        final float upX = 0.0f; // Does not change state as we are not rotating around the z axis
        final float upY = (float) Math.signum(Math.cos(latitude)); // d/dy (sin(y)) [1 | -1 | 0]
        final float upZ = upY == 0.0f ? (float) Math.signum(-Math.sin(latitude)) : 0.0f; // d/dt (cos(y)) [cannot be 0 if upY is 0]

        // View                          |---EYE--------|  |---CENTER-----|  |--UP VECTOR-|
        // NOTE: vectors eye and up must be sufficiently perpendicular (i.e. not parallel), else viewMatrix is undefined
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, 0.0f, 0.0f, 0.0f, upX, upY, upZ);
    }

    public void update(final float x, final float y) {
        //Log.d("SAM", "TOUCHED: " + x + ", " + y);

        // ADD CUBE CONTROL HERE

        btnBegin.update(x, y, true); /*TODO: CHANGE*/ btnBegin.update(0,0,false);
    }

    public int castRay(final float point[], final float x, final float y) {
        final float tmpMatrix[] = new float[16];

        // Homogeneous clip co-ordinates
        final float ray[] = new float[] { x, y, -1.0f, 1.0f };

        // Eye co-ordinates
        Matrix.invertM(tmpMatrix, 0, projectionMatrix, 0);
        Matrix.multiplyMV(ray, 0, tmpMatrix, 0, ray.clone(), 0);
        ray[2] = -1.0f; ray[3] = 0.0f; // forwards facing, not a point

        // World co-ordinates
        Matrix.invertM(tmpMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMV(ray, 0, tmpMatrix, 0, ray.clone(), 0);
        final float D[] = new float[] { ray[0], ray[1], ray[2] }; // xyz

        // Normalise (any point to this?)
        final float mag = (float) Math.sqrt( D[0]*D[0] + D[1]*D[1] + D[2]*D[2] );
        D[0] /= mag; D[1] /= mag; D[2] /= mag;

        /*
        Log.d("SAM", "RAY: " + Float.toString(ray[0]) + ", " + Float.toString(ray[1]) + ", " + Float.toString(ray[2]));
        Log.d("SAM", "Dsam :" + D[0] + ", " + D[1] + ", " + D[2]);
        Log.d("SAM", "EYE: " + Float.toString(eyeX) + ", " + Float.toString(eyeY) + ", " + Float.toString(eyeZ));
        */

        float distance = Float.POSITIVE_INFINITY;
        int face = -1;

        for (int i = 0; i < 6; i++) {
            final float n[] = { NOCCubeRenderer.normal[3*i], NOCCubeRenderer.normal[3*i+1], NOCCubeRenderer.normal[3*i+2] };
            final float d = (float) Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);

            final float denom = (D[0]*n[0] + D[1]*n[1] + D[2]*n[2]);
            if (denom == 0.0f) continue;

            float t = -( (eyeX*n[0] + eyeY*n[1] + eyeZ*n[2]) - d ) / denom;

            final float p[] = { //point
                eyeX + t*D[0],
                eyeY + t*D[1],
                eyeZ + t*D[2],
            };

            if (!((-1.0f <= p[0] && p[0] <= 1.0f) && (-1.0f <= p[1] && p[1] <= 1.0f) && (-1.0f <= p[2] && p[2] <= 1.0f))) {
                t = Float.POSITIVE_INFINITY;
            }

            if (t < distance) {
                distance = t;
                face = i;
            }
        }

        point[0] = eyeX + distance*D[0];
        point[1] = eyeY + distance*D[1];
        point[2] = eyeZ + distance*D[2];

        //Log.d("SAM", "POINT: " + point[0] + ", " + point[1] + ", " + point[2]);

        return face;
    }

    // Loader/setup functions
    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static int loadTexture(final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // No pre-scaling

            // Read in resource (decode into a format understood by Android)
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL (Subsequent GL calls will refer to this texture)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering (Nearest picks closest texel)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}