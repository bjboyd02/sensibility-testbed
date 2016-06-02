//
// Created by lukas on 5/27/16.
//
#include "media.h"

struct media_cache {
    jclass class;
    jmethodID get_instance;
    jmethodID microphone_record;
    jmethodID tts_speak;
    jmethodID is_tts_speaking;
} m_cached;


void media_start_media() {
    JNIEnv* jni_env;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jclass class = jh_getClass(jni_env, "com/snakei/MediaService");

    jmethodID get_instance = jh_getGetter(jni_env, class, "()Lcom/snakei/MediaService;");
    jobject instance = jh_getInstance(jni_env, class, get_instance);

    jmethodID start_media = jh_getMethod(jni_env, class, "start_media", "()V");

    jmethodID microphone_record = jh_getMethod(jni_env, class, "microphoneRecord", "(Ljava/lang/String;I)V");
    jmethodID tts_speak = jh_getMethod(jni_env, class, "ttsSpeak", "(Ljava/lang/String;)I");
    jmethodID is_tts_speaking = jh_getMethod(jni_env, class, "isTtsSpeaking", "()Z");

    m_cached = (struct media_cache){.class = class, .get_instance = get_instance,
            .microphone_record = microphone_record,
            .tts_speak = tts_speak, .is_tts_speaking = is_tts_speaking};

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
    JNIEnv *jni_env;
    char *text;
    jstring java_text;

    if (!PyArg_ParseTuple(args, "s", &text)){
        LOGI("Wrong arguments. I guess I should raise an Exception.");
        Py_RETURN_NONE;
    }
    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Convert C string to Java string
    java_text = (*jni_env)->NewStringUTF(jni_env, text);

    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
    PyObject* success = jh_callIntMethod(jni_env, instance, m_cached.tts_speak, java_text);

    (*jni_env)->DeleteLocalRef(jni_env, instance);
    (*jni_env)->DeleteLocalRef(jni_env, java_text);

    return success;
}

PyObject* media_microphone_record(PyObject *self, PyObject *args) {
    JNIEnv *jni_env;
    char *file_name;
    int duration;
    jstring java_file_name;

    if (!PyArg_ParseTuple(args, "si", &file_name, &duration)){
        LOGI("Wrong arguments. I guess I should raise an Exception.");
        Py_RETURN_NONE;
    }

    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Convert C string to Java string
    java_file_name = (*jni_env)->NewStringUTF(jni_env, file_name);

    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
    jh_callVoidMethod(jni_env, instance, m_cached.microphone_record, java_file_name, (jint) duration);

    (*jni_env)->DeleteLocalRef(jni_env, instance);
    (*jni_env)->DeleteLocalRef(jni_env, java_file_name);
}

PyObject* media_is_media_playing(PyObject *self) {

}
PyObject* media_is_tts_speaking(PyObject *self) {
    JNIEnv *jni_env;
    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
    PyObject* is_speaking  = jh_callBooleanMethod(jni_env, instance, m_cached.is_tts_speaking);
    (*jni_env)->DeleteLocalRef(jni_env, instance);
    return is_speaking;
}
PyObject* media_get_media_play_info(PyObject *self) {

}
