/*
 * Created by
 * albert.rafetseder@univie.ac.at
 * lukas.puehringer@nyu.edu
 * on 3/29/16
 *
 * C Python module uses OutputService.java via JNI to send messages to UI (Toast) and prompts
 * for user input.
 * This involves conversion of that message from Python to C, and then to Java.
 *
 *
 */


#include "outputs.h"


/*
 * Logs a message to Android Log
 */
PyObject* androidlog_log(PyObject *self, PyObject *python_message) {
  char* c_message;

  // Convert Python string to C string
  c_message = PyString_AsString(python_message);
  LOGI("%s", c_message);

  Py_RETURN_NONE;
}

/*
 * Logs a message as Android Toast (UI)
 */
PyObject* androidlog_toast(PyObject *self, PyObject *python_message) {
  char* c_message;
  jstring java_message;

  c_message = PyString_AsString(python_message);
  java_message = jni_get_string(c_message);

  jni_py_call_static_void(
    cached_output_class, cached_output_toast, cached_context, java_message);

  jni_delete_reference((jobject) java_message);

  Py_RETURN_NONE;
}

/*
 * Prompt for user input (yes/no) using a Toast (UI)
 */
PyObject* androidlog_prompt(PyObject *self, PyObject *python_message) {
  char* c_message;
  jstring java_message;
  PyObject* retval;

  c_message = PyString_AsString(python_message);
  java_message = jni_get_string(c_message);

  retval = jni_py_call_static_boolean(cached_output_class, cached_output_prompt,
    cached_context, java_message);

  jni_delete_reference((jobject) java_message);

  return retval;
}


/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidLogMethods[] = {
        {"log", androidlog_log, METH_O,  "Write to Android Log."},
        {"toast", androidlog_toast, METH_O,  "Show Android Toast."},
        {"prompt", androidlog_prompt, METH_O,  "Prompt for Yes/No input."},
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
void androidlog_init_pymodule() {
  Py_InitModule("androidlog", AndroidLogMethods);
}