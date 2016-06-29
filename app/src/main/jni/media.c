//
// Created by lukas on 5/27/16.
//
#include "media.h"

static struct media_cache {
    jclass class;
    jmethodID get_instance;
    jmethodID stop_media;
    jmethodID start_media;
    jmethodID microphone_record;
    jmethodID tts_speak;
    jmethodID is_tts_speaking;
    jmethodID is_media_playing;
} m_cached;

void media_start_media() {
    jh_call(m_cached.class, m_cached.get_instance, jh_callVoidMethod, m_cached.start_media);
}

void media_stop_media() {
    jh_call(m_cached.class, m_cached.get_instance, jh_callVoidMethod, m_cached.stop_media);
}

PyObject* media_tts_speak(PyObject *self, PyObject *args) {
    char *text;
    jstring java_text;

    if (!PyArg_ParseTuple(args, "s", &text)){
        LOGI("Wrong arguments. I guess I should raise an Exception.");
        Py_RETURN_NONE;
    }
    java_text = jh_getJavaString(text);
    PyObject* result = jh_call(m_cached.class, m_cached.get_instance, jh_callVoidMethod,
                                m_cached.tts_speak, java_text);

    jh_deleteReference((jobject) java_text);
    return result;
}

PyObject* media_microphone_record(PyObject *self, PyObject *args) {
    char *file_name;
    int duration;

    if (!PyArg_ParseTuple(args, "si", &file_name, &duration)){
        LOGI("Wrong arguments. I guess I should raise an Exception.");
        Py_RETURN_NONE;
    }

    jstring java_file_name = jh_getJavaString(file_name);
    PyObject* result = jh_call(m_cached.class, m_cached.get_instance,
                      jh_callVoidMethod, m_cached.microphone_record,
                      java_file_name, (jint) duration);

    jh_deleteReference((jobject) file_name);
    return result;
}

PyObject* media_is_media_playing(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance,
                   jh_callBooleanMethod, m_cached.is_media_playing);
}

PyObject* media_is_tts_speaking(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance,
                   jh_callBooleanMethod, m_cached.is_tts_speaking);
}

static PyMethodDef AndroidMediaMethods[] = {
        {"tts_speak", (PyCFunction) media_tts_speak, METH_VARARGS,
                        "Text-to-speech"},
        {"microphone_record", (PyCFunction) media_microphone_record, METH_VARARGS,
                        "Record audio"},
        {"is_tts_speaking", (PyCFunction) media_is_tts_speaking, METH_NOARGS,
                        "Returns true if TTS is currently speaking or about to speak, false otherwise."},
        {"is_media_playing", (PyCFunction) media_is_media_playing, METH_NOARGS,
                        "Returns true if TTS is currently speaking or about to speak, false otherwise."},
        {NULL, NULL, 0, NULL} // This is the end-of-array marker
};

//PyMODINIT_FUNC initmedia(void) {
void initmedia() {
    Py_InitModule("media", AndroidMediaMethods);
    jclass class = jh_getClass( "com/snakei/MediaService");

    m_cached = (struct media_cache){
            .class = class,
            .start_media = jh_getMethod(class, "start_media", "()V"),
            .stop_media = jh_getMethod(class, "stop_media", "()V"),
            .get_instance = jh_getGetter(class, "()Lcom/snakei/MediaService;"),
            .microphone_record = jh_getMethod(class, "microphoneRecord",
                                              "(Ljava/lang/String;I)V"),
            .tts_speak = jh_getMethod(class,"ttsSpeak",
                                      "(Ljava/lang/String;)V"),
            .is_tts_speaking = jh_getMethod(class, "isTtsSpeaking", "()Z"),
            .is_media_playing = jh_getMethod(class, "isMediaPlaying", "()Z")};
}