#include "interpreter.h"


/* Verbosely execute Python code.
 * Exceptions are print to Android log using LOGI macro.
 * Returns 0 on success or -1 if an exception was raised.
 */
int Verbose_PyRun_SimpleString(const char *code) {
  // Used for globals and locals
  PyObject *module = PyImport_AddModule("__main__");
  PyObject *d = PyDict_New();
  if (module == NULL)
    return -1;
  d = PyModule_GetDict(module);

  PyRun_StringFlags(code, Py_file_input, d, d, NULL);

  if (PyErr_Occurred()) {
    PyObject *errtype, *errvalue, *traceback;
    PyObject *errstring = NULL;

    PyErr_Fetch(&errtype, &errvalue, &traceback);

    if(errtype != NULL) {
      errstring = PyObject_Str(errtype);
      LOGI("Errtype: %s\n", PyString_AS_STRING(errstring));
    }
    if(errvalue != NULL) {
      errstring = PyObject_Str(errvalue);
      LOGI("Errvalue: %s\n", PyString_AS_STRING(errstring));
    }

    Py_XDECREF(errvalue);
    Py_XDECREF(errtype);
    Py_XDECREF(traceback);
    Py_XDECREF(errstring);
  }
  return 0;
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
  {"log2", androidlog_log2, METH_O,
    "Log an informational string through JNI."},
  {NULL, NULL, 0, NULL} // This is the end-of-array marker
};

// Only functions taking two PyObject* arguments are PyCFunction
// sensor_get_sensor_list only takes one, so we need to cast.
static PyMethodDef AndroidsensorMethods[] = {
  {"get_sensor_list", (PyCFunction) sensor_get_sensor_list, METH_NOARGS,
    "Get a list of sensor info dictionaries."},
  {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(JNIEnv* env, jobject instance, jstring python_home, jstring python_path, jstring python_script, jstring python_arguments) {
  LOGI("Ask for jni sensor values");
  if (sensor_start_sensing())
    LOGI("Started sensing");
  else
    LOGI("couldn't start sensing");
  int i = 0;
  for (i; i < 60; i++) {
    sensor_get_accelerometer();
    sleep(1);
  }
  if (sensor_stop_sensing())
    LOGI("Stopped sensing");
  else
    LOGI("couldn't stop sensing");


  char* home = (char*) (*env)->GetStringUTFChars(env, python_home, NULL);
  char* path = (char*) (*env)->GetStringUTFChars(env, python_path, NULL);
  // Environment variable EXTERNAL_STORAGE is /storage/emulated/legacy
  char* script = "/storage/emulated/legacy/Android/data/com.sensibility_testbed/files/test2.py"; //(char*) (*env)->GetStringUTFChars(env, python_script, NULL);
  char* args = (char*) (*env)->GetStringUTFChars(env, python_arguments, NULL);

  FILE* script_pointer;

  LOGI("home is %s but I won't set it! Ha!", home);
  //Py_SetPythonHome(home);

  LOGI("script is %s", script);
  LOGI("Oh and btw, args are %s", args);

  //Py_SetProgramName("/sdcard/mypython/python");
  LOGI("Before Py_Initialize...");
  Py_Initialize();

  LOGI("path is %s", path);
  PySys_SetPath(path);

  // Print stats about the environment
  LOGI("ProgramName %s", (char*) Py_GetProgramName());
  LOGI("Prefix %s", Py_GetPrefix());
  LOGI("ExecPrefix %s", Py_GetExecPrefix());
  LOGI("ProgramFullName %s", Py_GetProgramFullPath());
  LOGI("Path %s", Py_GetPath());
  LOGI("Platform %s", Py_GetPlatform());
  LOGI("PythonHome %s", Py_GetPythonHome());

  LOGI("Initializing androidlog module");
  Py_InitModule("androidlog", AndroidlogMethods);
  LOGI("androidlog initted");

  LOGI("Initializing sensors module");
  Py_InitModule("sensor", AndroidsensorMethods);
  LOGI("androidsensors initted");

  LOGI("PyRun string returns %i", Verbose_PyRun_SimpleString("import androidlog, sensor\nl = androidlog.log2\ns = sensor.get_sensor_list\nl('check out these lovely sensors: ' +  repr(s()))"));
  //LOGI("PyRun string returns %i", Verbose_PyRun_SimpleString("import androidlog\nl = androidlog.log2\nl(str(locals()))"));
  LOGI("PyRun string returns %i", Verbose_PyRun_SimpleString("import androidlog\nl = androidlog.log2\nl('Ooh yeah!!!!!!!!!')\ntry:\n  import os\nexcept Exception, e:\n  l(repr(e))\nl('still k')\nl(os.getlogin())\n")); //l(str(os.getresuid()))\nl(os.getgroups())\nl(str(os.getresgid()))\n") );
  //try:\n  l('How?')\n  f = open('/sdcard/Android/data/com.sensibility_testbed/files/blip', 'w')\nexcept Exception, e:\n  l(repr(e))\nelse:\n  f.write('It worketh!!!\\n')\nl('Done.')\n") );

  LOGI("Now do the file!!!");
  script_pointer = fopen(script, "r");
  if (script_pointer == NULL) {
    LOGI("NULL file pointer for '%s' because errno %i '%s'", script, errno, strerror(errno));
  }
  LOGI("PyRun returns %i", PyRun_SimpleFile(script_pointer, script));

  LOGI("Before Py_Finalize...");
  Py_Finalize();
  LOGI("Done. Bye!");


};

