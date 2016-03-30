#ifndef _SNAKEI_INTERPRETER_H_
#define _SNAKEI_INTERPRETER_H_

#include <Python.h>
#include <jni.h>
#include "outputs.h"
#include "sensors.h"

void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(JNIEnv* env, jobject instance, jstring python_environment, jstring python_arguments);

#endif /* defined _SNAKEI_INTERPRETER_H_ */

