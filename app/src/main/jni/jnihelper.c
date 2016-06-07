//
// Created by lukas on 5/27/16.
//

#include "jnihelper.h"

jclass jh_getClass(JNIEnv* jni_env, const char *class_name) {
    jclass class;
    class = (*jni_env)->FindClass(jni_env, class_name);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getClass: exception occurred");
    }
    if (class == NULL) {
        LOGI("jh_getClass: returned NULL");
    }
    return class;
}

jmethodID jh_getGetter(JNIEnv* jni_env, jclass class, const char *type_signature) {
    jmethodID getter;
    getter = (*jni_env)->GetStaticMethodID(jni_env, class, "getInstance", type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getGetter: exception occurred");
    }
    if (getter == NULL) {
        LOGI("jh_getGetter: returned NULL");
    }
    return getter;
}

jmethodID jh_getMethod(JNIEnv* jni_env, jclass class, const char *method_name, const char *type_signature) {
    jmethodID method;
    method = (*jni_env)->GetMethodID(jni_env, class, method_name, type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getMethod: exception occurred - %s", method_name);
    }
    if (method == NULL) {
        LOGI("jh_getMethod: returned NULL");
    }
    return method;
}

jobject jh_getInstance(JNIEnv* jni_env, jclass class, jmethodID getter) {
    jobject object;
    object = (*jni_env)->CallStaticObjectMethod(jni_env, class, getter);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getInstance: exception occurred");
    }
    if (object == NULL) {
        LOGI("jg_getInstance: returned NULL");
    }
    return object;
}
void jh_callVoidMethod(JNIEnv* jni_env, jobject object, jmethodID method, ...) {
    va_list args;
    va_start(args, method);
    (*jni_env)->CallVoidMethodV(jni_env, object, method, args);
    va_end(args);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_callVoidMethod: exception occurred");
    }
}

PyObject* jh_callBooleanMethod(JNIEnv* jni_env, jobject object, jmethodID method, ...) {

    va_list args;
    va_start(args, method);
    jboolean success = (*jni_env)->CallBooleanMethodV(jni_env, object, method, args);
    va_end(args);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_callBooleanMethod: exception occurred");
    }

    if (success) {
        Py_RETURN_TRUE;
    } else {
        Py_RETURN_FALSE;
    }
}
PyObject* jh_callIntMethod(JNIEnv* jni_env, jobject object, jmethodID method, ...) {

    va_list args;
    va_start(args, method);
    int retval = (*jni_env)->CallIntMethodV(jni_env, object, method, args);
    va_end(args);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_callIntMethod: exception occurred");
    }
    return Py_BuildValue("i", retval);

}
PyObject* jh_callStringMethod(JNIEnv* jni_env, jobject object, jmethodID method, ...) {

    jstring java_string;
    va_list args;
    va_start(args, method);
    java_string = (*jni_env)->CallObjectMethodV(jni_env, object, method, args);
    va_end(args);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_callStringMethod: exception occurred");
    }
    if(java_string == NULL) {
        LOGI("jh_callStringMethod: returned NULL");
    }

    PyObject *py_string = NULL;

    if (java_string != NULL) {
        // Convert Java string to C char*
        const char *c_string = (*jni_env)->GetStringUTFChars(jni_env, java_string, 0);
        // Convert C char* to Python string
        py_string = PyString_FromString(c_string);
        // Free memory and delete reference
        (*jni_env)->ReleaseStringUTFChars(jni_env, java_string, c_string);
    }
    (*jni_env)->DeleteLocalRef(jni_env, java_string);

    return py_string;
}

PyObject* jh_callJsonStringMethod(JNIEnv* jni_env, jobject object, jmethodID method, ...) {

    jstring java_string;
    va_list args;
    va_start(args, method);
    java_string = (*jni_env)->CallObjectMethodV(jni_env, object, method, args);
    va_end(args);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_callJSONMethod: exception occurred");
    }
    if(java_string == NULL) {
        LOGI("jh_callJSONMethod: returned NULL");
    }

    PyObject *obj = NULL;

    if (java_string != NULL) {
        // Convert Java string to C char*
        const char *c_string = (*jni_env)->GetStringUTFChars(jni_env, java_string, 0);
        // Convert C char* to Python string
        obj = JSON_decode_c(c_string);
        // Free memory and delete reference
        (*jni_env)->ReleaseStringUTFChars(jni_env, java_string, c_string);
    }
    (*jni_env)->DeleteLocalRef(jni_env, java_string);

    return obj;
}