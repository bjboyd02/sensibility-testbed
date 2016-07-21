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

void logf2(const char* format, ...) {
  FILE *fp;
  va_list args;

  fp = fopen("/sdcard/interpreter.log", "a+");
  va_start(args, format);
  vfprintf(fp, format, args);
  fclose(fp);
  va_end(args);
}

void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(
        JNIEnv* env, jobject instance, jstring python_scripts) {

  pid_t pid;
  pid = fork();
  if (pid == 0) {
    char *files = (char *) (*env)->GetStringUTFChars(env, python_scripts, NULL);
    logf2("Before Py_Initialize...");
    Py_SetPythonHome("/data/data/com.sensibility_testbed/files/python");
    Py_Initialize();
    logf2("After Py_Initialize...");

    char *filename;
    char *full_filename;

    filename = "test_process.py";
    full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
    strcpy(full_filename, files);
    strcat(full_filename, filename);

    logf2("Before PyRUN...\n");
    logf2("PyRun returns %i\n", Verbose_PyRun_SimpleFile(full_filename));
    logf2("After PyRUN\n");

    Py_Finalize();
  }







  // https://github.com/kuri65536/sl4a/blob/master/android/ScriptingLayerForAndroid/jni/com_googlecode_android_scripting_Exec.cpp
//  pid_t pid;
//  char* cmd;
//  cmd = "/bin/pwd";
//  char *args[0];
//  char *envp[0];
//  // Open a master pseudo terminal with read/write permissions
//  // (creates pseudo terminal slave)
//  // Returns a file discriptor
//  // Abort if it does not work
//  int ptm = open("/dev/ptmx", O_RDWR);
//  if(ptm < 0){
//    LOGI("Cannot open /dev/ptmx: %s\n", strerror(errno));
//    return -1;
//  }
//
//  // Manipulate filedescriptor to enable the close-on-exec
//  fcntl(ptm, F_SETFD, FD_CLOEXEC);
//
//  // Sets mode and ownership of slave pseudo temrinal to UID of this process
//  // Unlocks slave pseudoterminal
//  // stores name of slave pseudoterminal
//  // needs to be done before using slave pseudo terminal
//  // Abort if none of this works
//  if (grantpt(ptm) || unlockpt(ptm) ||
//      ((devname = (char*) ptsname(ptm)) == 0)) {
//    LOGI("Trouble with /dev/ptmx: %s\n", strerror(errno));
//    return -1;
//  }
//
//  pid = fork();
//  // Parent and child processes start execution here
//  // Both have identical but separate adress spaces
//
//  if(pid < 0) {
//    LOGI("Fork failed: %s\n", strerror(errno));
//    return -1;
//  }
//  LOGI("pid %d\n", pid);
//  // This is the child process
//  if(pid == 0){
//
//    int pts;
//
//    // Creates new session (collection of process group)
//    setsid();
//
//    // Opens pseudo terminal slave
//    pts = open(devname, O_RDWR);
//    if(pts < 0) {
//      exit(-1);
//    }
//    // Duplicate slave to special unix filedescriptors
//    // Standard input
//     dup2(pts, 0);
//     // Standard output
//     dup2(pts, 1);
//     // Standard error
//     dup2(pts, 2);
//     // Close master, because we have above?
//    close(ptm);
//    LOGI("and do I get here?\n");
//    // run run run
//    LOGI("execv returned %d\n", execve(cmd, args, envp));
//    sleep(5);
//    LOGI("Child goes home\n");
//    return -1;
//  } else {
//    sleep(8);
//    LOGI("Parent goes home\n");
//    return -1;
//  }

//
//
//
////
//////    PySys_SetPath(path);
////
////
////    // Print stats about the environment
////  LOGI("ProgramName %s", (char*) Py_GetProgramName());
////  LOGI("Prefix %s", Py_GetPrefix());
////  LOGI("ExecPrefix %s", Py_GetExecPrefix());
////  LOGI("ProgramFullName %s", Py_GetProgramFullPath());
////  LOGI("Path %s", Py_GetPath());
////  LOGI("Platform %s", Py_GetPlatform());
////  LOGI("PythonHome %s", Py_GetPythonHome());
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

