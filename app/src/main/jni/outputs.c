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

  JNIEnv* jni_env;
  jclass output_service_class;
  jmethodID log_message;

  // Use the cached JVM pointer to get a new environment
  (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
  
  // Find Java class
  output_service_class = (*jni_env)->FindClass(jni_env, "com/snakei/OutputService");

  // Find the Java method we want to call
  log_message = (*jni_env)->GetStaticMethodID(jni_env, 
      output_service_class, "logMessage", "(Ljava/lang/String;)V");

  // Convert Python string to C string
  c_message = PyString_AsString(python_message);
  // Convert C string to Java string
  java_message = (*jni_env)->NewStringUTF(jni_env, c_message);

  // Call output method
  (*jni_env)->CallStaticVoidMethod(jni_env, output_service_class, 
      log_message, java_message);

  Py_RETURN_NONE;  // I.e., `return Py_None;` with reference counting
}

