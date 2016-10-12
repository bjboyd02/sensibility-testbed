/*
 * Created by
 * lukas.puehringer@nyu.edu
 * on 7/25/16
 *
 * C Python module uses PythonInterpreterService.java via JNI to start a new
 * Service Process and run a python program
 *
 * This is a replacement for Python's subprocess.popen("python", ...)
 *
 */
#include "popen.h"

/*
 * Caches native references of used Java Class and Java Methods
 */

PyObject* android_popen_python(PyObject *self, PyObject *args) {

  JNIEnv* jni_env;
  jobjectArray popen_args;

  PyObject* args_list;
  int argc;
  int i;

  if (!PyArg_ParseTuple(args, "O", &args_list)) {
    // Todo proper error raising, this must be a non empty array!!!!
    return NULL;
  }

  argc = PyList_Size(args_list);

  char* argv[argc];
  for (i = 0; i < argc; i++){
    argv[i] = PyString_AsString(PyList_GetItem(args_list, i));
    // Todo XXX: error checking and proper raising
  }

  // Todo: Add this to jnihelper
  jni_env = jni_get_env();

  popen_args = (jobjectArray)(*jni_env)->NewObjectArray(jni_env, argc,
                                                        (*jni_env)->FindClass(jni_env, "java/lang/String"),
                                                        (*jni_env)->NewStringUTF(jni_env, ""));
  for(i = 0; i < argc; i++) {
    (*jni_env)->SetObjectArrayElement(jni_env, popen_args, i,
                                      (*jni_env)->NewStringUTF(jni_env, argv[i]));
  }

  jh_callStaticVoid(popen_class, popen_method, popen_args, cached_context);

  jh_deleteReference((jobject) popen_args);
  Py_RETURN_NONE;
}

/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidPythonMethods[] = {
    {"popen_python", android_popen_python, METH_VARARGS,
      "Run python script in new service process."},
    {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


/*
 * Initializes Python module (android), looks up Java static runScript method
 *
 * Note:
 * If we wanted to build the module as .so or .dll we could
 * would have to change the signature to
 * PyMODINIT_FUNC initandroid(void)
 *
 */
void initandroid() {
  Py_InitModule("android", AndroidPythonMethods);
}