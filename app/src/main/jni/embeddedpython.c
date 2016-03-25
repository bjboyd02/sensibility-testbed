/*
 * Stolen from https://github.com/aaaaalbert/HelloAndroidJni
 */
#include <Python.h>
#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>

# define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, __FILE__, __VA_ARGS__))

/*
 * This function should be called when the Java code does
 * `System.loadLibrary()`.
 * See https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html#JNI_OnLoad
*/
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("Look Ma and Pa, I'm writing to the Android log!");
    return JNI_VERSION_1_6;
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
    jclass main_activity_class;
    jmethodID accelgetter;
    FILE* file_pointer;
    char* file_name = "/sdcard/test.py";

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
/*
    float anacceleration;
    char floatinstring[16];
    size_t size = 256;
    char current_working_dir[size];

    LOGI("Blix Blypsilon %s", getcwd(current_working_dir, size));


    main_activity_class = (*env)->FindClass(env,
            "com/example/aaaaa/helloandroidjni/MainActivity");

    accelgetter = (*env)->GetStaticMethodID(env, main_activity_class,
            "getAccelValue", "()F");

    anacceleration = (*env)->CallStaticFloatMethod(env, main_activity_class, accelgetter);



    LOGI("Acceleration!!!! It's %f", anacceleration);

    sprintf(floatinstring, "%f", anacceleration);
    return (*env)->NewStringUTF(env, floatinstring);
*/
}
