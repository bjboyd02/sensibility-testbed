#ifndef _SNAKEI_SENSORS_H_
#define _SNAKEI_SENSORS_H_

#include <Python.h>
#include <jni.h>
#include "snakei.h"

void c_sensor_list();

PyObject* sensor_get_sensor_list(PyObject *self);

#endif /* defined _SNAKEI_SENSORS_H_ */

