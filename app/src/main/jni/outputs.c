#include "outputs.h"

static struct output_cache {
    jclass class;
    jmethodID log_message;
} m_cached;

/*
 * Log a message from Python through JNI.
 *
 * This involves conversion of that message from Python to C, and 
 * then to Java.
 */
PyObject* androidlog_log(PyObject *self, PyObject *python_message) {
  char* c_message;
  jstring java_message;

  // Convert Python string to C string
  c_message = PyString_AsString(python_message);
  // Convert C string to Java string
  java_message = jh_getJavaString(c_message);

  PyObject* result = jh_callStaticVoid(m_cached.class, m_cached.log_message,
                                       java_message);

  jh_deleteReference((jobject) java_message);

  // XXX: do we have to delete the string reference?
  // We shouldn't have to, that's why it is called local, BUT

  return result;  // I.e., `return Py_None;` with reference counting
}

/* Describe to Python how the method should be called */
static PyMethodDef AndroidLogMethods[] = {
        {"log", androidlog_log, METH_O,
                "Log an informational string through JNI."},
        {NULL, NULL, 0, NULL} // This is the end-of-array marker
};

//PyMODINIT_FUNC initandroidlog(void) {
void initandroidlog() {
  Py_InitModule("androidlog", AndroidLogMethods);
  jclass class = jh_getClass("com/snakei/OutputService");
  m_cached = (struct output_cache){
          .class = class,
          .log_message = jh_getStaticMethod(class, "logMessage",
                                            "(Ljava/lang/String;)V")};
}