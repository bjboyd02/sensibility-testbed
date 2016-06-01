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
//
//PyObject* jnihelper_callBooleanMethod(JNIEnv* jni_env, jclass class, jobject object, const char *method_name) {
//    jmethodID method = (*jni_env)->GetMethodID(jni_env, class, method_name, "()V");
//
//    va_list args;
//    va_start(args, method);
//    jboolean success = (*jni_env)->CallBooleanMethod(jni_env, object, method, args);
//    va_end(args);
//
//    if (success) {
//        Py_RETURN_TRUE;
//    } else {
//        Py_RETURN_FALSE;
//    }
//}