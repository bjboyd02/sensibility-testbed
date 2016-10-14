/*
 * Created by lukas.puehringer@nyu.edu
 * on 5/27/16.
 *
 * Provides helper functions that can be used by C-Python extension
 * to call into the Android Java Virtual Machine (JVM) using the
 * Java Native Interface (JNI)
 *
 * There are two different types of helper functions:
 *  - Functions that search for java classes and methods or instantiate
 *    java objects and return the according JNI reference, e.g.:
 *    jclass, jmethodID, jobject, jstring
 *
 *  - Functions that call methods of a java object, convert the data returned
 *    from java to a Python Object using the C-Python API and return the
 *    PyObject pointer to the caller
 *
 * These helper functions also perform other tasks that should be invisible to
 * the caller:
 *  - Attach the current thread to the JVM
 *  - Catch and log Java errors
 *    and in case they return to Python re-raise a Python Exception
 *  - Delete local references to Java Objects that where created during
 *    the call and are no longer needed
 *  - Convert primitive Java data types to Python objects
 *  - Use cjson library to decode string serialized
 *    complex Java data types (JSON format) to Python objects
 *
 *
 * Todo:
 *  - Consider name refactor,
 *    maybe something like java-c-python-glue would be more appropriate
 *
 *  - Consider restructuring, maybe JNI can be even more invisible to
 *    the calling C functions Currently repeatedly used Java classes and
 *    methods are cached in the extension modules, by calling helper
 *    functions that return Java objects (jclass, jmethodID, jstring) maybe the
 *    caching could also happen here, so that no Java/JNI types are
 *    exposed to outside code at all
 *
 *  - All of the helper functions calling a Java objects's method are
 *    wrapped themselves by another helper function  as well to perform
 *    tasks that are common to all of those, i.e.:
 *      jni_py_call attaches the thread to JVM,
 *      instantiates the required Java object, calls one of
 *      _void, _boolean, _json, ...
 *      (passed as function pointer to jni_py_call), deletes the
 *      reference to the Java object and returns the PyObject
 *      pointer received from the wrapped function
 *
 *   - Currently we always attach the thread to JVM, because attaching
 *     when we don't have to is a no-op, whereas not attaching when
 *     we have to is a big problem
 *     This has to be revised once we do multithreading
 *     Also releasing the threads isn't yet taken care of
 *
 */

#include "jniglue.h"

/*
 * Attach current thread to Java VM and return a valid JNIEnv pointer
 */
JNIEnv *jni_get_env(void) {
    JNIEnv *env;
    int status = (*cached_vm)->AttachCurrentThread(cached_vm, &env, NULL);
    if(status < 0) {
        LOGI("jni_get_env: could not attach thread");
        return 0;
    }
    pthread_setspecific(jni_thread_key, (void*) env);
    return env;
}


/*
 * Detach current thread from JavaVM
 * called automatically
 */
void jni_detach_current_thread(void *env) {
    JNIEnv *jni_env = (JNIEnv*) env;
    if (jni_env != NULL) {
        (*cached_vm)->DetachCurrentThread(cached_vm);
        pthread_setspecific(jni_thread_key, NULL);
    }
}

/*
 * Return a new JNI global reference
 * Don't forget to free the reference!!!
 */
jobject jni_get_global_reference(jobject local_ref) {
    JNIEnv *jni_env;
    jni_env = jni_get_env();
    return (*jni_env)->NewGlobalRef(jni_env, local_ref);
}


/*
 * Takes a native reference and deletes it from JNI local reference table
 *
 * The JNI local reference table can only house 512 references at the same time
 * and they only get deleted automatically if the native call returns, i.e. if
 * the native process that was called FROM Java to e.g. start a
 * Python Interpreter/ Seattle sandbox in which e.g. native calls
 * to the JVM are performed
 *
 * Native references like jstring need to be casted to jobject before calling
 * this method, e.g. jni_delete_reference((jobject) variable_of_type_string)
 *
 * <Arguments>
 *   jobject - C character array pointer (char*)
 *
 * Note:
 *  - Does not handle errors
 *  - I'd like this not to be exposed to the extensions module (see doc string)
 *
 */
void jni_delete_reference(jobject obj) {
    JNIEnv *jni_env;
    jni_env = jni_get_env();
    (*jni_env)->DeleteLocalRef(jni_env, obj);
}


void jni_delete_global_reference(jobject obj) {
    JNIEnv *jni_env;
    jni_env = jni_get_env();
    (*jni_env)->DeleteGlobalRef(jni_env, obj);
}


/*
 * Takes a C character array pointer, creates a Java String from it
 * and returns a reference to the Java String
 *
 * This is useful if we need to pass a Java String to one of the
 * methods wrapped by jni_py_call which takes variadic arguments
 *
 * <Arguments>
 *   string - C character array pointer (char*)
 *
 * <Returns>
 *   StringUTF - Native reference to Java String (jstring)
 *
 * Note:
 *  - REQUIRES CALL TO jni_delete_reference WHEN NO LONGER NEED !!!
 *  - Only logs JAVA Errors, does not catch them (which can make the app crash)
 *  - The JNI local reference table can only house 512 references
 *  - I'd like this not to be exposed to the extensions module (see doc string)
 *
 */
jstring jni_get_string(char *string) {
    JNIEnv *jni_env;
    jni_env = jni_get_env();
    return (*jni_env)->NewStringUTF(jni_env, string);
}


jobjectArray jni_get_string_array(int argc, char *argv[]) {
    JNIEnv *jni_env;
    jobjectArray string_array;
    int i;

    jni_env = jni_get_env();

    string_array = (jobjectArray)(*jni_env)->NewObjectArray(jni_env, argc,
            (*jni_env)->FindClass(jni_env, "java/lang/String"),
            (*jni_env)->NewStringUTF(jni_env, ""));

    for(i = 0; i < argc; i++) {
        (*jni_env)->SetObjectArrayElement(jni_env, string_array, i,
                (*jni_env)->NewStringUTF(jni_env, argv[i]));
    }

    return string_array;
}


/*
 * Takes a Java Class name, searches for the Class in the JVM and returns
 * a Java Class reference
 *
 * <Arguments>
 *   class_name - Name of a Java Class (const char*),
 *   e.g. "com/snakei/SensorService"
 *
 * <Returns>
 *   class - Native reference to a Java Class (jclass)
 *
 * Note:
 *  - REQUIRES CALL TO jni_delete_reference WHEN NO LONGER NEED !!!
 *  - Only logs JAVA Errors, does not catch them (which can make the app crash)
 *  - The JNI local reference table can only house 512 references
 *  - I'd like this not to be exposed to the extensions module (see doc string)
 *
 */
jclass jni_find_class(const char *class_name) {
    JNIEnv *jni_env;
    jclass class;

    jni_env = jni_get_env();
    class = (*jni_env)->FindClass(jni_env, class_name);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jni_find_class: exception occurred");
    }
    if (class == NULL) {
        LOGI("jni_find_class: returned NULL");
    }
    return class;
}


/*
 * Convenience function to find class and create global reference from it.
 *
 * Note:
 *  - REQUIRES CALL TO jni_delete_global_reference WHEN NO LONGER NEED !!!
 * - while jmethodID objects are global references per default, jclass objects
 * are not. We need them to be global to work accross multiple threads
 */
jclass jni_find_class_as_global(const char *class_name) {
    jclass local_class;
    local_class = jni_find_class(class_name);
    return (jclass)jni_get_global_reference((jobject) local_class);
}


/*
 * Takes a Java Class reference and the JNI Method signature of a
 * static Java Singleton getter "getInstance" and returns
 * the Java method reference
 *
 * <Arguments>
 *   class - Native reference to a Java Class (jclass)
 *   type_signature - JNI Method signature (const char*),
 *   e.g. "()Lcom/snakei/SensorService;"
 *
 * <Returns>
 *   getter - Native reference to a Java Method (jMethodID)
 *
 * Note:
 *  - REQUIRES CALL TO jni_delete_reference WHEN NO LONGER NEED !!!
 *  - Only logs JAVA Errors, does not catch them (which can make the app crash)
 *  - The JNI local reference table can only house 512 references
 *  - I'd like this not to be exposed to the extensions module (see doc string)
 *
 */
jmethodID jni_find_getter(jclass class, const char *type_signature) {
    JNIEnv *jni_env;
    jmethodID getter;

    jni_env = jni_get_env();
    getter = (*jni_env)->GetStaticMethodID(
            jni_env, class, "getInstance", type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jni_find_getter: exception occurred");
    }
    if (getter == NULL) {
        LOGI("jni_find_getter: returned NULL");
    }
    return getter;
}


/*
 * Takes a Java Class reference, the name of a Java Object Method and the
 * JNI Method signature of a Java Object Method and returns
 * the Java Method reference
 *
 * <Arguments>
 *   class - Native reference to a Java Class (jclass)
 *   method_name - Java Method name (const char*), e.g. "getSensorList"
 *   type_signature - JNI Method signature (const char*),
 *   e.g. "()Ljava/lang/String;"
 *
 * <Returns>
 *   method - Native reference to a Java Method (jMethodID)
 *
 * Note:
 *  - REQUIRES CALL TO jni_delete_reference WHEN NO LONGER NEED !!!
 *  - Only logs JAVA Errors, does not catch them (which can make the app crash)
 *  - The JNI local reference table can only house 512 references
 *  - I'd like this not to be exposed to the extensions module (see doc string)
 *
 */
jmethodID jni_find_method(
        jclass class, const char *method_name, const char *type_signature) {
    JNIEnv *jni_env;
    jmethodID method;

    jni_env = jni_get_env();
    method = (*jni_env)->GetMethodID(
            jni_env, class, method_name, type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jni_find_method: exception occurred - %s", method_name);
    }
    if (method == NULL) {
        LOGI("jni_find_method: returned NULL");
    }
    return method;
}


/*
 * Takes a Java Class reference, the name of a static Java Method and
 * the JNI Method signature of a static Java Method and returns
 * the Java Method reference
 *
 * <Arguments>
 *   class - Native reference to a Java Class (jclass)
 *   method_name - Java Method name (const char*), e.g. "logMessage"
 *   type_signature - JNI Method signature (const char*),
 *   e.g. "(Ljava/lang/String;)V"
 *
 * <Returns>
 *   method - Native reference to a Java Method (jMethodID)
 *
 * Note:
 *  - REQUIRES CALL TO jni_delete_reference WHEN NO LONGER NEED !!!
 *  - Only logs JAVA Errors, does not catch them (which can make the app crash)
 *  - The JNI local reference table can only house 512 references
 *  - I'd like this not to be exposed to the extensions module (see doc string)
 *
 */
jmethodID jni_find_static_method(
            jclass class, const char *method_name, const char *type_signature) {
    JNIEnv *jni_env;
    jmethodID method;

    jni_env = jni_get_env();
    method = (*jni_env)->GetStaticMethodID(jni_env, class, method_name,
                                           type_signature);
    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jni_find_static_method: exception occurred - %s", method_name);
    }
    if (method == NULL) {
        LOGI("jni_find_static_method: returned NULL");
    }
    return method;
}


/*
 * Takes a Java Class reference and a Java Method reference
 * (usually to a Singleton getter) and returns a Java Object reference
 *
 * <Arguments>
 *   class - Native reference to a Java Class (jclass)
 *   getter - Native reference to a Java Method (jmethodID)
 *
 * <Returns>
 *   object - Native reference to a Java Object (jobject)
 *
 * Note:
 *  - REQUIRES CALL TO jni_delete_reference WHEN NO LONGER NEED !!!
 *  - Only logs JAVA Errors, does not catch them (which can make the app crash)
 *  - The JNI local reference table can only house 512 references
 *  - I'd like this not to be exposed to the extensions module (see doc string)
 *
 */
jobject jni_get_instance(jclass class, jmethodID getter) {
    JNIEnv *jni_env;
    jobject object;

    jni_env = jni_get_env();
    object = (*jni_env)->CallStaticObjectMethod(jni_env, class, getter);

    if ((*jni_env)->ExceptionOccurred(jni_env)){
        LOGI("jni_get_instance: exception occurred");
    }
    if (object == NULL) {
        LOGI("jni_get_instance: returned NULL");
    }
    return object;
}



/* JNI Python helpers - call Java return python */


/*
 * Internal function
 *
 * Checks if an error was thrown in Java, clears (catches) the exception,
 * calls the toString method of the Java Exception, converts the returned
 * Java String to a Python String and raises a Python Exception with
 * that String. The raised Exception can subsequently handled in Python.
 * Also takes a C char pointer and writes it to the log, to know
 * which C function call was called before Java threw the Exception
 *
 * <Arguments>
 *   jni_env - Pointer to JNI environment this thread is attached to (JNIEnv*)
 *   where - C character array that gets written to the log (const char*)
 *
 * <Returns>
 *   return code - 1 if error was thrown, 0 if not (int)
 *
 * Note:
 *  - Does not attach thread to JVM, because it assumes the calling function
 *    from which it receives the jni_env pointer has done this
 *
 */
int __handle_errors(JNIEnv* jni_env, const char *where) {

    jthrowable error;
    const char *error_msg;
    jclass class;
    jmethodID method;
    jstring error_msg_java;

    error = (*jni_env)->ExceptionOccurred(jni_env);
    if (error) {
        LOGI("%s", where);
        (*jni_env)->ExceptionClear(jni_env);

        // Maybe cache java/lang/Object and toString
        class = (*jni_env)->FindClass(jni_env, "java/lang/Object");
        method = (*jni_env)->GetMethodID(
                jni_env, class, "toString", "()Ljava/lang/String;");
        error_msg_java = (*jni_env)->CallObjectMethod(jni_env, error, method);
        error_msg = (*jni_env)->GetStringUTFChars(jni_env, error_msg_java, 0);
        PyErr_SetString(PyExc_Exception, error_msg);
        (*jni_env)->ReleaseStringUTFChars(jni_env, error_msg_java, error_msg);
        (*jni_env)->DeleteLocalRef(jni_env, error_msg_java);
        (*jni_env)->DeleteLocalRef(jni_env, class);
        return 1;
    }
    return 0;
}


/*
 * Calls void Java Method on passed Java object passing variadic arguments
 *
 * If an exception was thrown in Java it gets caught in C and
 * re-raised in Python
 *
 * <Arguments>
 *   jni_env - Pointer to JNI environment this thread is attached to (JNIEnv*)
 *   object - Java Object whose method is to be called (jobject)
 *   method - Java Object method to be called (jmethodID)
 *   args - Arguments that get passed to the Java method (va_list)
 *
 * <Returns>
 *   Python None (Py_None) or NULL if Java threw an exception
 *
 * Note:
 *   Does not need to be called directly, but passed as function pointer to
 *   wrapper jni_py_call for this method that takes care of attaching thread
 *   to JVM and instantiating the needed object
 *
 */
PyObject* _void(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {

    // V for va_list
    (*jni_env)->CallVoidMethodV(jni_env, object, method, args);
    if (__handle_errors(jni_env, "_void: exception occurred")) {
        // If we want to re-raise the exception in Python we have to return NULL
        return NULL;
    }
    Py_RETURN_NONE;
}


/*
 * Calls boolean Java Method on passed Java object passing variadic
 * arguments and converts returned Java boolean to Python bool
 *
 * If an exception was thrown in Java it gets caught in C
 * and re-raised in Python
 *
 * <Arguments>
 *   jni_env - Pointer to JNI environment this thread is attached to (JNIEnv*)
 *   object - Java Object whose method is to be called (jobject)
 *   method - Java Object method to be called (jmethodID)
 *   args - Arguments that get passed to the Java method (va_list)
 *
 * <Returns>
 *  Py_True or Py_False or NULL if Java threw an exception
 *
 * Note:
 *   Does not need to be called directly, but passed as function pointer to
 *   wrapper jni_py_call for this method that takes care of attaching thread
 *   to JVM and instantiating the needed object
 */
PyObject* _boolean(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {
    jboolean success;

    success = (*jni_env)->CallBooleanMethodV(jni_env, object, method, args);
    if (__handle_errors(jni_env, "_boolean: exception occurred")) {
        //If we want to re-raise the exception in Python we have to return NULL
        return NULL;
    }
    if (success) {
        Py_RETURN_TRUE;
    } else {
        Py_RETURN_FALSE;
    }
}


/*
 * Calls int Java Method on passed Java object passing variadic
 * arguments and converts returned Java int to Python int
 *
 * If an exception was thrown in Java it gets caught in C
 * and re-raised in Python
 *
 * <Arguments>
 *   jni_env - Pointer to JNI environment this thread is attached to (JNIEnv*)
 *   object - Java Object whose method is to be called (jobject)
 *   method - Java Object method to be called (jmethodID)
 *   args - Arguments that get passed to the Java method (va_list)
 *
 * <Returns>
 *   Python int or NULL if Java threw an exception
 *
 * Note:
 *   Does not need to be called directly, but passed as function pointer to
 *   wrapper jni_py_call for this method that takes care of attaching thread
 *   to JVM and instantiating the needed object
 *
 */
PyObject* _int(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {
    int retval;

    retval = (*jni_env)->CallIntMethodV(jni_env, object, method, args);
    if (__handle_errors(jni_env, "_int: exception occurred")) {
        // If we want to re-raise the exception in Python we have to return NULL
        return NULL;
    }
    return Py_BuildValue("i", retval);
}


/*
 * Calls String Java Method on passed Java object passing
 * variadic arguments and converts returned Java String to Python string
 *
 * If an exception was thrown in Java it gets caught in C
 * and re-raised in Python
 *
 * <Arguments>
 *   jni_env - Pointer to JNI environment this thread is attached to (JNIEnv*)
 *   object - Java Object whose method is to be called (jobject)
 *   method - Java Object method to be called (jmethodID)
 *   args - Arguments that get passed to the Java method (va_list)
 *
 * <Returns>
 *   Python String or Python None (Py_None) if Java returned NULL
 *   or NULL if Java threw an exception
 *
 * Note:
 *   Does not need to be called directly, but passed as function pointer to
 *   wrapper jni_py_call for this method that takes care of attaching thread
 *   to JVM and instantiating the needed object
 *
 */
PyObject* _string(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {
    jstring java_string;
    const char *c_string;
    PyObject *py_string = NULL;

    // V for va_list
    java_string = (*jni_env)->CallObjectMethodV(jni_env, object, method, args);

    if (__handle_errors(jni_env, "_string: exception occurred")) {
        // If we want to re-raise the exception in Python we have to return NULL
        return NULL;
    }

    // We have to check for NULL, because Java could
    // have returned NULL without throwing an
    // Exception, in this case we should just return a Python None
    if(java_string == NULL) {
        Py_RETURN_NONE;
    }

    // Convert Java string to C char*
    c_string = (*jni_env)->GetStringUTFChars(jni_env, java_string, 0);
    // Convert C char* to Python string
    py_string = PyString_FromString(c_string);
    // Free memory and delete reference
    (*jni_env)->ReleaseStringUTFChars(jni_env, java_string, c_string);
    (*jni_env)->DeleteLocalRef(jni_env, java_string);

    return py_string;
}


/*
 * Calls String serialized JSONObject/JSONArray Java Method
 * on passed Java object passing variadic arguments and converts
 * returned Java String first to C chars, then uses cjson
 * to decode C chars as Python Object
 *
 * If an exception was thrown in Java it gets caught in C
 * and re-raised in Python
 *
 * <Arguments>
 *   jni_env - Pointer to JNI environment this thread is attached to (JNIEnv*)
 *   object - Java Object whose method is to be called (jobject)
 *   method - Java Object method to be called (jmethodID)
 *   args - Arguments that get passed to the Java method (va_list)
 *
 * <Returns>
 *   Python Dictionary or List or
 *     Python None (Py_None) if Java returned NULL or
 *     NULL if Java threw an exception
 *
 * Note:
 *   Does not need to be called directly, but passed as function pointer to
 *   wrapper jni_py_call for this method that takes care of attaching thread
 *   to JVM and instantiating the needed object
 *
 */
PyObject* _json(
        JNIEnv* jni_env, jobject object, jmethodID method, va_list args) {
    jstring java_string;
    const char *c_string_const;
    char *c_string;
    PyObject *py_object = NULL;

    java_string = (*jni_env)->CallObjectMethodV(jni_env, object, method, args);

    // V for va_list
    if (__handle_errors(jni_env, "_json: exception occurred")) {
        // If we want to re-raise the exception in Python we have to return NULL
        return NULL;
    }

    if(java_string == NULL) {
        LOGI("_json: returned NULL");
        Py_RETURN_NONE;
    }

    // Convert Java string to C const char*
    // JNI method will only give us a const char* although we want a char*
    c_string_const = (*jni_env)->GetStringUTFChars(jni_env, java_string, 0);

    // Convert Java string to char*
    // cjson will only accept a char* although we have a const char*
    c_string = malloc(strlen(c_string_const) + 1);
    strcpy(c_string, c_string_const);

    // Convert C char* to Python string
    py_object = JSON_decode_c(c_string);

    // Free memory and delete reference
    free(c_string);
    (*jni_env)->ReleaseStringUTFChars(jni_env, java_string, c_string_const);
    (*jni_env)->DeleteLocalRef(jni_env, java_string);

    return py_object;
}


/*
 * Wrapper for above jni_py_call* helper functions on Java Object Methods
 *
 * Takes a native reference to a Java Class, a native reference to a
 * Java Class Constructor(usually a Singleton constructor) of that class,
 * a function pointer to one of the above helper functions that should
 * be used to call the Java Method, a native reference to that Java
 * Method and variadic arguments.
 *
 * Attaches the current thread to the JVM, then instantiates the Java Object
 * to be called,converts the variadic arguments to a va_list and calls the
 * passed Java Method using the passed helper function on the instantiated
 * Object passing it the va_list as arguments,
 * eventually delete the native reference to the Java Object
 *
 * <Arguments>
 *   class - Native reference to Java class of the Method to be called (jclass)
 *   get_instance - Native reference to Constructor of that class (jmethodID)
 *   _jni_py_call - Function pointer to helper that is used to call Java Method
 *   cached_method - Native reference to Java Method to be called (jmethodID)
 *   ... - Variadic arguments to be passed on to Java Method
 *
 * <Returns>
 *   A Python Object or NULL if Java threw an exception
 *
 */
PyObject* jni_py_call(
        PyObject* (*_jni_py_call)(JNIEnv*, jobject, jmethodID, va_list),
        jclass class, jmethodID get_instance, jmethodID cached_method, ...) {

    JNIEnv *jni_env;
    va_list args;
    PyObject* result;
    jobject instance;

    jni_env = jni_get_env();
    instance = jni_get_instance(class, get_instance);

    va_start(args, cached_method);
    result = (*_jni_py_call)(jni_env, instance, cached_method, args);
    va_end(args);
    (*jni_env)->DeleteLocalRef(jni_env, instance);

    return result;
}


/*
 * A single helper function to call a static Java Method
 *
 * Attaches current thread to JVM, converts the variadic arguments to a va_list
 * and calls the passed Java Method passing it the va_list as arguments.
 *
 * If an exception was thrown in Java it gets caught in C
 * and re-raised in Python
 *
 * <Arguments>
 *   class - Native reference to Java class of Method to be called (jclass)
 *   cached_method - Native reference to Java Method to be called (jmethodID)
 *   ... - Variadic arguments to be passed on to Java Method
 *
 * <Returns>
 *   Python None (Py_None) or NULL if Java threw an exception
 *
 * Note:
 *  This function has no wrapper like the Java Object Methods above,
 *  because so far there is only one call to a static Java
 *  Method in the code that uses these helpers and above all
 *  static functions don't require an object that has to
 *  be instantiated beforehand and released afterwards
 *
 */
PyObject* jni_py_call_static_void(jclass class, jmethodID cached_method, ...) {

    JNIEnv *jni_env;
    va_list args;

    jni_env = jni_get_env();

    va_start(args, cached_method);
    (*jni_env)->CallStaticVoidMethodV(jni_env, class, cached_method, args);
    va_end(args);

    if (__handle_errors(
            jni_env, "jni_py_call_static_void: exception occurred")) {
        return NULL;
    }

    Py_RETURN_NONE;
}


