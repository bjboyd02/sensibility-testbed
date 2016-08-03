#ifndef _SNAKEI_INTERPRETER_H_
#define _SNAKEI_INTERPRETER_H_

#include <Python.h>
#include <jni.h>
#include <errno.h>
#include <string.h>
#include <sys/prctl.h>

#include "snakei.h"
#include "outputs.h"
#include "sensors.h"
#include "location.h"
#include "media.h"
#include "pyhelper.h"
#include "miscinfo.h"
#include "pythonrun.h"


void Java_com_snakei_PythonInterpreter_runScript(
    JNIEnv* env, jobject instance, jobjectArray j_args,
    jstring j_home, jstring j_path);

void interpreter_init(char* home, char* path);
void interpreter_run(int argc, char **argv);

#endif /* defined _SNAKEI_INTERPRETER_H_ */

