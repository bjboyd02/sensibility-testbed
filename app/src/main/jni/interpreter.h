#ifndef _SNAKEI_INTERPRETER_H_
#define _SNAKEI_INTERPRETER_H_

#include <Python.h>
#include <jni.h>
#include "outputs.h"
#include "sensors.h"

void start_python_interpreter_with_args(JNIEnv* env, jobject instance, jstring str);

#endif /* defined _SNAKEI_INTERPRETER_H_ */

