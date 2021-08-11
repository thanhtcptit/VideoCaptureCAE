package com.androidwave.camera2video;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements KeyEvent.Callback{

    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (null == savedInstanceState) {
            cameraFragment = CameraFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, cameraFragment)
                    .commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            cameraFragment.changeMode();
        }
        return true;
    }
}