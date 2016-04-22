#include "sensors.h"

/*
 *
 * Python extension to get a list of sensor info
 * for each available sensor.
 *
 * Returns
 *  [{"name" : <name>, "vendor":<vendor>},..]
 *
 * This is easy,
 * - get the cached JVM
 * - (new thread?)
 * - execute Java method in JVM using JNI, Java method
 *  = instantiates a SensorManager
 *  = calls appropriate getSensor method
 *  = transforms to a list of dicts of simple data types (char, int, float,..)
 *  = (destroys the manager?)
 *  = returns the list
 * - transform the data to Python
 * - free memory
 * - return
 */

PyObject* sensor_get_sensor_list(PyObject *self) {

    LOGI("Let's try to get some sensor info...");

    JNIEnv* jni_env;
    jclass sensor_service_class;
    jmethodID sensor_service_constructor;
    jobject sensor_service_object;
    jmethodID sensor_service_method;
    jobject sensor;


    // Use the cached JVM pointer to get a new environment
    // XXX: Will there be a concurrency issue with other code that uses the cached_vm?
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    // Find Sensor class
    sensor_service_class = (*jni_env)->FindClass(jni_env, "com/snakei/SensorService");
    // Find Sensor default constructor
    sensor_service_constructor = (*jni_env)->GetMethodID(jni_env, sensor_service_class, "<init>", "()V");
    // Call Sensor constructor
    sensor_service_object = (*jni_env)->NewObject(jni_env, sensor_service_class, sensor_service_constructor);

    // Find Sensor Service method
    sensor_service_method = (*jni_env)->GetMethodID(jni_env, sensor_service_class, "get_sensor_list", "()[Landroid/hardware/Sensor;");

    // Call non-static sensor method on created sensor object
    jobjectArray sensor_list = (*jni_env)->CallObjectMethod(jni_env, sensor_service_object, sensor_service_method);
    int sensor_list_count = (*jni_env)->GetArrayLength(jni_env, sensor_list);
    LOGI("C says we have %i sensors", (*jni_env)->GetArrayLength(jni_env, sensor_list));

    // Now get each sensor from the sensor array,
    // call its info functions and save them to python

    PyObject *py_sensor_list = PyList_New(sensor_list_count);
    PyObject *py_sensor_info;
    jclass sensor_class = (*jni_env)->FindClass(jni_env, "android/hardware/Sensor");
    int i;
    for (i = 0; i < sensor_list_count; i++) {
        py_sensor_info = PyDict_New();
        // Get Sensor
        jobject sensor = (*jni_env)->GetObjectArrayElement(jni_env, sensor_list, i);

        // Find and call Sensor get name method
        jmethodID sensor_method = (*jni_env)->GetMethodID(jni_env, sensor_class, "getName", "()Ljava/lang/String;");
        jstring java_sensor_name = (*jni_env)->CallObjectMethod(jni_env, sensor, sensor_method);

        // Convert java string to c char
        const char *sensor_name = (*jni_env)->GetStringUTFChars(jni_env, java_sensor_name, 0);

        // Create dict entry for the name
        PyDict_SetItemString(py_sensor_info, "name", PyString_FromString(sensor_name));
        (*jni_env)->ReleaseStringUTFChars(jni_env, java_sensor_name, sensor_name);

        // XXX Todo do the same for all the other sensors
        // ...

        // Add info dict to python sensor list
        PyList_SetItem(py_sensor_list, i, py_sensor_info);

    }

    return py_sensor_list;

    // Transform to C dict list to python dict list
    // ...
    // Release ?
//    (*cached_vm)->DetachCurrentThread(cached_vm);
    // Return to python
    // ...

}
/*
PyObject* sensor_get_sensor_list(PyObject *self) {

    // XXX JNIEnv could also be cached
    JNIEnv* jni_env;
    jclass sensor_service_class;
    jmethodID log_message;
    Get<PrimitiveType>ArrayElements array
    PyObject *py_sensor_list = PyList_New(sensor_count);
    PyObject *py_sensor_info;

    // Use the cached JVM pointer to get a new environment
    // XXX Environment could also be cached
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Find Java class
    output_service_class = (*jni_env)->FindClass(jni_env, "com/snakei/SensorService");

    // Find the Java method we want to call
    log_message = (*jni_env)->GetStaticMethodID(jni_env,
                                                output_service_class, "logMessage", "(Ljava/lang/String;)V");


    //For each sensor create a dictionary with all the available info
    int i = 0; //XXX LP: could use -std=99 compile flag instead
    for (i = 0; i < sensor_count; i++) {

    }


    return py_sensor_list;

} */

/*
 * Python Extension(s) to get actual sensor values
 *  e.g. Accelerometer
 *
 * This is harder and/as there are multiple approaches,
 * - Do we want one function for all sensors, like in current Repy Sensor API? [1]
 * - Do we want to poll a sensor? (let's start with this)
 *  e.g.:
 *      start_sensing(TYPE_ACCELEROMETER)
 *      get_current_value()
 *      stop_sensing()
 * - Do we want to use callback functions? (cool but hard)
 *  e.g.:
 *       start_sensing(TYPE_ACCELEROMETER, fn_called_with_sensor_values)
 *       stop_sensing()
 * - Should we go OO, like Yocto Python API? [2] (not really pythonic, is it?)
 *  e.g.:
 *       sensor = find_sensor(TYPE_ACCELEROMETER)
 *       sensor.start()
 *       sensor.getValue()
 *       sensor.stop()
 *
 * In any case we need to think of,
 *  - properly creating, starting, stopping, destroying sensors and sensor listener.
 *  - multiple sensors for one type.
 *  - multiple vessels concurrently accessing a sensor.
 *
 * [1] https://sensibilitytestbed.com/projects/project/wiki/sensors#Sensors
 * [2] http://www.yoctopuce.com/EN/products/yocto-meteo/doc/METEOMK1.usermanual.html#CHAP14
 *
 */