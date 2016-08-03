package com.dramacow.noccube;

import android.opengl.Matrix;
import android.util.Log;

public class NOCCubeRenderer {

    public final NOCCube noccube;
    public final Cube[] cubes;

    // Animation variables
    public static final int IDLE       = 0;
    public static final int EXPAND     = 1; // \__ used in
    public static final int CONTRACT   = 2; // /   scrambling
    public static final int ROTATING   = 4;
    public static final int SOLVED     = 5;
    public static final int COMPLETE   = 6;

    private int animation_state = EXPAND;

    // Rotation variables
    private int animated_cubes[] = new int[]{};
    private int rotational_axis;
    private float rotational_angle;
    private float direction;
    private float angular_speed = (float) Math.toDegrees(4.0*Math.PI);

    // Seperation variables (and constants)
    private static final float INIT_SEPERATION = 2.125f;
    private static final float INIT_SEP_VELOCITY = 128.0f;
    private static final float SEP_ACCELERATION = -4.0f;
    private float seperation = INIT_SEPERATION;
    private float sep_velocity = INIT_SEP_VELOCITY;
    private float alpha = 1.0f;

    // Normals used in ray casting
    public static float normal[] = {
        0.0f, 0.0f, 1.0f, // front face
        0.0f, 0.0f, -1.0f, // back face
        -1.0f, 0.0f, 0.0f, // left face
        1.0f, 0.0f, 0.0f, // right face
        0.0f, 1.0f, 0.0f, // top face
        0.0f, -1.0f, 0.0f // bottom face
    };

    // This version fills the center of noccube with blank cubes (inefficient)
    public NOCCubeRenderer(final NOCCube noccube, final int blank, final int tex[]) {
        this.noccube = noccube;

        final int d = noccube != null ? noccube.d : 2;
        cubes = new Cube[d*d*d];

        for (int z = 0; z < d; z++) {
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    // Calculate cube index if array was flattened
                    final int i = x + d*y + d*d*z;

                    // Texture splitting coefficient
                    final float frac = 1.0f/d;

                    // Graphic
                    cubes[i] = new Cube(
                        new int[] {
                            z == (d-1) ? tex[0] : blank,
                            z == 0     ? tex[1] : blank,
                            x == 0     ? tex[2] : blank,
                            x == (d-1) ? tex[3] : blank,
                            y == (d-1) ? tex[4] : blank,
                            y == 0     ? tex[5] : blank
                        },
                        new float[][] {
                            z == (d-1) ? Cube.faceTexCoords(frac, x, (d-y-1))       : Cube.fullFaceTexCoords,
                            z == 0     ? Cube.faceTexCoords(frac, (d-x-1), (d-y-1)) : Cube.fullFaceTexCoords,
                            x == 0     ? Cube.faceTexCoords(frac, z, (d-y-1))       : Cube.fullFaceTexCoords,
                            x == (d-1) ? Cube.faceTexCoords(frac, (d-z-1), (d-y-1)) : Cube.fullFaceTexCoords,
                            y == (d-1) ? Cube.faceTexCoords(frac, x, z)             : Cube.fullFaceTexCoords,
                            y == 0     ? Cube.faceTexCoords(frac, x, (d-z-1))       : Cube.fullFaceTexCoords
                        }
                    );

                    // Position matrix
                    final float diff = (d-1.0f)/2.0f;

                    Matrix.setIdentityM(cubes[i].modelM, 0);
                    Matrix.translateM(
                        cubes[i].modelM, 0,
                        2.0f * (x-diff),
                        2.0f * (y-diff),
                        2.0f * (z-diff)
                    );
                }
            }
        }
    }

    public int getAnimationState() {
        return animation_state;
    }

    public void rotate(final int cubeHandles[], final int axis, final boolean clockwise) {
        animation_state = ROTATING;
        animated_cubes = cubeHandles;
        rotational_axis = axis;
        rotational_angle = 0.0f;
        direction = clockwise ? 1.0f : -1.0f;

        Log.d("SAM", "ROTATION TRIGGERED");
    }

    public void draw(final float[] vpMatrix, final int program, final float dt) {
        // Sub-transformations are specified in the reverse order you wish them to occur in:
        // scale <- locale rotate <- translate

        final float rvpMatrix[] = vpMatrix.clone();

        // scale
        final float scale = noccube != null ? 1.0f/(float)noccube.d : 1.0f/2.0f; // Validation check
        Matrix.scaleM(rvpMatrix, 0, scale, scale, scale);

        switch (animation_state) {
            case EXPAND: {
                seperation += 8.0f * dt;

                if (seperation >= INIT_SEPERATION + 1.0f) {
                    seperation = INIT_SEPERATION + 1.0f;
                    animation_state = CONTRACT;

                    noccube.scramble();
                    setCubePosition(seperation);
                }

                setCubePosition(seperation);

                break;
            }

            case CONTRACT: {
                seperation += -8.0f * dt;

                if (seperation <= INIT_SEPERATION) {
                    seperation = INIT_SEPERATION;
                    animation_state = IDLE;
                }

                setCubePosition(seperation);

                break;
            }

            case ROTATING: {
                /*setCubePosition(seperation);
                animation_state = noccube.isSolved() ? SOLVED : IDLE;*/

                float angle = angular_speed * dt;
                rotational_angle += angle;

                if (rotational_angle >= 90.0f) {
                    angle -= rotational_angle - 90.0f;
                    animation_state = noccube.isSolved() ? SOLVED : IDLE;
                    Log.d("SAM", animation_state == SOLVED ? "SOLVED" : "UNSOLVED");
                }

                rotateCubes(animated_cubes, angle, rotational_axis, direction);

                break;
            }

            case SOLVED: {
                seperation += sep_velocity * dt;
                alpha = sep_velocity / INIT_SEP_VELOCITY;
                sep_velocity += SEP_ACCELERATION;

                if (sep_velocity <= 0.0f) {
                    alpha = 0.0f;
                    animation_state = COMPLETE;
                }

                setCubePosition(seperation);

                break;
            }

            case COMPLETE: {
                return;
            }
        }

        for (int i = 0; i < cubes.length; i++) {
            final float mvpMatrix[] = rvpMatrix.clone();

            // translate
            Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix.clone(), 0, cubes[i].modelM, 0);

            cubes[i].draw(mvpMatrix, program, alpha);
        }
    }

    private void rotateCubes(final int[] cubeHandles, final float angle, final int rotational_axis, final float direction) {
        final float tmpMatrix[] = new float[16];

        String message = "";
        for (int h : cubeHandles) {
            message += Integer.toString(h) + ", ";
        }
        Log.d("SAM", message);

        for (int h : cubeHandles) {
            switch (rotational_axis) {
                case NOCCube.AXIS_X:
                    Matrix.setRotateM(tmpMatrix, 0, angle, direction, 0.0f, 0.0f);
                    break;

                case NOCCube.AXIS_Y:
                    Matrix.setRotateM(tmpMatrix, 0, angle, 0.0f, direction, 0.0f);
                    break;

                case NOCCube.AXIS_Z:
                    Matrix.setRotateM(tmpMatrix, 0, angle, 0.0f, 0.0f, direction);
                    break;

                default:
                    Matrix.setIdentityM(tmpMatrix, 0); // Won't do anything in matrix calculation
                    break;
            }

            Matrix.multiplyMM(cubes[h].modelM, 0, tmpMatrix, 0, cubes[h].modelM.clone(), 0);
        }
    }

    private void setCubePosition(final float s) {
        final int d = noccube != null ? noccube.d : 2;

        NOCCube.Cube cube_model[][][] = noccube.cubes;

        for (int z = 0; z < d; z++) {
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    // For setting (0,0) as the centre
                    final float diff = (d - 1.0f) / 2.0f;

                    // Quick alias'
                    final float M[] = cubes[cube_model[z][y][x].displayHandle].modelM;
                    final int R[]   = cube_model[z][y][x].R;

                    M[0]  =           R[0]; M[1]  =           R[1]; M[2]  =           R[2]; M[3]  = 0.0f;
                    M[4]  =           R[3]; M[5]  =           R[4]; M[6]  =           R[5]; M[7]  = 0.0f;
                    M[8]  =           R[6]; M[9]  =           R[7]; M[10] =           R[8]; M[11] = 0.0f;
                    M[12] = s * (x - diff); M[13] = s * (y - diff); M[14] = s * (z - diff); M[15] = 1.0f;

                    /*Log.d("SAM", M[0] + " " + M[1] + " " + M[2] + " " + M[3] + "\n" +
                                 M[4] + " " + M[5] + " " + M[6] + " " + M[7] + "\n" +
                                 M[8] + " " + M[9] + " " + M[10] + " " + M[11] + "\n" +
                                 M[12] + " " + M[13] + " " + M[14] + " " + M[15]);*/
                }
            }
        }
    }
}