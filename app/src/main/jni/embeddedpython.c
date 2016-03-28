#include <Python.h>
#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>

// Define a "convenience" macro for simple logging (see `adb logcat`)
// (taken from the Android NDK samples)
// TODO Can be removed once logging through JNI works.
# define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, __FILE__, __VA_ARGS__))

void start_python_interpreter_with_args(jstring str) {
    // XXX This obviously isn't an implementation.
    // XXX It's just here so that the code compiles (for now)
    if (1) {};
};

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


/* Following the suggestions in https://developer.android.com/training/articles/perf-jni.html#native_libraries,
 * we pre-cache the `JNIEnv` and classes that we will use in C code.
 * First, declare a few references we will populate soon and then reuse 
 * in the course of our runtime.
 */
JNIEnv* env;
jclass output_service_class;
jmethodID log_message;

int cache_and_register_stuff(JavaVM* vm) {
    jclass python_interpreter_service_class;

    LOGI("Getting JNIEnv so as to look up classes");
    /* Get the environment for looking up classes, signal an error 
     * to our caller if that doesn't work. */
    if ((*vm)->GetEnv(vm, (void**) env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    // PythonInterpreterService is where we register our C functions
    python_interpreter_service_class = (*env)->FindClass(env, "com/snakei/PythonInterpreterService");
    LOGI("Registering natives");
    /* Register the C code's native functions (so as to save us some 
     * name mangling)
     * see https://stackoverflow.com/questions/1010645/what-does-the-registernatives-method-do */
    if ((*env)->RegisterNatives(env, python_interpreter_service_class, methods, 
            sizeof(methods)/sizeof(methods[0])) < 0) {
        return -2;
    }

    // From OutputService, we get methods to log/toast/notify
    output_service_class = (*env)->FindClass(env, "com/snakei/OutputService");
    log_message = (*env)->GetMethodID(env, output_service_class, 
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



/* Python-callable wrapper for LOGI */
static PyObject*
androidlog_log(PyObject *self, PyObject *python_string)
{
  LOGI("%s", PyString_AsString(python_string));
  Py_RETURN_NONE;  // I.e., `return Py_None;` with reference counting
}




/* Describe to Python how the method should be called */
static PyMethodDef AndroidlogMethods[] = {
    {"log", androidlog_log, METH_O,
     "Log an informational string to the Android log."},
    {NULL, NULL, 0, NULL} // This is the end-of-array marker
};



JNIEXPORT void JNICALL
Java_com_sensibility_1testbed_ScriptActivity_startEmbeddedPython(JNIEnv *env, jobject instance)
{
    jclass output_service_class;
    jmethodID log_message;
    FILE* file_pointer;
    char* file_name = "/sdcard/test.py";

    char floatinstring[16];

    output_service_class = (*env)->FindClass(env,
        "com/sensibility_testbed/OutputService");

    log_message = (*env)->GetMethodID(env, output_service_class,
        "logMessage", "()java/lang/String");

    //return (*env)->NewStringUTF(env, floatinstring);

    //Py_SetProgramName("/sdcard/mypython/python");
    LOGI("Before Py_Initialize...");
    Py_Initialize();

    // Print stats about the environment
    LOGI("ProgramName %s", (char*) Py_GetProgramName());
    LOGI("Prefix %s", Py_GetPrefix());
    LOGI("ExecPrefix %s", Py_GetExecPrefix());
    LOGI("ProgramFullName %s", Py_GetProgramFullPath());
    LOGI("Path %s", Py_GetPath());
    LOGI("Platform %s", Py_GetPlatform());
    LOGI("PythonHome %s", Py_GetPythonHome());


    PySys_SetPath("/sdcard/mypython/");

    LOGI("Initializing androidlog module");
    Py_InitModule("androidlog", AndroidlogMethods);

    PyRun_SimpleString("import androidlog\nandroidlog.log('Sure hope this works.')\n");

    LOGI("Now do the file!!!");
    file_pointer = fopen(file_name, "r");
    PyRun_SimpleFile(file_pointer, file_name);
//from time import time,ctime\n"
//       "print 'Today is',ctime(time())\n");
    LOGI("Before Py_Finalize...");
    Py_Finalize();
    LOGI("Done. Bye!");

}
