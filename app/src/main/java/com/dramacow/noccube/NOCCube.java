package com.dramacow.noccube;

import android.util.Log;

public class NOCCube {

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;

    public final int D; // Cube dimensions
    private NOCCube.Cube cubes[][][]; // z, y, x

    private interface IntLambda {
        int operation(int a, int b, int c);
    }

    private int operate(IntLambda op, int a, int b, int c) {
        return op.operation(a, b, c);
    }

    private Cube[][][] permute(IntLambda opX, IntLambda opY, IntLambda opZ) {
        final Cube new_cubes[][][] = new NOCCube.Cube[D][D][D];

        for (int z = 0; z < D; z++) {
            for (int y = 0; y < D; y++) {
                for (int x = 0; x < D; x++) {
                    new_cubes[z][y][x] = cubes[operate(opZ, x, y, z)][operate(opY, x, y, z)][operate(opX, x, y, z)];
                }
            }
        }

        return new_cubes;
    }

    public NOCCube(final int D) {
        this.D = (D > 1 && D < 10) ? D : 2;

        cubes = new NOCCube.Cube[D][D][D];
        for (int z = 0; z < D; z++) {
            for (int y = 0; y < D; y++) {
                for (int x = 0; x < D; x++) {
                    cubes[z][y][x] = new NOCCube.Cube(x + D*y + D*D*z);
                }
            }
        }
    }

    public int[] rotate(final int axis, final int slice, final boolean clockwise) {
        if (axis < 0 || axis >= 3 || slice < 0 || slice >= D) return new int[]{}; // Validate by return empty array

        final int cubeHandles[] = new int[D*D];

        // Deep copy (NOTE: clone() appears to only make shallow copies for non-primitive arrays)
        Cube new_cubes[][][] = new Cube[D][D][D];
        for (int z = 0; z < D; z++) {
            for (int y = 0; y < D; y++) {
                for (int x = 0; x < D; x++) {
                    new_cubes[z][y][x] = new NOCCube.Cube(cubes[z][y][x]);
                }
            }
        }


        printToLog();

        switch (axis) {
            // x-axis
            case AXIS_X:
                for (int z = 0; z < D; z++) {
                    for (int y = 0; y < D; y++) {
                        cubeHandles[y + D*z] = cubes[z][y][slice].displayHandle;
                    }
                }

                for (int z = 0; z < D; z++) {
                    for (int y = 0; y < D; y++) {
                        if (!clockwise) new_cubes[z][y][slice] = new NOCCube.Cube(cubes[y][(D-1)-z][slice]);
                        else           new_cubes[z][y][slice] = new NOCCube.Cube(cubes[(D-1)-y][z][slice]);
                    }
                }

                break;

            // y-axis
            case AXIS_Y:
                for (int z = 0; z < D; z++) {
                    for (int x = 0; x < D; x++) {
                        cubeHandles[x + D*z] = cubes[z][slice][x].displayHandle;
                    }
                }

                for (int z = 0; z < D; z++) {
                    for (int x = 0; x < D; x++) {
                        if (!clockwise) new_cubes[z][slice][x] = new NOCCube.Cube(cubes[(D-1)-x][slice][z]);
                        else           new_cubes[z][slice][x] = new NOCCube.Cube(cubes[x][slice][(D-1)-z]);
                    }
                }

                break;

            // z-axis
            case AXIS_Z:
                for (int y = 0; y < D; y++) {
                    for (int x = 0; x < D; x++) {
                        cubeHandles[x + D*y] = cubes[slice][y][x].displayHandle;
                    }
                }

                for (int y = 0; y < D; y++) {
                    for (int x = 0; x < D; x++) {
                        if (!clockwise) new_cubes[slice][y][x] = new NOCCube.Cube(cubes[slice][x][(D-1)-y]);
                        else           new_cubes[slice][y][x] = new NOCCube.Cube(cubes[slice][(D-1)-x][y]);
                    }
                }

                break;
        }

        cubes = new_cubes;

        printToLog();

        return cubeHandles;
    }

    public class Cube {
        float anglex, angley;
        final int displayHandle;

        public Cube(final int displayHandle) {
            anglex = angley = 0.0f;
            this.displayHandle = displayHandle;
        }

        public Cube(final Cube that) {
            this.anglex = that.anglex;
            this.angley = that.angley;
            this.displayHandle = that.displayHandle;
        }
    }

    public void printToLog() {
        String line = "";

        for (int z = 0; z < D; z++) {
            for (int y = 0; y < D; y++) {
                for (int x = 0; x < D; x++) {
                    line += cubes[z][y][x].displayHandle + ", ";
                }
                Log.d("SAM", line); line = "";
            }
        }
    }
}