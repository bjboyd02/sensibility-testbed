//
// Created by lukas on 7/25/16.
//

#ifndef SENSIBILITY_TESTBED_PYTHONRUN_H
#define SENSIBILITY_TESTBED_PYTHONRUN_H

#include <Python.h>
#include <jni.h>
#include "snakei.h"
#include "jnihelper.h"

PyObject* android_run(PyObject *self, PyObject *python_script);

#endif //SENSIBILITY_TESTBED_PYTHONRUN_H
