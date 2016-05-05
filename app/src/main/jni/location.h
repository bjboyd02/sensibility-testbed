//
// Created by lukas on 5/4/16.
//

#ifndef _SNAKEI_location_H_
#define _SNAKEI_location_H_

#include <Python.h>
#include <jni.h>
#include "snakei.h"

void location_start_location();
void location_stop_location();
PyObject* location_get_location();

#endif //_SNAKEI_location_H_
