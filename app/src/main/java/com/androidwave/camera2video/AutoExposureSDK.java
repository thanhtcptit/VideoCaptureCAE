package com.androidwave.camera2video;

import android.content.res.AssetManager;

public class AutoExposureSDK {
    public native void init(AssetManager mgr, boolean useGpu);

    public native int process(byte[] input, int width, int height, boolean isActive);

    public native int getExposureValue();

    public native int getISOValue();

    public native float getMeanValue();

    private int elapsedMs = 0;

    public int getElapsedMs() {
        return elapsedMs;
    }

    static {
        System.loadLibrary("autoexp");
    }
}
