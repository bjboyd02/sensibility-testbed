//
// Created by lukas on 5/27/16.
//

#include "jnihelper.h"

jclass jnihelper_getClass(JNIEnv* jni_env, const char *class_name) {
    jclass class;
    class = (*jni_env)->FindClass(jni_env, class_name);
    if (class == NULL) {
        LOGI("getClass returned NULL");
    }
    return class;
}

jobject jnihelper_getInstance(JNIEnv* jni_env, jclass class, const char *class_name) {
    jmethodID getter;
    jobject object;

    // class_name e.g. com/snakei/SensorService
    char type_signature[strlen(class_name) + 3];
    sprintf(type_signature, "()L%s;", class_name);
    // signature e.g. "()Lcom/snakei/SensorService;"
    getter = (*jni_env)->GetStaticMethodID(jni_env, class, "getInstance", type_signature);
    object = (*jni_env)->CallStaticObjectMethod(jni_env, class, getter);
    if (object == NULL) {
        LOGI("getInstance() returned NULL");
    }
    return object;
}
void jnihelper_callVoidMethod(JNIEnv* jni_env, jclass class, jobject object, const char *method_name, const char *type_signature, ...) {
    jmethodID method = (*jni_env)->GetMethodID(jni_env, class, method_name, type_signature);

    va_list args;
    va_start(args, method);
    (*jni_env)->CallVoidMethod(jni_env, object, method, args);
    va_end(args);
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