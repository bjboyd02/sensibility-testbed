//
// Created by lukas on 5/27/16.
//
#include "media.h"

struct media_cache {
    jclass class;
    jmethodID get_instance;
    jmethodID microphone_record;
    jmethodID tts_speak;
} m_cached;


void media_start_media() {
    JNIEnv* jni_env;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jclass class = jh_getClass(jni_env, "com/snakei/MediaService");

    jmethodID get_instance = jh_getGetter(jni_env, class, "()Lcom/snakei/MediaService;");
    jobject instance = jh_getInstance(jni_env, class, get_instance);

    jmethodID start_media = jh_getMethod(jni_env, class, "start_media", "()V");

    jmethodID microphone_record = jh_getMethod(jni_env, class, "microphoneRecord", "(Ljava/lang/String;I)V");
    jmethodID tts_speak = jh_getMethod(jni_env, class, "ttsSpeak", "(Ljava/lang/String;)Z");

    m_cached = (struct media_cache){.class = class, .get_instance = get_instance,
            .microphone_record = microphone_record,
            .tts_speak = tts_speak};

    jh_callVoidMethod(jni_env, instance, start_media);

    // XXX: Only detach if AttachCurrentThread wasn't a no-op
    //(*cached_vm)->DetachCurrentThread(cached_vm);
    (*jni_env)->DeleteLocalRef(jni_env, instance);
}

void media_stop_media() {
    JNIEnv* jni_env;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
    jmethodID stop_media = jh_getMethod(jni_env, m_cached.class, "stop_media", "()V");
    jh_callVoidMethod(jni_env, instance, stop_media);

    // XXX: Only detach if AttachCurrentThread wasn't a no-op
    //(*cached_vm)->DetachCurrentThread(cached_vm);
    (*jni_env)->DeleteLocalRef(jni_env, instance);
}


PyObject* media_tts_speak(PyObject *self, PyObject *args) {

//    if (!PyArg_ParseTuple(args, "b", &message)){
//        LOGI("Wrong arguments. I guess I should raise an Exception.");
//        Py_RETURN_NONE;
//    }
//
//    JNIEnv *jni_env;
//    // Use the cached JVM pointer to get a new environment
//    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
//
//    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
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
}
