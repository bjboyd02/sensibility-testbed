/*
 * Created by
 * lukas.puehringer@nyu.edu
 * on 3/10/17
 *
 * C Python module uses DataService.java via JNI to query data sent via URI click
 *
 */
#include "data.h"


void data_init() {
    jni_py_call(_void,
                cached_data_class, cached_data_get_instance,
                cached_data_init, cached_context);
}

void data_start_data() {
    jni_py_call(_void,
                cached_data_class, cached_data_get_instance,
                cached_data_start_data);
}

void data_stop_data() {
    jni_py_call(_void,
                cached_data_class, cached_data_get_instance,
                cached_data_stop_data);
}

PyObject* data_get_most_recent_data(PyObject *self) {
    jni_py_call(_json,
                cached_data_class, cached_data_get_instance,
                cached_data_get_most_recent_data);
}

PyObject* data_get_all_data(PyObject *self) {
    jni_py_call(_json,
                cached_data_class, cached_data_get_instance,
                cached_data_get_all_data);
}

/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidDataMethods[] = {
    {"get_data", (PyCFunction) data_get_most_recent_data, METH_NOARGS,
            "Get most recent data sent via clicked link."},
    {"get_all_data", (PyCFunction) data_get_all_data, METH_NOARGS,
            "Get all data sent via clicked link."},
};

/*
 * Initializes Python module (androiddata)
 *
 * Note:
 * If we wanted to build the module as .so or .dll we could
 * would have to change the signature to
 * PyMODINIT_FUNC data_init_pymodule(void)
 *
 */
void data_init_pymodule() {
    Py_InitModule("androiddata", AndroidDataMethods);
}