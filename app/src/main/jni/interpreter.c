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



void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(JNIEnv* env, jobject instance, jstring python_home, jstring python_path, jstring python_script, jstring python_arguments) {
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

  LOGI("PyRun string returns %i", PyRun_SimpleString("import androidlog\nl = androidlog.log2\nl('Ooh yeah!')\ntry:\n  import os\nexcept Exception, e:\n  l(repr(e)\nl('still k')\nl(os.getlogin())\n")); //l(str(os.getresuid()))\nl(os.getgroups())\nl(str(os.getresgid()))\n") );
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

