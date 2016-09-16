#ifndef _SNAKEI_INTERPRETER_H_
#define _SNAKEI_INTERPRETER_H_

#include <jni.h>
#include <errno.h>
#include <string.h>
#include <sys/prctl.h>
#include <sys/wait.h>
#include <Python.h>
#include "snakei.h"
#include "outputs.h"
#include "sensors.h"
#include "location.h"
#include "media.h"
#include "pyhelper.h"
#include "miscinfo.h"
#include "popen.h"

void interpreter_init(char* home, char* path);
void interpreter_run(int argc, char **argv);

#endif /* defined _SNAKEI_INTERPRETER_H_ */

