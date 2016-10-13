/*
 * Created by
 * albert.rafetseder@univie.ac.at
 * lukas.puehringer@nyu.edu
 * on 3/29/16
 *
 * C Python module uses OutputService.java via JNI to log messages
 * from Python to the Android log
 *
 * This involves conversion of that message from Python to C, and
 * then to Java.
 *
 * Note:
 * Btw. this is currently the only C Python module in snakei that doesn't use
 * Java Singletons
 * There is no need to provide a global state to
 * subsequent calls to this method, like in sensor polling methods, where a
 * listener needs to stay registered as long as the sensor is polled natively
 *
 */


#include "outputs.h"

/*
 * Logs a message from Python to Android Log
 */
PyObject* androidlog_log(PyObject *self, PyObject *python_message) {
  char* c_message;

  // Convert Python string to C string
  c_message = PyString_AsString(python_message);

  LOGI("NATIVE LOG: %s", c_message);

  Py_RETURN_NONE;
}


/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidLogMethods[] = {
        {"log", androidlog_log, METH_O,  "Write to Android Log."},
        {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


/*
 * Initializes Python module (androidlog)
 *
 * Note:
 * If we wanted to build the module as .so or .dll we could
 * would have to change the signature to
 * PyMODINIT_FUNC initandroidlog(void)
 *
 */
void initandroidlog() {
  Py_InitModule("androidlog", AndroidLogMethods);
}