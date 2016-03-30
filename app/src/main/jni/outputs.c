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

  LOGI("Use the cached JVM pointer to get a new environment");
  (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
  
  LOGI("Find Java class");
  output_service_class = (*jni_env)->FindClass(jni_env, "com/snakei/OutputService");

  LOGI("Find the Java method we're gonna call");
  log_message = (*jni_env)->GetStaticMethodID(jni_env, 
      output_service_class, "logMessage", "(Ljava/lang/String;)V");

  LOGI("Convert to C string");
  c_message = PyString_AsString(python_message);
  LOGI("Convert to Java string with env %p", jni_env);
  java_message = (*jni_env)->NewStringUTF(jni_env, c_message);

  LOGI("Call output method");
  (*jni_env)->CallStaticVoidMethod(jni_env, output_service_class, 
      log_message, java_message);

  LOGI("I'm done!");
  Py_RETURN_NONE;  // I.e., `return Py_None;` with reference counting
}

