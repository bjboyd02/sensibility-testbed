#ifndef _SNAKEI_OUTPUTS_H_
#define _SNAKEI_OUTPUTS_H_

#include <Python.h>
#include <jni.h>
#include "snakei.h"

// Log messages of no more than this many characters
#define MAX_MESSAGE_LENGTH 1024

PyObject* androidlog_log2(PyObject *self, PyObject *python_message);

#endif /* defined _SNAKEI_OUTPUTS_H_ */

