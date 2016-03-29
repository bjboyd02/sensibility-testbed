#include "snakei.h"

// Define a "convenience" macro for simple logging (see `adb logcat`)
// (taken from the Android NDK samples)
# define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, __FILE__, __VA_ARGS__))



/*
 * Define an array of JNINativeMethod structures to map method names 
 * (as later usable from Java code) and signatures (argument types, 
 * return type) onto C function names.
 *
 * See https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/functions.html#wp17734
 */
static JNINativeMethod methods[] = {
  {"startPythonInterpreterWithArgs", "java/lang/String()", (void*)&start_python_interpreter_with_args},
};




int cache_and_register_stuff(JavaVM* vm) {
  jclass python_interpreter_service_class;

  LOGI("Getting JNIEnv so as to look up classes");
  /* Get the environment for looking up classes, signal an error 
   * to our caller if that doesn't work. */
  if ((*vm)->GetEnv(vm, (void**) jni_environment, JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }

  // PythonInterpreterService is where we register our C functions
  python_interpreter_service_class = (*jni_environment)->FindClass(jni_environment, "com/snakei/PythonInterpreterService");
  LOGI("Registering natives");
  /* Register the C code's native functions (so as to save us some 
   * name mangling)
   * see https://stackoverflow.com/questions/1010645/what-does-the-registernatives-method-do */
  if ((*jni_environment)->RegisterNatives(jni_environment, python_interpreter_service_class, methods, 
      sizeof(methods)/sizeof(methods[0])) < 0) {
    return -2;
  }

  // From OutputService, we get methods to log/toast/notify
  output_service_class = (*jni_environment)->FindClass(jni_environment, "com/snakei/OutputService");
  log_message = (*jni_environment)->GetMethodID(jni_environment, output_service_class, 
      "logMessage", "java/lang/String()");

  // TODO From SensorService, we get methods to read sensor values

  // That's all!
  return 0;
}




/*
 * This function is called when the Java code does `System.loadLibrary()`.
 * See https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html#JNI_OnLoad
*/
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  if (cache_and_register_stuff(vm) != 0) {
    return -1;
  } else {
    return JNI_VERSION_1_6;
  }
}


