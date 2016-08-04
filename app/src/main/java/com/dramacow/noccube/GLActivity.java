package com.dramacow.noccube;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class GLActivity extends Activity {

    private GLSurfaceViewEx surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

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
