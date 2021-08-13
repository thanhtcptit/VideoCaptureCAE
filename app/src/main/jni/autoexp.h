#ifndef AUTOEXPOSURE_H
#define AUTOEXPOSURE_H

#include <math.h>
#include <string>
#include <vector>
#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <opencv2/opencv.hpp>

#include "tensorflow/lite/model.h"
#include "tensorflow/lite/kernels/register.h"


#if defined(__ANDROID__)
#define TAG "AutoExposureSDK"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#include "tensorflow/lite/delegates/gpu/gl_delegate.h"
#include "tensorflow/lite/delegates/gpu/delegate.h"
#endif

class AutoExposureSDK {
public:
    static AutoExposureSDK* instance;
    float currentMean = 0;
    int currentExposure = 30, currentISO = 500, currentExposureIndex = 2, currentISOIndex = 4;

    struct timeval start_time, stop_time;
    int elapsedMs;

    AutoExposureSDK();
    ~AutoExposureSDK();

    int process(JNIEnv *env, jbyteArray input, jint width, jint height, jboolean isActive);
private:
    int minExposure = 10, maxExposure = 1000, minISO = 100, maxISO = 1600;
    int exposureStep = 5, isoStep = 50;
    float optimalMSV = 2.5f, acceptanceInterval = 0.25f;
    float exposureRange[14], isoRange[16];
    bool increaseExp = false, decreaseExp = false, changeISO = false;
};

#endif //AUTOEXPOSURE_H

