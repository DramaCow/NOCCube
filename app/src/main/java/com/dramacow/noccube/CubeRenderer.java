package com.dramacow.noccube;

public interface CubeRenderer {
    // TODO: no longer need to pass in program
    public void draw(final float[] vpMatrix, final int program, final float dt);
    public void permute(final int axis, final int slice,  final boolean clockwise);
}
