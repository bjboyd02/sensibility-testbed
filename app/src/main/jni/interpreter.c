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


void Java_com_snakei_PythonInterpreter_runScript(
    JNIEnv* env, jobject instance, jobjectArray j_args,
    jstring j_home, jstring j_path) {

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

  interpreter_init(home, path);
  interpreter_run(argc, argv);

  // We can't call Py_Finalize because a child might still be using Python
  // Todo: What should we do about this? Maybe wait?
  // Py_Finalize();

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
  LOGI("RUNNING %s", argv[0]);
  pid_t pid;
  pid = fork();
  if (pid == 0) {

    // Set process name
    LOGI("Setting proc name '%s' returns %i\n", argv[0], prctl(PR_SET_NAME, argv[0]));

    PySys_SetArgv(argc, argv);
    Py_SetProgramName(argv[0]);

    LOGI("PyRun returns %i\n", Verbose_PyRun_SimpleFile(argv[0]));

  } else {
//    (*jni_env)->CallStaticIntMethod(jni_env, class, method, 1);
    int ppid = getpid();
    int status;
    LOGI("Parent %i starts waiting for %i", ppid, pid);
    waitpid(pid, &status, 0);

    int macroret;
    if (macroret = WIFEXITED(status))
      LOGI("WIFEXITED %i", macroret);
    if (macroret = WEXITSTATUS(status))
      LOGI("WEXITSTATUS %i", macroret);
    if (macroret = WIFSIGNALED(status))
      LOGI("WIFSIGNALED %i", macroret);
    if (macroret = WTERMSIG(status))
      LOGI("WTERMSIG %i", macroret);
    if (macroret = WCOREDUMP(status))
      LOGI("WCOREDUMP %i", macroret);
    if (macroret = WIFSTOPPED(status))
      LOGI("WIFSTOPPED %i", macroret);
    if (macroret = WSTOPSIG(status))
      LOGI("WSTOPSIG %i", macroret);

    LOGI("Parent %i stops waiting for child %i, exit status was: %i", ppid, pid, status);
  }




//
//    LOGI("Before init log...");
//    initandroidlog();
//    LOGI("After init log...");
//
//    char *filename;
//    char *full_filename;
//
//    filename = "test_process.py";
//    full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//    strcpy(full_filename, files);
//    strcat(full_filename, filename);
//
//    LOGI("Before PyRUN...");
//    LOGI("PyRun returns %i", Verbose_PyRun_SimpleFile(full_filename));
//    LOGI("After PyRUN");
//


//    filename = "test_python.py";
//    full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//    strcpy(full_filename, files);
//    strcat(full_filename, filename);
//    LOGI("PyRun returns %i", Verbose_PyRun_SimpleFile(full_filename));

//  LOGI("Start Sensing IN C!!!!");
//  initsensor();
//  int i;
//  for (i = 1;  i <= 17; i++) {
//     sensor_start_sensing(i);
//  }
//
//  // Och, memory...
//  filename = "test_sensors.py";
//  full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//  strcpy(full_filename, files);
//  strcat(full_filename, filename);
//
//  LOGI("PyRun returns %i", Verbose_PyRun_SimpleFile(full_filename));
//  LOGI("Stop Sensing IN C!!!!");
//  int j;
//  for (j = 1;  j <= 17; j++) {
//    sensor_stop_sensing(j);
//   }
//
//  LOGI("Start Locating IN C!!!!");
//  initlocation();
//  location_start_location();
//  filename = "test_location.py";
//  full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//  strcpy(full_filename, files);
//  strcat(full_filename, filename);
//  LOGI("PyRun File: %s", full_filename);
//  LOGI("PyRun returns %i for %s", Verbose_PyRun_SimpleFile(full_filename),
//       filename);
//  LOGI("Stop Locating IN C!!!!");
//  location_stop_location();
//
//  LOGI("Start Media-ing IN C!!!!");
//  initmedia();
//  media_start_media();
//  filename = "test_tts.py";
//  full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//  strcpy(full_filename, files);
//  strcat(full_filename, filename);
//  LOGI("PyRun File: %s", full_filename);
//  LOGI("PyRun returns %i for %s", Verbose_PyRun_SimpleFile(full_filename),
//       filename);
//  LOGI("Stop Media-ing IN C!!!!");
//  media_stop_media();
//
//  LOGI("Init and start MiscInfo-ing IN C!!!!");
//  initmiscinfo();
//  filename = "test_miscinfo.py";
//  full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//  strcpy(full_filename, files);
//  strcat(full_filename, filename);
//  LOGI("PyRun File: %s", full_filename);
//  LOGI("PyRun returns %i for %s", Verbose_PyRun_SimpleFile(full_filename),
//       filename);

//  LOGI("Before Py_Finalize...");
//  Py_Finalize();
//
//  LOGI("Done. Bye!");
};

