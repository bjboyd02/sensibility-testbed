#ifndef _SNAKEI_SENSORS_H_
#define _SNAKEI_SENSORS_H_

#include <Python.h>
#include <jni.h>
#include "snakei.h"

void c_sensor_list();

PyObject* sensor_get_sensor_list(PyObject *self);
PyObject* sensor_start_sensing(PyObject *self);
PyObject* sensor_stop_sensing(PyObject *self);
PyObject* sensor_get_acceleration(PyObject *self);

#endif /* defined _SNAKEI_SENSORS_H_ */

