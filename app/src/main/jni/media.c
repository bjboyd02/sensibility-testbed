/*
 * Created by lukas.puehringer@nyu.edu
 * on 5/27/16.
 *
 * C Python module uses MediaService.java via JNI to return record from
 * microphone and synthesize Text-To-Speech
 *
 * Usage:
 * Module initialization - call initmedia() from C
 *  - Initializes Python module (media)
 *
 * Media initialization - call media_start_media from C
 *  - Calls start_media Java method to initialize Text-To-Speech engine
 *
 * Perform media tasks - call media.* from Python
 *  - Calls the according method in Java
 *  - Return values are documented in MediaService.java
 *
 * Media de-initialization - call media_stop_media from C
 *  - Calls stop_media Java method to release Text-To-Speech engine
 *
 *  Note:
 *  Cf. sensors.c notes
 *
 */
#include "media.h"

/*
 * Cf. start_media() in MediaService.java for details
 *
 * Not in Python module - needs to be called from C!
 *
 */
void media_start_media() {
    jni_py_call(_void,
            cached_media_class, cached_media_get_instance,
            cached_media_start_media);
}


/*
 * Cf. stop_media() in MediaService.java for details
 *
 * Not in Python module - needs to be called from C!
 *
 */
void media_stop_media() {
    jni_py_call(_void,
            cached_media_class, cached_media_get_instance,
            cached_media_stop_media);
}


/*
 * Cf. ttsSpeak() in MediaService.java for details
 *
 * Arguments
 *   message to synthesize (string)
 */
PyObject* media_tts_speak(PyObject *self, PyObject *args) {
    char *text;
    jstring java_text;

    if (!PyArg_ParseTuple(args, "s", &text)){
        LOGI("Wrong arguments. I guess I should raise an Exception.");
        Py_RETURN_NONE;
    }
    java_text = jni_get_string(text);
    PyObject* result = jni_py_call(_void,
            cached_media_class, cached_media_get_instance,
            cached_media_tts_speak, java_text);

    jni_delete_reference((jobject) java_text);
    return result;
}


/*
 * Cf. microphoneRecord() in MediaService.java for details
 *
 * Arguments
 *   path to file where microphone should record to (string)
 *   duration of recording (int)
 */
PyObject* media_microphone_record(PyObject *self, PyObject *args) {
    char *file_name;
    int duration;

    if (!PyArg_ParseTuple(args, "si", &file_name, &duration)){
        LOGI("Wrong arguments. I guess I should raise an Exception.");
        Py_RETURN_NONE;
    }

    jstring java_file_name = jni_get_string(file_name);
    PyObject* result = jni_py_call(_void,
            cached_media_class, cached_media_get_instance,
            cached_media_microphone_record,
            java_file_name, (jint) duration);

    jni_delete_reference((jobject) file_name);
    return result;
}


/*
 * Cf. isMediaPlaying() in MediaService.java for details
 */
PyObject* media_is_media_playing(PyObject *self) {
    return jni_py_call(_boolean,
            cached_media_class, cached_media_get_instance,
            cached_media_is_media_playing);
}


/*
 * Cf. isTtsSpeaking() in MediaService.java for details
 */
PyObject* media_is_tts_speaking(PyObject *self) {
    return jni_py_call(_boolean,
        cached_media_class, cached_media_get_instance,
        cached_media_is_tts_speaking);
}


/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidMediaMethods[] = {
        {"tts_speak", (PyCFunction) media_tts_speak, METH_VARARGS,
         "Synthesize Text-to-speech"},
        {"microphone_record",
          (PyCFunction) media_microphone_record, METH_VARARGS,
         "Record audio to specified file for specified seconds "},
        {"is_tts_speaking", (PyCFunction) media_is_tts_speaking, METH_NOARGS,
         "True if TTS is speaking or about to speak, False otherwise"},
        {"is_media_playing", (PyCFunction) media_is_media_playing, METH_NOARGS,
         "True if media is playing, False otherwise"},
        {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


/*
 * Initializes Python module (media),
 *
 * Note:
 * If we wanted to build the module as .so or .dll we could
 * would have to change the signature to
 * PyMODINIT_FUNC initmedia(void)
 *
 */
void initmedia() {
    Py_InitModule("media", AndroidMediaMethods);
}