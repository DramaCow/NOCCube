package com.dramacow.noccube;

import android.util.Log;

import java.util.Arrays;
import java.util.Random;

public class NOCCube {

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;

    public final int d; // Cube dimensions

    private boolean solved = false;
    public NOCCube.Cube cubes[][][]; // z, y, x TODO: should not be public

    public NOCCube(final int d) {
        this.d = d > 1 ? d : 2;

        cubes = new NOCCube.Cube[d][d][d];
        for (int z = 0; z < d; z++) {
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    cubes[z][y][x] = new NOCCube.Cube(x + d*y + d*d*z);
                }
            }
        }
    }

    public void scramble() {
        Random rand = new Random();
        for (int i = 0; i < 1 /*d*d*d*/; i++) {
            this.rotate(
                rand.nextInt(3),   // axis
                rand.nextInt(d),   // slice
                rand.nextBoolean() // direction (clockwise)
            );
        }
    }

    public int[] rotate(final int axis, final int slice, final boolean clockwise) {
        // Validate rotation input
        if (axis < 0 || axis >= 3 || slice < 0 || slice >= d) return new int[]{};

        Log.d("SAM",
            (axis == AXIS_X ? "X " : axis == AXIS_Y ? "Y " : "Z ") +
            (Integer.toString(slice) + " ") +
            (clockwise ? "clockwise" : "anti-clockwise")
        );

        final int cubeHandles[] = new int[d*d]; // TODO: make this a private member

        // deep copy
        Cube new_cubes[][][] = new Cube[d][d][d];
        for (int z = 0; z < d; z++) {
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    new_cubes[z][y][x] = new NOCCube.Cube(cubes[z][y][x]);
                }
            }
        }

        switch (axis) {
            // x-axis
            case AXIS_X: {
                for (int z = 0; z < d; z++) {
                    for (int y = 0; y < d; y++) {
                        cubeHandles[y + d*z] = cubes[z][y][slice].displayHandle;

                        if (!clockwise) new_cubes[z][y][slice] = new NOCCube.Cube(cubes[y][(d - 1) - z][slice]);
                        else            new_cubes[z][y][slice] = new NOCCube.Cube(cubes[(d - 1) - y][z][slice]);

                        new_cubes[z][y][slice].rotateX(clockwise);
                    }
                }
                break;
            }

            // y-axis
            case AXIS_Y: {
                for (int z = 0; z < d; z++) {
                    for (int x = 0; x < d; x++) {
                        cubeHandles[x + d*z] = cubes[z][slice][x].displayHandle;

                        if (!clockwise) new_cubes[z][slice][x] = new NOCCube.Cube(cubes[(d - 1) - x][slice][z]);
                        else            new_cubes[z][slice][x] = new NOCCube.Cube(cubes[x][slice][(d - 1) - z]);

                        new_cubes[z][slice][x].rotateY(clockwise);
                    }
                }
                break;
            }

            // z-axis
            case AXIS_Z: {
                for (int y = 0; y < d; y++) {
                    for (int x = 0; x < d; x++) {
                        cubeHandles[x + d*y] = cubes[slice][y][x].displayHandle;

                        if (!clockwise) new_cubes[slice][y][x] = new NOCCube.Cube(cubes[slice][x][(d - 1) - y]);
                        else            new_cubes[slice][y][x] = new NOCCube.Cube(cubes[slice][(d - 1) - x][y]);

                        new_cubes[slice][y][x].rotateZ(clockwise);
                    }
                }
                break;
            }
        }

        cubes = new_cubes;
        solved = checkSolved();
        return cubeHandles;
    }

    public boolean isSolved() {
        return solved;
    }

    private boolean checkSolved() {
        // Maintain a reference to previously checked rotation matrix
        int R[] = cubes[0][0][0].R;

        // NOTE: the success condition is NOT determined by the absolute position of each cube.
        //       Instead, success is achieved when all cubes are oriented in the same direction,
        //       irrespective of whether this direction is the same as the starting direction.
        //       This means success can occur when the whole cube is oriented in any direction,
        //       so long as the relative position and orientation of the individual cubes form the globe.
        //       Achieved by comparing the rotation matrices of each individual cube.

        for (int z = 0; z < d; z++) {
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    // NOTE: an unnecessary comparison between the first cube's matrix
                    //       to itself is made. Though not by any means a big performance
                    //       issue, it can easily be avoided by adjusting the logic.
                    //       For the time being, I cannot be bothered.

                    if (!Arrays.equals(R, cubes[z][y][x].R)) {
                        return false;
                    }

                    R = cubes[z][y][x].R;
                }
            }
        }

        return true;
    }

    public class Cube {
        public int R[]; // 3x3 rotation matrix
        final int displayHandle;

        public Cube(final int displayHandle) {
            this.displayHandle = displayHandle;
            this.R = new int[] {1,0,0,  // [ 0 1 2 ]
                                0,1,0,  // [ 3 4 5 ]
                                0,0,1}; // [ 6 7 8 ]
        }

        public Cube(final Cube that) {
            this.displayHandle = that.displayHandle;
            this.R = new int[] {that.R[0], that.R[1], that.R[2],
                                 that.R[3], that.R[4], that.R[5],
                                 that.R[6], that.R[7], that.R[8]};
        }

        // =========================================================
        // ==| NOTE: rows of the array are columns of the matrix |==
        // =========================================================

        /* x clockwise = [ 1  0  0 ] , x anti-clockwise = [ 1  0  0 ]
                         [ 0  0 -1 ]                      [ 0  0  1 ]
                         [ 0  1  0 ]                      [ 0 -1  0 ] */
        private void rotateX(boolean clockwise) {
            int c = clockwise ? 1 : -1;
            int rotationM[] = {
                R[0], -c*R[2], c*R[1],
                R[3], -c*R[5], c*R[4],
                R[6], -c*R[8], c*R[7]
            };
            R = rotationM;
        }

        /* y clockwise = [ 0  0  1 ] , y anti-clockwise = [ 0  0 -1 ]
                         [ 0  1  0 ]                      [ 0  1  0 ]
                         [-1  0  0 ]                      [ 1  0  0 ] */
        private void rotateY(boolean clockwise) {
            int c = clockwise ? 1 : -1;
            int rotationM[] = {
                c*R[2], R[1], -c*R[0],
                c*R[5], R[4], -c*R[3],
                c*R[8], R[7], -c*R[6]
            };
            R = rotationM;
        }

        /* z clockwise = [ 0 -1  0 ] , z anti-clockwise = [ 0  1  0 ]
                         [ 1  0  0 ]                      [-1  0  0 ]
                         [ 0  0  1 ]                      [ 0  0  1 ] */
        private void rotateZ(boolean clockwise) {
            int c = clockwise ? 1 : -1;
            int rotationM[] = {
                -c*R[1], c*R[0], R[2],
                -c*R[4], c*R[3], R[5],
                -c*R[7], c*R[6], R[8],
            };
            R = rotationM;
        }
    }
}