/*
 * Created by
 * albert.rafetseder@univie.ac.at
 * lukas.puehringer@nyu.edu
 * on 3/30/16
 *
 * The native implementation of startNativePythonInterpreter declared in
 * PythonInterpreterService.java
 *
 * Currently the interpreter is used to initialize "sensor" functions as Python
 * modules - init*(), acquire resources in Java - *_start_*(), run Python
 * tests scripts that use the new modules, and eventually release the
 * resources - *_stop_*().
 *
 *
 * Note:
 * Btw. this is the local JNI scope, all local references that get created
 * in here, e.g. by running a Python script that uses C-Python Extension, which
 * create native references to Java objects will not be automatically released
 * until this function returns
 * They can (and should be) released explicitly, though
 *
 *
 */

#include "interpreter.h"


JNIEXPORT void JNICALL
Java_com_snakei_PythonInterpreterService_runScript(
    JNIEnv* env, jobject instance, jobjectArray j_args,
    jstring j_home, jstring j_path, jobject context) {

  int argc;
  argc = (*env)->GetArrayLength(env, j_args);

  char *argv[argc];
  char *home;
  char *path;

  home = (char*) (*env)->GetStringUTFChars(env, j_home, NULL);
  path = (char*) (*env)->GetStringUTFChars(env, j_path, NULL);

  int i;
  for (i = 0; i < argc; i++) {
    jstring j_arg = (jstring) (*env)->GetObjectArrayElement(env, j_args, i);
    const char* arg_tmp = (*env)->GetStringUTFChars(env, j_arg, NULL);
    argv[i] = malloc(strlen(arg_tmp) + 1);
    strcpy(argv[i], arg_tmp);

    // We can release right away because we made a copy
    (*env)->ReleaseStringUTFChars(env, j_arg, arg_tmp);
  }

  JNIEnv *jni_env = jni_get_env();
  // Cache the application context to use it in the extensions
  cached_context = (*jni_env)->NewGlobalRef(jni_env, context);
  interpreter_init(home, path);
  interpreter_run(argc, argv);

  // Once interpreter_run returns we should be done with this process
   Py_Finalize();

  // XXX: This might lead to problems if a child process is still using
  // those char pointers
  (*env)->ReleaseStringUTFChars(env, j_home, home);
  (*env)->ReleaseStringUTFChars(env, j_path, path);

  for(i = 0; i < argc; i++) {
    free(argv[i]);
  }
}

void interpreter_init(char* home, char* path) {

  // C doesn't look for the file on the python search path anyway
  // We could iterate through all python paths and try to find the file
  // or we just cd into passed path and execute passed script
  // CAUTION!! in this case path must be a single path (no : delimiter)
  chdir(path);

  Py_SetPythonHome(home);
  // Apparently we can call Py_Initialize several times without problems
  Py_Initialize();
  PyEval_InitThreads();

  // Initialize C-Python Extensions
  initandroid();
  initandroidlog();
  miscinfo_init_pymodule();
  sensor_init_pymodule();

  // Init Java Services (this could be done in Python)
  miscinfo_init();
  sensor_init();

  // Start all the sensors (drains battery)
  // Think of a way to do this in seattle
  int i;
  for (i = 1;  i <= 17; i++) {
    sensor_start_sensing(i);
  }

  PyObject *sys_module = PyImport_ImportModule("sys");
  PyObject *sys_attr_path = PyObject_GetAttrString(sys_module, "path");
  PyList_Append(sys_attr_path, PyString_FromString(path));

  // Injecting Python print wrapper
  // cf.  https://github.com/kivy/python-for-android/blob/master/pythonforandroid/bootstraps/webview/build/jni/src/start.c#L181-L197
  LOGI("PyRun returns %i\n", Verbose_PyRun_SimpleString(
        "import sys\n" \
        "import androidlog\n" \
        "class LogFile(object):\n" \
        "    def __init__(self):\n" \
        "        self.buffer = ''\n" \
        "    def write(self, s):\n" \
        "        s = self.buffer + s\n" \
        "        lines = s.split(\"\\n\")\n" \
        "        for l in lines[:-1]:\n" \
        "            androidlog.log(l)\n" \
        "        self.buffer = lines[-1]\n" \
        "    def flush(self):\n" \
        "        return\n" \
        "sys.stdout = sys.stderr = LogFile()"));
}

void interpreter_run(int argc, char **argv) {
  PySys_SetArgv(argc, argv);
  Py_SetProgramName(argv[0]);

  LOGI("PyRun returns %i\n", Verbose_PyRun_SimpleFile(argv[0]));
}
