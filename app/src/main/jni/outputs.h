#ifndef _SNAKEI_OUTPUTS_H_
#define _SNAKEI_OUTPUTS_H_

#include <jni.h>
#include <Python.h>
#include "snakei.h"
#include "jniglue.h"

// Log messages of no more than this many characters
#define MAX_MESSAGE_LENGTH 1024

void androidlog_init_pymodule();
PyObject* androidlog_log(PyObject *self, PyObject *python_message);
PyObject* androidlog_toast(PyObject *self, PyObject *python_message);
PyObject* androidlog_prompt(PyObject *self, PyObject *python_message);

#endif /* defined _SNAKEI_OUTPUTS_H_ */

