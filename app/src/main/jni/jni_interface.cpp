#include <android/asset_manager_jni.h>

#include "autoexp.h"

extern "C"
void setElapsedTime(JNIEnv *env, jobject thiz) {
    jclass thisClass = env->GetObjectClass(thiz);
    jfieldID fidElapsedMs = env->GetFieldID(thisClass, "elapsedMs", "I");
    if (fidElapsedMs == NULL) {
        LOGE("fidElapsedMs is null");
        return;
    }
    jint elapsedMs = AutoExposureSDK::instance->elapsedMs;
    env->SetIntField(thiz, fidElapsedMs, elapsedMs);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGD("JNI_OnLoad");
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    LOGD("JNI_OnUnload");
}

extern "C" JNIEXPORT void JNICALL
Java_com_androidwave_camera2video_AutoExposureSDK_init(JNIEnv* env, jobject thiz, jobject mgr, jboolean use_gpu) {
    if (AutoExposureSDK::instance == nullptr) {
//        AAssetManager* mgr = AAssetManager_fromJava(env, mgr);
        AutoExposureSDK::instance = new AutoExposureSDK();
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_androidwave_camera2video_AutoExposureSDK_process(JNIEnv *env, jobject thiz, jbyteArray input, jint width, jint height, jboolean isActive) {
    if (AutoExposureSDK::instance == nullptr) {
        LOGE("Instance is null");
        return -1;
    }
    int res = AutoExposureSDK::instance->process(env, input, width, height, isActive);
    setElapsedTime(env, thiz);
    return res;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_androidwave_camera2video_AutoExposureSDK_getExposureValue(JNIEnv * env, jobject thiz) {
    return AutoExposureSDK::instance->currentExposure;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_androidwave_camera2video_AutoExposureSDK_getISOValue(JNIEnv * env, jobject thiz) {
    return AutoExposureSDK::instance->currentISO;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_androidwave_camera2video_AutoExposureSDK_getMeanValue(JNIEnv *env, jobject thiz) {
    return AutoExposureSDK::instance->currentMean;
}
