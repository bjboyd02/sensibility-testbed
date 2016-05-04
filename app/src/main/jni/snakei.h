#ifndef _SNAKEI_H_
#define _SNAKEI_H_

#include <Python.h>
#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>

#include "interpreter.h"
#include "outputs.h"
#include "sensors.h"
#include "pyhelper.h"



// Define a "convenience" macro for simple logging (see `adb logcat`)
// (taken from the Android NDK samples)
# define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, __FILE__, __VA_ARGS__))



/* Following the suggestions in https://developer.android.com/training/articles/perf-jni.html#native_libraries,
 * we pre-cache the `JNIEnv` and classes that we will use in C code.
 * First, declare a few references we will populate soon and then reuse 
 * in the course of our runtime.
 */
JavaVM* cached_vm;
JNIEnv* cached_jni_env;
jclass cached_output_service_class;
jmethodID cached_log_message;



#endif /* defined _SNAKEI_H_ */

