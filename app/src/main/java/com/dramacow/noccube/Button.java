package com.dramacow.noccube;

import android.opengl.Matrix;

public class Button {
    private float x = 0.0f, y = 0.0f; // centre
    private float w = 1.0f, h = 1.0f; // half extents

    private boolean hasClicked = false; // Used to prevent repeated onClick calls when button is held

    private Quad quad;

    public Button(final int texId) {
        this.quad = new Quad(texId);
    }

    public void set(float x, float y, float w, float h) {
        this.x = x; this.y = y;
        this.w = w; this.h = h;

        Matrix.setIdentityM(this.quad.modelM, 0);
        Matrix.translateM(this.quad.modelM, 0, x, y, 0.0f);
        Matrix.scaleM(this.quad.modelM, 0, w, h, 1.0f);
    }

    // To be overidden
    public void onClick() {}

    //To be called in the update function of the screen class
    public final void update(float touchx, float touchy, boolean down) {
        if (hasClicked && !down) hasClicked = false;

        //If the mouse/touchscreen is on the button
        if (!(touchx < x-w || touchx > x+w || touchy < y-h || touchy > y+h)) {
            //Check if touched or hovered
            if (down) {
                if (!hasClicked) {
                    hasClicked = true;
                    onClick();
                }
            }
        }
    }

    public void draw(final float[] vpMatrix, final int program) {
        float mvpMatrix[] = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, this.quad.modelM, 0);
        this.quad.draw(mvpMatrix, program, 1.0f);
    }
}
