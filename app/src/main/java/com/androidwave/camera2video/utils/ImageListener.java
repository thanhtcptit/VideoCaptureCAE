package com.androidwave.camera2video.utils;

import android.content.Context;
import android.graphics.ImageFormat;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;

import com.androidwave.camera2video.ui.base.BaseFragment;

import java.nio.ByteBuffer;

public class ImageListener implements ImageReader.OnImageAvailableListener {
    private BaseFragment mFragment;
    private Handler mBackgroundHandler;
    private int countFrame, skipFrames;
    private boolean isReady;

    public ImageListener(BaseFragment fragment, Handler handler) {
        mFragment = fragment;
        mBackgroundHandler = handler;
        countFrame = 0;
        skipFrames = 5;
        isReady = true;
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        countFrame += 1;
        if (countFrame >= skipFrames && isReady) {
            countFrame = 0;
            isReady = false;

            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    Image image = imageReader.acquireLatestImage();
                    if (image != null) {
                        int imageFormat = image.getFormat();

                        if (imageFormat == ImageFormat.JPEG) {
                            byte[] bytes = ImgUtils.convertJPEG(image);
                            Constants.image = new ImageData(bytes, image.getWidth(), image.getHeight(), imageFormat, 0);
                            image.close();

                            mFragment.processAE();
                        } else if (imageFormat == ImageFormat.YUV_420_888) {
                            byte[] bytes = ImgUtils.convertYUV420ToNV21(image);
                            Constants.image = new ImageData(bytes, image.getWidth(), image.getHeight(), imageFormat, 0);
                            image.close();

                            mFragment.processAE();
                        } else {
                            Log.e("ImageListener", "Wrong format");
                            image.close();
                        }
                    }
                    isReady = true;
                }
            });
        } else {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    Image image = imageReader.acquireLatestImage();
                    if (image != null) image.close();
                }
            });
        }
    }
}
