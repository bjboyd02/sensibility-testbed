//
// Created by lukas on 6/3/16.
//

#ifndef SENSIBILITY_TESTBED_MISCINFO_H
#define SENSIBILITY_TESTBED_MISCINFO_H
#include <Python.h>
#include <jni.h>
#include "snakei.h"
#include "cjson.h"
#include "jnihelper.h"

void miscinfo_start_miscinfo();
PyObject* miscinfo_get_battery_info(PyObject *self);
PyObject* miscinfo_is_wifi_enabled(PyObject *self);
PyObject* miscinfo_get_wifi_state(PyObject *self);
PyObject* miscinfo_get_wifi_connection_info(PyObject *self);
PyObject* miscinfo_get_wifi_scan_info(PyObject *self);


#endif //SENSIBILITY_TESTBED_MISCINFO_H
