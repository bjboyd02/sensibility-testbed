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



void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(JNIEnv* env, jobject instance, jstring python_environment, jstring python_arguments) {
  FILE* file_pointer;
  char* file_name = "/sdcard/test.py";

  char floatinstring[16];


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

  PyRun_SimpleString("import androidlog\nandroidlog.log2('Sure hope this works.')\n");

  LOGI("Now do the file!!!");
  file_pointer = fopen(file_name, "r");
  PyRun_SimpleFile(file_pointer, file_name);
//from time import time,ctime\n"
//     "print 'Today is',ctime(time())\n");
  LOGI("Before Py_Finalize...");
  Py_Finalize();
  LOGI("Done. Bye!");


};

