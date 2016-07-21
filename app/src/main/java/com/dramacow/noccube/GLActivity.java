package com.dramacow.noccube;

import android.app.Activity;
import android.os.Bundle;

public class GLActivity extends Activity {

    private GLSurfaceViewEx surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new GLSurfaceViewEx(this);
        setContentView(surfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Consider de-allocating objects that
        // consume significant memory here.
        surfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // re-allocate de-allocated graphic objects from onPause()
        surfaceView.onResume();
    }
}
