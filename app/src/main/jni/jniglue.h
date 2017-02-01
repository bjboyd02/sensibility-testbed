//
// Created by lukas on 5/27/16.
//

#ifndef SENSIBILITY_TESTBED_JNIHELPER_H
#define SENSIBILITY_TESTBED_JNIHELPER_H

#include <jni.h>
#include <stdarg.h>
#include <stddef.h>
#include <pthread.h>
#include <Python.h>
#include "snakei.h"
#include "cjson.h"

/* Environment and thread functions */

JNIEnv *jni_get_env(void);
void jni_detach_current_thread(void *env);

jobject jni_get_global_reference(jobject local_ref);
void jni_delete_reference(jobject obj);
void jni_delete_global_reference(jobject obj);

jstring jni_get_string(char *string);
jobject jni_get_string_array(int argc, char *argv[]);


jclass jni_find_class(const char *class_name);
jclass jni_find_class_as_global(const char *class_name);
jmethodID jni_find_getter(jclass class, const char *type_signature);
jmethodID jni_find_method(
        jclass class, const char *method_name, const char *type_signature);
jmethodID jni_find_static_method(
        jclass class, const char *method_name, const char *type_signature);
jobject jni_get_instance(jclass class, jmethodID getter);


/* JNI Python helpers - call Java return python */

PyObject* _void(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args);
PyObject* _boolean(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args);
PyObject* _int(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args);
PyObject* _string(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args);
PyObject* _json(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args);

PyObject* jni_py_call(
        PyObject* (*_jni_py_call)(JNIEnv*, jobject, jmethodID, va_list),
        jclass class, jmethodID get_instance, jmethodID cached_method, ...);

PyObject* jni_py_call_static_void(jclass class, jmethodID cached_method, ...);
PyObject* jni_py_call_static_boolean(jclass class, jmethodID cached_method, ...);

#endif //SENSIBILITY_TESTBED_JNIHELPER_H
