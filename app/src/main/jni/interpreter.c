#include "interpreter.h"

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
// where this is not the case we need to cast
static PyMethodDef AndroidsensorMethods[] = {
  {"get_sensor_list", (PyCFunction) sensor_get_sensor_list, METH_NOARGS,
    "Get a list of sensor info dictionaries."},
  {"get_acceleration", (PyCFunction) sensor_get_acceleration, METH_NOARGS,
          "Get list of accelerator values. [sample ts, poll ts, x, y, z]"},
  {"get_magnetic_field", (PyCFunction) sensor_get_magnetic_field, METH_NOARGS,
          "Get list of magnetic field values ... "},
  {"get_proximity", (PyCFunction) sensor_get_proximity, METH_NOARGS,
          "...."},
  {"get_light", (PyCFunction) sensor_get_light, METH_NOARGS,
          "...."},
  {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(JNIEnv* env, jobject instance, jstring python_home, jstring python_path, jstring python_script, jstring python_files, jstring python_arguments) {
  char* home = (char*) (*env)->GetStringUTFChars(env, python_home, NULL);
  char* path = (char*) (*env)->GetStringUTFChars(env, python_path, NULL);
  char* files = (char*) (*env)->GetStringUTFChars(env, python_files, NULL);
  // Environment variable EXTERNAL_STORAGE is /storage/emulated/legacy
  char* script = "/storage/emulated/legacy/Android/data/com.sensibility_testbed/files/test2.py"; //(char*) (*env)->GetStringUTFChars(env, python_script, NULL);
  char* args = (char*) (*env)->GetStringUTFChars(env, python_arguments, NULL);

  LOGI("home is %s but I won't set it! Ha!", home);
  //Py_SetPythonHome(home);

  LOGI("script is %s", script);
  LOGI("Oh and btw, args are %s", args);

  LOGI("Start Sensing IN C!!!!");
  sensor_start_sensing(1);
  sensor_start_sensing(11);
  sensor_start_sensing(14);
  sensor_start_sensing(9);

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

  //XXX: Temporary way to write python code until
  // we have found out how to run files
  const char* python_code = ""\
"import androidlog, sensor, sys, time\n"\
"l = androidlog.log2\n"\
"l('Lets get some sensor info')\n"\
"\n"\
"for sensor_info in sensor.get_sensor_list():\n"\
"  l(repr(sensor_info))\n"\
"\n"\
"l('Oh, wow, lovely sensors, why not poll them?')\n"\
"l('Lets start with some of the existing sensors...')\n"\
"\n"\
"for i in xrange(2):\n"\
"  l('Accelerometer: ' + repr(sensor.get_acceleration()))\n"\
"  l('Magnetic field: ' + repr(sensor.get_magnetic_field()))\n"\
"  l('Proximity: ' + repr(sensor.get_proximity()))\n"\
"  l('Light: ' + repr(sensor.get_light()))\n"\
"  time.sleep(0.5)\n"\
"  \n"\
"\n"\
"l('Bye, bye!')";
  LOGI("PyRun string returns %i", Verbose_PyRun_SimpleString(python_code));

//LOGI("PyRun string returns %i", Verbose_PyRun_SimpleString("import androidlog, sensor\nl = androidlog.log2\ns = sensor.get_sensor_list\nl('check out these lovely sensors: ' +  repr(s()))"));
  //LOGI("PyRun string returns %i", Verbose_PyRun_SimpleString("import androidlog\nl = androidlog.log2\nl(str(locals()))"));
//  LOGI("PyRun string returns %i", Verbose_PyRun_SimpleString("import androidlog\nl = androidlog.log2\nl('Ooh yeah!!!!!!!!!')\ntry:\n  import os\nexcept Exception, e:\n  l(repr(e))\nl('still k')\nl(os.getlogin())\n")); //l(str(os.getresuid()))\nl(os.getgroups())\nl(str(os.getresgid()))\n") );
  //try:\n  l('How?')\n  f = open('/sdcard/Android/data/com.sensibility_testbed/files/blip', 'w')\nexcept Exception, e:\n  l(repr(e))\nelse:\n  f.write('It worketh!!!\\n')\nl('Done.')\n") );

  LOGI("Now do the file!!!");

  // Och, memory...
  char *filename = "test_asset.py";
  char *full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
  strcpy(full_filename, files);
  strcat(full_filename, filename);

  LOGI("PyRun returns %i", Verbose_PyRun_SimpleFile(full_filename));

  LOGI("Before Py_Finalize...");
  Py_Finalize();
  LOGI("Stop Sensing IN C!!!!");
  sensor_stop_sensing(1);
  sensor_stop_sensing(11);
  sensor_stop_sensing(14);
  sensor_stop_sensing(9);

  LOGI("Done. Bye!");


};

