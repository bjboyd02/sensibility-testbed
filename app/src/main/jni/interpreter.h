#ifndef _SNAKEI_INTERPRETER_H_
#define _SNAKEI_INTERPRETER_H_

#include <Python.h>
#include <jni.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include "outputs.h"
#include "sensors.h"
#include "pyhelper.h"

void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(JNIEnv* env, jobject instance, jstring python_home, jstring python_path, jstring python_script, jstring python_files, jstring python_arguments);
#endif /* defined _SNAKEI_INTERPRETER_H_ */

