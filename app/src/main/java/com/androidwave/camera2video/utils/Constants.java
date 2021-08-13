package com.androidwave.camera2video.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
import android.util.Size;

import java.util.ArrayList;



public class Constants {
    public static boolean useCAE = false;

    public static int videoFrameWidth = 1280;
    public static int videoFrameHeight = 720;
    public static ImageData image = new ImageData(null,0,0,0,0);

    public static int exposureValue = 30;
    public static int isoValue = 500;
    public static int exposureIndex = 2;
    public static int isoIndex = 4;
    public static final int[] allExpFrac = {6, 12, 30, 50, 80, 125, 200, 300, 400, 500, 600, 700, 800, 1000};
    public static final int[] allISO = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600};
}
