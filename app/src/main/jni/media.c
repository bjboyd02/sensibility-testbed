//
// Created by lukas on 5/27/16.
//
#include "media.h"

//void _start_stop_media(const char* method_name) {
//    JNIEnv* jni_env;
//    // Use the cached JVM pointer to get a new environment
//    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
//    // Find LocationService class and get singleton instance
//    jclass media_class = jnihelper_getClass(jni_env, "com/snakei/MediaService");
//    jobject instance = jnihelper_getInstance(jni_env, media_class, "com/snakei/MediaService");
//    jnihelper_callVoidMethod(jni_env, media_class, instance, method_name);
//
//    // XXX: Only detach if AttachCurrentThread wasn't a no-op
//    //(*cached_vm)->DetachCurrentThread(cached_vm);
//
//    (*jni_env)->DeleteLocalRef(jni_env, media_class);
//    (*jni_env)->DeleteLocalRef(jni_env, instance);
//}
//
//void media_start_media() {
//    _start_stop_media("start_media");
//}
//
//void media_stop_media() {
//    _start_stop_media("stop_media");
//}

//PyObject* media_tts_speak(PyObject *self, PyObject *args) {
//
//    if (!PyArg_ParseTuple(args, "b", &message)){
//        LOGI("Wrong arguments. I guess I should raise an Exception.");
//        Py_RETURN_NONE;
//    }
//    JNIEnv *jni_env;
//    // Use the cached JVM pointer to get a new environment
//    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
//    // Find LocationService class and get singleton instance
//    jclass media_class = jnihelper_getClass(jni_env, "com/snakei/MediaService");
//    jobject instance = jnihelper_getInstance(jni_env, media_class, "com/snakei/MediaService");
//
//    jmethodID method = (*jni_env)->GetMethodID(jni_env, media_class, "ttsSpeak", "(Ljava/lang/String;)Z");
//    jboolean success = (*jni_env)->CallBooleanMethod(jni_env, instance, method, message);
//
//    (*jni_env)->DeleteLocalRef(jni_env, media_class);
//    (*jni_env)->DeleteLocalRef(jni_env, instance);
//
//    if (success) {
//        Py_RETURN_TRUE;
//    } else {
//        Py_RETURN_FALSE;
//    }
//}

void test_me(){

    JNIEnv *jni_env;
    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    // Find LocationService class and get singleton instance
    jclass media_class = jnihelper_getClass(jni_env, "com/snakei/MediaService");
    jobject instance = jnihelper_getInstance(jni_env, media_class, "com/snakei/MediaService");

    jint a = 100;
    jint b = 200;
    jint c = 42;
    jnihelper_callVoidMethod(jni_env, media_class, instance, "test", "(III)V", a,b,c);
    (*jni_env)->DeleteLocalRef(jni_env, media_class);
    (*jni_env)->DeleteLocalRef(jni_env, instance);
}