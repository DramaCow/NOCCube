package com.dramacow.noccube;

public class DisplayCubeRenderer implements CubeRenderer {
    public Cube displaycube;

    public DisplayCubeRenderer(final int tex[]) {
        displaycube = new Cube(
            new int[] {
                tex[0], tex[1], tex[2],
                tex[3], tex[4], tex[5]
            },
            new float[][] {
                Cube.fullFaceTexCoords, Cube.fullFaceTexCoords, Cube.fullFaceTexCoords,
                Cube.fullFaceTexCoords, Cube.fullFaceTexCoords, Cube.fullFaceTexCoords
            }
        );
    }

    public void draw(final float[] vpMatrix, final int program, final float dt) {
        displaycube.draw(vpMatrix, program, 1.0f);
    }

    public void permute(final int axis, final int slice,  final boolean clockwise) {
        // Do nothing
    }
}
