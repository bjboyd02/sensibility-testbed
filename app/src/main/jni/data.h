/*
 * Created by
 * lukas.puehringer@nyu.edu
 * on 3/10/17
 */

#ifndef SENSIBILITY_TESTBED_DATA_H
#define SENSIBILITY_TESTBED_DATA_H

#include <jni.h>
#include <Python.h>
#include "snakei.h"
#include "jniglue.h"

void data_init_pymodule();
void data_start_data();
void data_stop_data();
void data_init();

PyObject* data_get_most_recent_data(PyObject *self);
PyObject* data_get_all_data(PyObject *self);

#endif //SENSIBILITY_TESTBED_DATA_H
