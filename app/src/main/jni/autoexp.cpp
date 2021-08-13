#include "autoexp.h"

double get_us(struct timeval t) { return (t.tv_sec * 1000000 + t.tv_usec); }

AutoExposureSDK* AutoExposureSDK::instance = nullptr;

AutoExposureSDK::AutoExposureSDK()
: exposureRange {6, 12, 30, 50, 80, 125, 200, 300, 400, 500, 600, 700, 800, 1000},
  isoRange {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 1600} {
    LOGD("init");
}

AutoExposureSDK::~AutoExposureSDK() {
    delete instance;
}

int AutoExposureSDK::process(JNIEnv *env, jbyteArray input, jint width, jint height, jboolean isActive) {
    gettimeofday(&start_time, nullptr);

    jbyte *inputBuffer = env->GetByteArrayElements(input, JNI_FALSE);
    cv::Mat yuv(height + height / 2, width, CV_8UC1, inputBuffer);
    cv::Mat rgb, hsv, hist;
    cv::cvtColor(yuv, rgb, cv::COLOR_YUV2RGB_NV21, 3);
    cv::cvtColor(rgb, hsv, cv::COLOR_RGB2HSV);

    const int channels[] = {2};
    int histSize = 5;
    float range[] = { 0, 256 };
    const float* histRange[] = { range };
    cv::calcHist(&hsv, 1, channels, cv::Mat(), hist, 1, &histSize, histRange, true, false);
    auto ptr = (float*) hist.data;
    currentMean = (ptr[0] + 2 * ptr[1] + 3 * ptr[2] + 4 * ptr[3] + 5 * ptr[4]) / (float) (rgb.rows * rgb.cols);

    if (isActive) {
        float err = optimalMSV - currentMean;
        int multiply = 5 * (int) std::abs(err / acceptanceBound);
        if (err > acceptanceBound || err < -acceptanceBound) {
            currentExposureIndex -= (err > 0) ? 1 : ((err < 0) ? -1 : 0);
            currentExposureIndex = std::min(std::max(currentExposureIndex, 0), 13);

            currentExposure -= multiply * ((err > 0) ? 1 : ((err < 0) ? -1 : 0));
            currentExposure = std::min(std::max(currentExposure, 6), 1000);
        } else {
            changeGain = false;
        }

        gettimeofday(&stop_time, nullptr);
        elapsedMs = (int) (get_us(stop_time) - get_us(start_time)) / 1000;
        LOGD("Mean: %f - Exp: %d - Process time: %d ms", currentMean, currentExposure, elapsedMs);
    }
    return 1;
}
