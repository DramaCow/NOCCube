package com.dramacow.noccube;

import android.util.Log;

import java.util.Arrays;

public class NOCCube {

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;

    public final int D; // Cube dimensions
    public NOCCube.Cube cubes[][][]; // z, y, x  TODO: should this really be public?

    /*
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
    */

    public NOCCube(final int D) {
        this.D = D > 1 ? D : 2;

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
        Log.d("SAM",
            (axis == AXIS_X ? "X " : axis == AXIS_Y ? "Y " : "Z ") +
            (Integer.toString(slice) + " ") +
            (clockwise ? "clockwise" : "anti-clockwise")
        );

        if (axis < 0 || axis >= 3 || slice < 0 || slice >= D) return new int[]{}; // Validate by return empty array

        final int cubeHandles[] = new int[D*D]; // TODO: make this a private member

        // Deep copy (NOTE: clone() appears to only make shallow copies for non-primitive arrays)
        Cube new_cubes[][][] = new Cube[D][D][D];
        for (int z = 0; z < D; z++) {
            for (int y = 0; y < D; y++) {
                for (int x = 0; x < D; x++) {
                    new_cubes[z][y][x] = new NOCCube.Cube(cubes[z][y][x]);
                }
            }
        }

        // TODO: place each of the rotations into a seperate function
        switch (axis) {
            // x-axis
            case AXIS_X: {
                for (int z = 0; z < D; z++) {
                    for (int y = 0; y < D; y++) {
                        cubeHandles[y + D * z] = cubes[z][y][slice].displayHandle;
                    }
                }

                for (int z = 0; z < D; z++) {
                    for (int y = 0; y < D; y++) {
                        if (!clockwise) {
                            new_cubes[z][y][slice] = new NOCCube.Cube(cubes[y][(D - 1) - z][slice]);
                            new_cubes[z][y][slice].anglex = (new_cubes[z][y][slice].anglex - 90) % 360;
                        } else {
                            new_cubes[z][y][slice] = new NOCCube.Cube(cubes[(D - 1) - y][z][slice]);
                            new_cubes[z][y][slice].anglex = (new_cubes[z][y][slice].anglex + 90) % 360;
                        }
                    }
                }

                break;
            }

            // y-axis
            case AXIS_Y: {
                for (int z = 0; z < D; z++) {
                    for (int x = 0; x < D; x++) {
                        cubeHandles[x + D * z] = cubes[z][slice][x].displayHandle;
                    }
                }

                for (int z = 0; z < D; z++) {
                    for (int x = 0; x < D; x++) {
                        if (!clockwise) {
                            new_cubes[z][slice][x] = new NOCCube.Cube(cubes[(D - 1) - x][slice][z]);
                            new_cubes[z][slice][x].angley = (new_cubes[z][slice][x].angley - 90) % 360;
                        } else {
                            new_cubes[z][slice][x] = new NOCCube.Cube(cubes[x][slice][(D - 1) - z]);
                            new_cubes[z][slice][x].angley = (new_cubes[z][slice][x].angley + 90) % 360;
                        }
                    }
                }

                break;
            }

            // z-axis
            case AXIS_Z: {
                for (int y = 0; y < D; y++) {
                    for (int x = 0; x < D; x++) {
                        cubeHandles[x + D * y] = cubes[slice][y][x].displayHandle;
                    }
                }

                for (int y = 0; y < D; y++) {
                    for (int x = 0; x < D; x++) {
                        if (!clockwise) {
                            new_cubes[slice][y][x] = new NOCCube.Cube(cubes[slice][x][(D - 1) - y]);
                            new_cubes[slice][y][x].anglez = (new_cubes[slice][y][x].anglez - 90) % 360;
                        } else {
                            new_cubes[slice][y][x] = new NOCCube.Cube(cubes[slice][(D - 1) - x][y]);
                            new_cubes[slice][y][x].anglez = (new_cubes[slice][y][x].anglez + 90) % 360;
                        }
                    }
                }

                break;
            }
        }

        cubes = new_cubes;

        String message = "";
        Arrays.sort(cubeHandles);
        for (int h : cubeHandles) {
            message += Integer.toString(h) + ", ";
        }
        Log.d("SAM", message);

        return cubeHandles;
    }

    public boolean isSolved() {
        for (int z = 0; z < D; z++) {
            for (int y = 0; y < D; y++) {
                for (int x = 0; x < D; x++) {
                    if (cubes[z][y][x].displayHandle != x + D*y + D*D*z ||
                        cubes[z][y][x].anglex != 0.0f                   ||
                        cubes[z][y][x].angley != 0.0f                   ||
                        cubes[z][y][x].anglez != 0.0f) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public class Cube {
        int anglex, angley, anglez;
        final int displayHandle;

        public Cube(final int displayHandle) {
            anglex = angley = anglez = 0;
            this.displayHandle = displayHandle;
        }

        public Cube(final Cube that) {
            this.anglex = that.anglex;
            this.angley = that.angley;
            this.anglez = that.anglez;
            this.displayHandle = that.displayHandle;
        }
    }
}