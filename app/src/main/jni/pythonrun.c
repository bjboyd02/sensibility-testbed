/*
 * Created by
 * lukas.puehringer@nyu.edu
 * on 7/25/16
 *
 * C Python module uses PythonInterpreter.java via JNI to execute Python file
 *
 */
#include "pythonrun.h"

/*
 * Caches native references of used Java Class and Java Methods
 */
static struct output_cache {
  jclass class;
  jmethodID run_script;
} m_cached;


PyObject* android_run(PyObject *self, PyObject *python_script) {
  char* c_python_script;
//  char* c_python_args;

  jstring java_python_script;

  // Convert Python string to C string
  c_python_script = PyString_AsString(python_script);
  // Convert C string to Java string
  java_python_script = jh_getJavaString(c_python_script);

  PyObject* result = jh_callStaticVoid(m_cached.class, m_cached.run_script,
                                       java_python_script);

  jh_deleteReference((jobject) java_python_script);

  // XXX: do we have to delete the string reference?
  // We shouldn't have to, that's why it is called local, BUT

  return result;  // I.e., `return Py_None;` with reference counting
}



/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidPythonMethods[] = {
    {"run", android_run, METH_O,
      "Run python script in new process."},
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
  jclass class = jh_getClass("com/snakei/PythonInterpreter");
  m_cached = (struct output_cache){
      .class = class,
      .run_script = jh_getStaticMethod(class, "runScript",
                                        "(Ljava/lang/String;)V")};
}