package com.dramacow.noccube;

import android.opengl.Matrix;
import android.util.Log;

public class NOCCubeRenderer {

    public final NOCCube noccube;
    public final Cube[] cubes;

    // Animation variables
    private boolean animating = false; // Used to disable permuting via input
    private int animated_cubes[] = new int[]{};
    private int rotational_axis;
    private float rotational_angle;
    private float direction;
    private float angular_speed = (float) Math.toDegrees(4.0*Math.PI);

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

        final int d = noccube != null ? noccube.D : 2;
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
                            2.125f * (x-diff),
                            2.125f * (y-diff),
                            2.125f * (z-diff)
                    );
                }
            }
        }
    }

    public boolean isAnimating() {
        return animating;
    }

    public void rotate(final int cubeHandles[], final int axis, final boolean clockwise) {
        animating = true;
        animated_cubes = cubeHandles;
        rotational_axis = axis;
        rotational_angle = 0.0f;
        direction = clockwise ? 1.0f : -1.0f;

        Log.d("SAM", "ANIMATION TRIGGERED");
    }

    public void draw(final float[] vpMatrix, final int program, final float dt) {
        // Sub-transformations are specified in the reverse order you wish them to occur in:
        // scale <- locale rotate <- translate

        final float tmpMatrix[] = new float[16];
        final float rvpMatrix[] = vpMatrix.clone();

        // scale
        final float scale = noccube != null ? 1.0f/(float)noccube.D : 1.0f/2.0f; // Validation check
        Matrix.scaleM(rvpMatrix, 0, scale, scale, scale);

        // locale rotate
        if (animating) {
            float angle = angular_speed * dt;
            rotational_angle += angle;

            if (rotational_angle >= 90.0f) {
                angle -= rotational_angle - 90.0f;
                animating = false;
            }

            for (int cubeHandle : animated_cubes) {
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

                final float modelM[] = cubes[cubeHandle].modelM;
                Matrix.multiplyMM(modelM, 0, tmpMatrix, 0, modelM.clone(), 0);
            }
        }

        for (int i = 0; i < cubes.length; i++) {
            final float mvpMatrix[] = rvpMatrix.clone();

            // translate
            Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix.clone(), 0, cubes[i].modelM, 0);

            cubes[i].draw(mvpMatrix, program);
        }
    }
}