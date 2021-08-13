package com.androidwave.camera2video.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImgUtils {
    private final static String TAG = "ImgUtils";

    public static Bitmap decodeImageJPEGToBMP(Image image) {
        if (image == null) return null;
        if (image.getFormat() != ImageFormat.JPEG) return null;

        long start1 = System.currentTimeMillis();

        ByteBuffer jpgBuffer = image.getPlanes()[0].getBuffer();
        byte[] byteArray = new byte[jpgBuffer.remaining()];
        jpgBuffer.get(byteArray);
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        long finish1 = System.currentTimeMillis();
        long timeElapsed1 = finish1 - start1;
        Log.i("render", String.format("converttobmp() *%d", timeElapsed1));
        return bmp;
    }

    public static Bitmap convertJPEGBytesToBMP(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static Bitmap rgbaBytesToBitmap(byte[] bitmapdata, int imageWidth, int imageHeight) {
        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = ByteBuffer.wrap(bitmapdata);
        bmp.copyPixelsFromBuffer(buffer);
        return bmp;
    }

    public static byte[] convert_RGB_To_RGBA(byte[] rgb, int width, int height) {
        byte[] rgba = new byte[width * height * 4];
        int rgbPix;
        int rgbaPix;
        for (int i = 0; i < rgb.length / 3; i++) {
            rgbPix = i * 3;
            rgbaPix = i * 4;
            rgba[rgbaPix++] = rgb[rgbPix++];
            rgba[rgbaPix++] = rgb[rgbPix++];
            rgba[rgbaPix++] = rgb[rgbPix];
            rgba[rgbaPix] = -1;
        }
        return rgba;
    }

    public static Bitmap convert_NV21ToBMP(byte[] nv21, int width, int height) {
        byte[] rgb = NativeConversion.convertYuvToRgb(NativeConversion.RGB_TYPE.RGB, nv21,
                NativeConversion.YUV_TYPE.NV21, width, height);
        byte[] rgba = convert_RGB_To_RGBA(rgb, width, height);
        return rgbaBytesToBitmap(rgba, width, height);
    }

    public static Bitmap convert_decodeImageYUVToBMP(Image image) {
        if (image == null) return null;
        int width = image.getWidth();
        int height = image.getHeight();
        Image.Plane Y = image.getPlanes()[0];
        Image.Plane UV = image.getPlanes()[1];

        int Yb = Y.getBuffer().remaining();
        int Ub = UV.getBuffer().remaining();

        byte[] data = new byte[Yb + Ub];

        Y.getBuffer().get(data, 0, Yb);
        UV.getBuffer().get(data, Yb, Ub);

        YuvImage YUVImage = new YuvImage(data, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        if (YUVImage != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                YUVImage.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
                return bmp;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static byte[] convertJPEG(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public static byte[] convertYUV420ToNV21(Image image) {
        if (image == null) return null;
        if (image.getFormat() != ImageFormat.YUV_420_888) return null;
        byte[] nv21;

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    public static Bitmap convert_YUV420ToBMP(Image image) {
        byte[] nv21 = convertYUV420ToNV21(image);
        int width = image.getWidth();
        int height = image.getHeight();

        YuvImage YUVImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        Bitmap bmp = null;

        if (YUVImage != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                YUVImage.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return bmp;
    }

    private static ImageData convert_rgbToNV21(Image image) {
        ByteBuffer rBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer gBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer bBuffer = image.getPlanes()[2].getBuffer();

        byte[] rBytes = bufferToBytes(rBuffer);
        byte[] gBytes = bufferToBytes(gBuffer);
        byte[] bBytes = bufferToBytes(bBuffer);

        int width = image.getWidth();
        int height = image.getHeight();
        int frameSize = width * height;
        int R, G, B, Y, U, V;
        int uvIndex = frameSize;
        byte[] nv21 = new byte[(int) (frameSize * 1.5)];
        for (int i = 0; i < width * height; i++) {
            R = rBytes[i];
            G = gBytes[i];
            B = bBytes[i];
            Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

            nv21[i] = (byte) Y;
            if ((i + 1) % 4 == 0) {
                nv21[uvIndex++] = (byte) (U);
                nv21[uvIndex++] = (byte) (V);
            }
        }
        return new ImageData(nv21, width, height, ExImageFormat.CONVERTED_NV21, -1);
    }

    public static byte[] convert_RgbaToNV21(int[] argb, int width, int height) {
        final int frameSize = width * height;
        byte[] nv21 = new byte[frameSize * 3 / 2];
        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                nv21[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    nv21[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    nv21[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                index++;
            }
        }
        return nv21;
    }

    public static ImageData convertToImageData(Image image, int requestedFormat) {
        switch (requestedFormat) {
            case ExImageFormat.CONVERTED_NV21:
                Bitmap tmp = decodeImageJPEGToBMP(image);
                int[] intArray = bitmapToInt(tmp);
                byte[] nv21 = convert_RgbaToNV21(intArray, tmp.getWidth(), tmp.getHeight());
                return new ImageData(nv21, image.getWidth(), image.getHeight(), ExImageFormat.NV21, -1);
            case ExImageFormat.JPEG:
                Bitmap bitmap = decodeImageJPEGToBMP(image);
                byte[] bytes = bitmapToBytes(bitmap);
                return new ImageData(bytes, image.getWidth(), image.getHeight(),
                        ExImageFormat.RGBA, -1);
            case ExImageFormat.NV21:
                return convert_YUV420888ToNV(image, requestedFormat);
            default:
                return null;
        }
    }

    private static byte[] bufferToBytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    public static int[] bitmapToInt(Bitmap bitmap) {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        int[] intArray = new int[x * y];
        bitmap.getPixels(intArray, 0, x, 0, 0, x, y);
        return intArray;
    }

    /**
     * Return an byte array of I420 image
     *
     * @param image an Android YUV_420_888 image which U/V pixel stride may be larger than the
     *              size of a single pixel
     * @return I420 byte array which U/V pixel stride is always 1.
     */
    private static byte[] convert_YUV_420_888_To_I420(Image image) {

        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[imageWidth * imageHeight *
                ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        int offset = 0;

        for (int plane = 0; plane < planes.length; ++plane) {
            final ByteBuffer buffer = planes[plane].getBuffer();
            final int rowStride = planes[plane].getRowStride();
            // Experimentally, U and V planes have |pixelStride| = 2, which
            // essentially means they are packed.
            final int pixelStride = planes[plane].getPixelStride();
            final int planeWidth = (plane == 0) ? imageWidth : imageWidth / 2;
            final int planeHeight = (plane == 0) ? imageHeight : imageHeight / 2;
            if (pixelStride == 1 && rowStride == planeWidth) {
                // Copy whole plane from buffer into |data| at once.
                buffer.get(data, offset, planeWidth * planeHeight);
                offset += planeWidth * planeHeight;
            } else {
                // Copy pixels one by one respecting pixelStride and rowStride.
                byte[] rowData = new byte[rowStride];
                for (int row = 0; row < planeHeight - 1; ++row) {
                    buffer.get(rowData, 0, rowStride);
                    for (int col = 0; col < planeWidth; ++col) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
                // Last row is special in some devices and may not contain the full
                // |rowStride| bytes of data.
                // See http://developer.android.com/reference/android/media/Image.Plane.html#getBuffer()
                buffer.get(rowData, 0, Math.min(rowStride, buffer.remaining()));
                for (int col = 0; col < planeWidth; ++col) {
                    data[offset++] = rowData[col * pixelStride];
                }
            }
        }
        return data;
    }

    /**
     * Input: YUV420888 (which is captured from Android Camera2)
     * Output: bytes array of NV21 format (YYYYYYYY VUVU)
     * size: WxHx1.5, UV - interleaved
     */

    public static ImageData convert_YUV420888ToNV(Image image, int dstNVFormat) {
        if (image == null) return null;
        if (image.getFormat() != ImageFormat.YUV_420_888) return null;

        int width = image.getWidth();
        int height = image.getHeight();
        int yByteLen = width * height;
        int swapped = (ExImageFormat.NV12 == dstNVFormat) ? 0 : 1;
        ByteBuffer yBuffer;
        ByteBuffer uBuffer;
        ByteBuffer vBuffer;
        try {
            yBuffer = image.getPlanes()[0].getBuffer();
            uBuffer = image.getPlanes()[1 + swapped].getBuffer();
            vBuffer = image.getPlanes()[2 - swapped].getBuffer();
        } catch (Exception e) {
            Log.e(TAG, "Fail to get buffers from image.");
            e.printStackTrace();
            return null;
        }

        int uPixelStride = image.getPlanes()[1].getPixelStride();
        int uvPixelStride = image.getPlanes()[2].getPixelStride();
        int uvByteLen = width * height / 4 * uvPixelStride;

        byte[] NV = new byte[yByteLen * 3 / 2];
        yBuffer.get(NV, 0, yByteLen);

        int currentPos = yByteLen;
        if (yByteLen + uvByteLen / uvPixelStride * 2 != NV.length) {
            Log.d(TAG, "length not matched");
            return null;
        }

        for (int i = 0; i < uvByteLen / uvPixelStride; i++) {
            //UV interleaved
            NV[currentPos] = uBuffer.get(i * uvPixelStride); //U-value
            NV[currentPos + 1] = vBuffer.get(i * uvPixelStride); //V-value
            currentPos += 2;
        }

        ImageData imageData = new ImageData(NV, width, height, dstNVFormat, 0);
       /* Log.d(TAG, String.format("NV21 size (%d,%d), bytes length %d, matched: %s", width,
                height, imageData.getData().length, currentPos==NV.length));*/
        return imageData;
    }
}
