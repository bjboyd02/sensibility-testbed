//
// Created by lukas on 5/27/16.
//

#ifndef SENSIBILITY_TESTBED_JNIHELPER_H
#define SENSIBILITY_TESTBED_JNIHELPER_H

#include <jni.h>
#include <snakei.h>
#include <stdarg.h>

jobject jnihelper_getInstance(JNIEnv* jni_env, jclass class, const char *class_name);
jclass jnihelper_getClass(JNIEnv* jni_env, const char *class_name);
void jnihelper_callVoidMethod(JNIEnv* jni_env, jclass class, jobject object, const char *method_name, const char *type_signature, ...);

PyObject* jnihelper_callBooleanMethod(JNIEnv* jni_env, jclass class, jobject object, const char *method_name, ...);
#endif //SENSIBILITY_TESTBED_JNIHELPER_H
