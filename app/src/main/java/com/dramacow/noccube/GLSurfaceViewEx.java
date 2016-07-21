package com.dramacow.noccube;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class GLSurfaceViewEx extends GLSurfaceView {

    private GLRendererEx renderer;
    private boolean rotatingCamera = false;
    private boolean rotatingCube = false;

    private float x1, y1;
    private float x2, y2;

    GLSurfaceViewEx(Context context) {
        super(context);

        // Create ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new GLRendererEx(context); //SHADER GETS CREATED IN HERE

        // Set the renderer for drawing on GLSurfaceView
        setRenderer(renderer);

        // Render the view only when there is a change in the drawing data (only ideal for GUIs)
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from touch screen, and other input controls.
        //renderer.permute(NOCCube.AXIS_X, 0, false);

        // Touch co-ordinates
        final float x = e.getX();
        final float y = e.getY();

        // Screen dimensions
        final float w = getWidth();
        final float h = getHeight();

        // Normalised device co-ordinates
        final float norm_x = ( (2.0f*x) / w ) - 1.0f;
        final float norm_y = -( (2.0f*y) / h ) + 1.0f; // Negated since y-axis upside down in opengl

        // Each case has their own scope to allow declaring different variables of the same names
        // without throwing annoying/retarded errors.
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x1 = x;
                y1 = y;

                final float point[] = new float[3];
                final int face = renderer.castRay(point, norm_x, norm_y);

                if (face != -1) {
                    rotatingCube = true;
                    renderer.face0 = face;
                    renderer.point0 = point;
                } else {
                    rotatingCamera = true;
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                if (rotatingCamera) {
                    x2 = x;
                    y2 = y;

                    renderer.adjustViewAngle(
                            2.0f * (float) Math.PI * ((y2 - y1) / h),
                            4.0f * (float) Math.PI * ((x2 - x1) / w)
                    );

                    x1 = x2;
                    y1 = y2;
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                if (rotatingCube) {
                    final float point[] = new float[3];
                    final int face = renderer.castRay(point, norm_x, norm_y);

                    if (face == renderer.face0) {
                        final float delta[] = {
                                point[0] - renderer.point0[0],
                                point[1] - renderer.point0[1],
                                point[2] - renderer.point0[2]
                        };

                        int axis = 0; int slice = 0; boolean clockwise = false;

                        // front and back faces
                        if (face == 0 || face == 1) {
                            if (Math.abs(delta[0]) < Math.abs(delta[1])) {
                                axis = NOCCube.AXIS_X;
                                slice = (int) Math.floor( ((renderer.point0[0] + 1.0f)/2.0f) * renderer.noccube.D );
                                clockwise = (face == 0 && delta[1] < 0.0f) || (face == 1 && delta[1] >= 0.0f);
                            }
                            else {
                                axis = NOCCube.AXIS_Y;
                                slice = (int) Math.floor( ((renderer.point0[1] + 1.0f)/2.0f) * renderer.noccube.D );
                                clockwise = (face == 0 && delta[0] >= 0.0f) || (face == 1 && delta[0] < 0.0f);
                            }
                        }
                        // left and right faces
                        else if (face == 2 || face == 3) {
                            if (Math.abs(delta[1]) < Math.abs(delta[2])) {
                                axis = NOCCube.AXIS_Y;
                                slice = (int) Math.floor( ((renderer.point0[1] + 1.0f)/2.0f) * renderer.noccube.D );
                                clockwise = (face == 2 && delta[2] >= 0.0f) || (face == 3 && delta[2] < 0.0f);
                            }
                            else {
                                axis = NOCCube.AXIS_Z;
                                slice = (int) Math.floor( ((renderer.point0[2] + 1.0f)/2.0f) * renderer.noccube.D );
                                clockwise = (face == 2 && delta[1] < 0.0f) || (face == 3 && delta[1] >= 0.0f);
                            }
                        }
                        // top and bottom faces
                        else if (face == 4 || face == 5) {
                            if (Math.abs(delta[0]) < Math.abs(delta[2])) {
                                axis = NOCCube.AXIS_X;
                                slice = (int) Math.floor( ((renderer.point0[0] + 1.0f)/2.0f) * renderer.noccube.D );
                                clockwise = (face == 4 && delta[2] >= 0.0f) || (face == 5 && delta[2] < 0.0f);
                            }
                            else {
                                axis = NOCCube.AXIS_Z;
                                slice = (int) Math.floor( ((renderer.point0[2] + 1.0f)/2.0f) * renderer.noccube.D );
                                clockwise = (face == 4 && delta[0] < 0.0f) || (face == 5 && delta[0] >= 0.0f);
                            }
                        }

                        renderer.permute(axis, slice, clockwise);
                    }
                }
                rotatingCamera = rotatingCube = false;
            }
            break;
        }

        return true;
    }
}