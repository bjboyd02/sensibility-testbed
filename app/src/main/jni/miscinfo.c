//
// Created by lukas on 6/3/16.
//

#include "miscinfo.h"

PyObject* miscinfo_jsontest(PyObject *self) {
    char **jsonstring = "[{\"lastName\": \"Doe\", \"age\": 45, \"somethingfloat\": 1.55, \"firstName\": \"John\"}, {\"lastName\": \"Smith\", \"age\": 18, \"somethingfloat\": 0.5, \"firstName\": \"Anna\"}]";
    PyObject *obj = JSON_decode_c(jsonstring);
    return obj;
}