//
// Created by lukas on 5/27/16.
//

#include "jnihelper.h"
#include "python2.7/pyerrors.h"

jclass jh_getClass(const char *class_name) {
    JNIEnv *jni_env;
    jclass class;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    class = (*jni_env)->FindClass(jni_env, class_name);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getClass: exception occurred");
    }
    if (class == NULL) {
        LOGI("jh_getClass: returned NULL");
    }
    return class;
}

jmethodID jh_getGetter(jclass class, const char *type_signature) {
    JNIEnv *jni_env;
    jmethodID getter;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    getter = (*jni_env)->GetStaticMethodID(jni_env, class, "getInstance", type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getGetter: exception occurred");
    }
    if (getter == NULL) {
        LOGI("jh_getGetter: returned NULL");
    }
    return getter;
}

jmethodID jh_getMethod(jclass class, const char *method_name, const char *type_signature) {
    JNIEnv *jni_env;
    jmethodID method;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    method = (*jni_env)->GetMethodID(jni_env, class, method_name, type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getMethod: exception occurred - %s", method_name);
    }
    if (method == NULL) {
        LOGI("jh_getMethod: returned NULL");
    }
    return method;
}

jmethodID jh_getStaticMethod(jclass class, const char *method_name, const char *type_signature) {
    JNIEnv *jni_env;
    jmethodID method;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    method = (*jni_env)->GetStaticMethodID(jni_env, class, method_name, type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getStaticMethod: exception occurred - %s", method_name);
    }
    if (method == NULL) {
        LOGI("jh_getStaticMethod: returned NULL");
    }
    return method;
}


jobject jh_getInstance(jclass class, jmethodID getter) {
    JNIEnv *jni_env;
    jobject object;

    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    object = (*jni_env)->CallStaticObjectMethod(jni_env, class, getter);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jh_getInstance: exception occurred");
    }
    if (object == NULL) {
        LOGI("jg_getInstance: returned NULL");
    }
    return object;
}

jstring jh_getJavaString(char *string) {
    JNIEnv *jni_env;
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    return (*jni_env)->NewStringUTF(jni_env, string);
}

void jh_deleteReference(jobject obj) {
    JNIEnv *jni_env;
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    (*jni_env)->DeleteLocalRef(jni_env, obj);
}

int _handle_errors(JNIEnv* jni_env, const char *where) {
    jthrowable error = (*jni_env)->ExceptionOccurred(jni_env);
    if (error) {
        LOGI("%s", where);
        (*jni_env)->ExceptionClear(jni_env);

        // Maybe cache java/lang/Object and toString
        jclass class = (*jni_env)->FindClass(jni_env, "java/lang/Object");
        jmethodID method = (*jni_env)->GetMethodID(jni_env, class,
                                                   "toString", "()Ljava/lang/String;");
        jstring error_msg_java = (*jni_env)->CallObjectMethod(jni_env, error, method);
        const char *error_msg = (*jni_env)->GetStringUTFChars(jni_env, error_msg_java, 0);
        PyErr_SetString(PyExc_Exception, error_msg);
        (*jni_env)->ReleaseStringUTFChars(jni_env, error_msg_java, error_msg);
        (*jni_env)->DeleteLocalRef(jni_env, error_msg_java);
        (*jni_env)->DeleteLocalRef(jni_env, class);
        return 1;
    }
    return 0;
}


/*
 * #######################################################
 * Don't call the subsequent functions directly
 * Use the jh_call() wrapper
 * #######################################################
 */



PyObject* jh_callVoidMethod(JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {

    (*jni_env)->CallVoidMethodV(jni_env, object, method, args);
    if (_handle_errors(jni_env, "jh_callVoidMethod: exception occurred")) {
        return NULL;
    }

    Py_RETURN_NONE;
}

PyObject* jh_callBooleanMethod(JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {

    jboolean success = (*jni_env)->CallBooleanMethodV(jni_env, object, method, args);

    if (_handle_errors(jni_env, "jh_callBooleanMethod: exception occurred")) {
        return NULL;
    }

    if (success) {
        Py_RETURN_TRUE;
    } else {
        Py_RETURN_FALSE;
    }
}

PyObject* jh_callIntMethod(JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {
    int retval = (*jni_env)->CallIntMethodV(jni_env, object, method, args);

    if (_handle_errors(jni_env, "jh_callIntMethod: exception occurred")) {
        return NULL;
    }

    return Py_BuildValue("i", retval);
}
PyObject* jh_callStringMethod(JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {

    jstring java_string;
    java_string = (*jni_env)->CallObjectMethodV(jni_env, object, method, args);

    if (_handle_errors(jni_env, "jh_callStringMethod: exception occurred")) {
        return NULL;
    }

    if(java_string == NULL) {
        LOGI("jh_callStringMethod: returned NULL");
        Py_RETURN_NONE;
    }

    PyObject *py_string = NULL;
    // Convert Java string to C char*
    const char *c_string = (*jni_env)->GetStringUTFChars(jni_env, java_string, 0);
    // Convert C char* to Python string
    py_string = PyString_FromString(c_string);
    // Free memory and delete reference
    (*jni_env)->ReleaseStringUTFChars(jni_env, java_string, c_string);
    (*jni_env)->DeleteLocalRef(jni_env, java_string);

    return py_string;
}

PyObject* jh_callJsonStringMethod(JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {

    jstring java_string;
    java_string = (*jni_env)->CallObjectMethodV(jni_env, object, method, args);

    if (_handle_errors(jni_env, "jh_callJsonStringMethod: exception occurred")) {
        return NULL;
    }

    if(java_string == NULL) {
        LOGI("jh_callJsonStringMethod: returned NULL");
        Py_RETURN_NONE;
    }

    PyObject *obj = NULL;
    // Convert Java string to C const char*
    const char *c_string_const = (*jni_env)->GetStringUTFChars(jni_env, java_string, 0);

    // Convert Java string to char*
    char *c_string = malloc(strlen(c_string_const) + 1);
    strcpy(c_string, c_string_const);

    // Convert C char* to Python string
    obj = JSON_decode_c(c_string);

    free(c_string);
    // Free memory and delete reference
    (*jni_env)->ReleaseStringUTFChars(jni_env, java_string, c_string_const);
    (*jni_env)->DeleteLocalRef(jni_env, java_string);

    return obj;
}


PyObject* jh_call(jclass class, jmethodID get_instance,
                  PyObject* (*jh_call)(JNIEnv*, jobject, jmethodID, va_list),
                  jmethodID cached_method, ...) {


    JNIEnv *jni_env;
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Get the instance
    jobject instance = jh_getInstance(class, get_instance);
    // Call the JNI function in
    PyObject* result;
    va_list args;
    va_start(args, cached_method);
    result = (*jh_call)(jni_env, instance, cached_method, args);
    va_end(args);
    (*jni_env)->DeleteLocalRef(jni_env, instance);

    return result;
}


PyObject* jh_callStaticVoid(jclass class, jmethodID cached_method, ...) {

    JNIEnv *jni_env;
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    va_list args;
    va_start(args, cached_method);
    (*jni_env)->CallStaticVoidMethodV(jni_env, class,
                                     cached_method, args);
    va_end(args);

    if (_handle_errors(jni_env, "jh_callStaticVoid: exception occurred")) {
        return NULL;
    }

    Py_RETURN_NONE;
}


