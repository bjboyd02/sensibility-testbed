#include "snakei.h"

/* XXX UNUSED!!!
 * Define an array of JNINativeMethod structures to map method names 
 * (as later usable from Java code) and signatures (argument types, 
 * return type) onto C function names.
 *
 * See https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/functions.html#wp17734
 */
static JNINativeMethod methods[] = {
  // XXX UNUSED!!!
  //{"startNativePythonInterpreter", "(Ljava/lang/String;Ljava/lang/String;)V", (void*)&start_native_python_interpreter},
};



/* XXX UNUSED!!! */
int cache_and_register_stuff(JavaVM* vm) {
  jclass python_interpreter_service_class;

  /* Attach the current thread to the Java VM so we can 
   * get the environment for looking up classes. */
  LOGI("Attaching current thread");
  (*vm)->AttachCurrentThread(vm, &jni_environment, NULL);

  // PythonInterpreterService is where we register our C functions
  python_interpreter_service_class = (*jni_environment)->FindClass(jni_environment, "com/snakei/PythonInterpreterService");

  LOGI("Registering natives");
  /* Register the C code's native functions (so as to save us some 
   * name mangling)
   * see https://stackoverflow.com/questions/1010645/what-does-the-registernatives-method-do */
  if ((*jni_environment)->RegisterNatives(jni_environment, python_interpreter_service_class, methods, 
      sizeof(methods)/sizeof(methods[0])) < 0) {
    LOGI("Well, that sort of failed.");
    return -2;
  }

  // From OutputService, we get methods to log/toast/notify
  output_service_class = (*jni_environment)->FindClass(jni_environment, "com/snakei/OutputService");
  log_message = (*jni_environment)->GetMethodID(jni_environment, output_service_class, 
      "logMessage", "(Ljava/lang/String;)V");

  // TODO From SensorService, we get methods to read sensor values

  // That's all!
  return 0;
}




/*
 * This function is called when the Java code does `System.loadLibrary()`.
 * See https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html#JNI_OnLoad
*/
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  /*
  if (cache_and_register_stuff(vm) != 0) {
    return JNI_ERR;
  } else {
  */
    return JNI_VERSION_1_6;
  //}
}


