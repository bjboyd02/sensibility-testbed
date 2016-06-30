#include "interpreter.h"

void Java_com_snakei_PythonInterpreterService_startNativePythonInterpreter(JNIEnv* env,
    jobject instance, jstring python_home, jstring python_path, jstring python_script,
    jstring python_files, jstring python_arguments) {

  char* home = (char*) (*env)->GetStringUTFChars(env, python_home, NULL);
  char* path = (char*) (*env)->GetStringUTFChars(env, python_path, NULL);
  char* files = (char*) (*env)->GetStringUTFChars(env, python_files, NULL);
  // Environment variable EXTERNAL_STORAGE is /storage/emulated/legacy
  char* args = (char*) (*env)->GetStringUTFChars(env, python_arguments, NULL);

  LOGI("home is %s but I won't set it! Ha!", home);
  //Py_SetPythonHome(home);

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

  initandroidlog();

//  LOGI("Start Sensing IN C!!!!");
//  initsensor();
//  int i;
//  for (i = 1;  i <= 17; i++) {
//     sensor_start_sensing(i);
//  }

  // Och, memory...
//  char *filename = "test_sensors.py";
//  char *full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//  strcpy(full_filename, files);
//  strcat(full_filename, filename);
//
//  LOGI("PyRun returns %i", Verbose_PyRun_SimpleFile(full_filename));
//  LOGI("Stop Sensing IN C!!!!");
//  int j;
//  for (j = 1;  j <= 17; j++) {
//    sensor_stop_sensing(j);
//   }

//  LOGI("Start Locating IN C!!!!");
//  initlocation();
//  location_start_location();
//  char *filename = "test_location.py";
//  char *full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//  strcpy(full_filename, files);
//  strcat(full_filename, filename);
//  LOGI("PyRun File: %s", full_filename);
//  LOGI("PyRun returns %i for %s", Verbose_PyRun_SimpleFile(full_filename), filename);
//  LOGI("Stop Locating IN C!!!!");
//  location_stop_location();

//  LOGI("Start Media-ing IN C!!!!");
//  initmedia();
//  media_start_media();
//  char *filename = "test_tts.py";
//  char *full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
//  strcpy(full_filename, files);
//  strcat(full_filename, filename);
//  LOGI("PyRun File: %s", full_filename);
//  LOGI("PyRun returns %i for %s", Verbose_PyRun_SimpleFile(full_filename), filename);
//  LOGI("Stop Media-ing IN C!!!!");
//  media_stop_media();

  LOGI("Init and start MiscInfo-ing IN C!!!!");
  initmiscinfo();
  char *filename = "test_miscinfo.py";
  char *full_filename = (char *) malloc(1 + strlen(files) + strlen(filename));
  strcpy(full_filename, files);
  strcat(full_filename, filename);
  LOGI("PyRun File: %s", full_filename);
  LOGI("PyRun returns %i for %s", Verbose_PyRun_SimpleFile(full_filename), filename);


  LOGI("Before Py_Finalize...");
  Py_Finalize();

  LOGI("Done. Bye!");
};

