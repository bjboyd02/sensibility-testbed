//
// Created by lukas on 7/25/16.
//

#ifndef SNAKEI_POPEN_H
#define SNAKEI_POPEN_H

#include <jni.h>
#include <Python.h>
#include "snakei.h"
#include "jnihelper.h"

void initandroid();
PyObject* android_popen_python(PyObject *self, PyObject *args);

#endif //SNAKEI_POPEN_H
