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

JavaVM* cached_vm;
jobject cached_context;
pthread_key_t jni_thread_key;

jclass popen_class;
jmethodID popen_method;

jclass miscinfo_class;
jmethodID miscinfo_getter;
jmethodID miscinfo_method_battery_info;


void jni_initialize(JavaVM *vm);
JNIEnv *jni_get_env(void);
void jni_detach_current_thread(void *env);

jobject jni_createGlobalReference(jobject local_ref);
jobject jni_createStringArray(int argc, char *argv[]);

jclass jh_getClass(const char *class_name);
jmethodID jh_getGetter(jclass class, const char *type_signature);
jmethodID jh_getMethod(jclass class, const char *method_name,
                       const char *type_signature);
jmethodID jh_getStaticMethod(jclass class, const char *method_name,
                             const char *type_signature);
jobject jh_getInstance(jclass class, jmethodID getter);
jstring jh_getJavaString(char *string);
void jh_deleteReference(jobject obj);

PyObject* jh_callVoidMethod(JNIEnv* jni_env, jobject object,
                            jmethodID method, va_list args);
PyObject* jh_callBooleanMethod(JNIEnv* jni_env, jobject object,
                               jmethodID method, va_list args);
PyObject* jh_callIntMethod(JNIEnv* jni_env, jobject object,
                           jmethodID method, va_list args);
PyObject* jh_callStringMethod(JNIEnv* jni_env, jobject object,
                              jmethodID method, va_list args);
PyObject* jh_callJsonStringMethod(JNIEnv* jni_env, jobject object,
                                  jmethodID method, va_list args);
PyObject* jh_call(jclass class, jmethodID get_instance,
                  PyObject* (*_jh_call)(JNIEnv*, jobject, jmethodID, va_list),
                  jmethodID cached_method, ...);

PyObject* jh_callStaticVoid(jclass class, jmethodID cached_method, ...);

#endif //SENSIBILITY_TESTBED_JNIHELPER_H
