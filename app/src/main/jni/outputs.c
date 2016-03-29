#include "outputs.h"

/*
 * Log a message from Python through JNI.
 *
 * This involves conversion of that message from Python to C, and 
 * then to Java.
 */
PyObject* androidlog_log2(PyObject *self, PyObject *python_message)
{
  char* c_message;
  jstring java_message;

  LOGI("Convert to C string");
  c_message = PyString_AsString(python_message);
  LOGI("Convert to JAva string");
  java_message = (*jni_environment)->NewStringUTF(jni_environment, c_message);

  (*jni_environment)->CallVoidMethod(jni_environment, output_service_class, java_message);

  Py_RETURN_NONE;  // I.e., `return Py_None;` with reference counting
}

