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

void c_sensor_list() {

    LOGI("Let's try to get some sensor info...");

    JNIEnv* jni_env;
    jclass sensor_class;
    jmethodID sensor_method;
    jmethodID sensor_constructor;
    jobject sensor_object;
    int dummy;

    // Use the cached JVM pointer to get a new environment
    // XXX: Will there be a concurrency issue with other code that uses the cached_vm?
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Find Sensor class
    sensor_class = (*jni_env)->FindClass(jni_env, "com/snakei/SensorService");

    // Find Sensor default constructor
    sensor_constructor = (*jni_env)->GetMethodID(jni_env, sensor_class, "<init>", "()V");

    // Call Sensor constructor
    sensor_object = (*jni_env)->NewObject(jni_env, sensor_class, sensor_constructor);

    // Find Sensor method
    sensor_method = (*jni_env)->GetMethodID(jni_env, sensor_class, "get_sensor_list", "()I");

    // Call non-static sensor method on created sensor object
    // XXX: Get stupid int until we figured out how to return a list of sensor info dicts
    dummy = (*jni_env)->CallIntMethod(jni_env, sensor_object, sensor_method);
    LOGI("This is what we got from java, yeah: %i", dummy);

    // Transform to C dict list to python dict list
    // ...

    // Release ?
//    (*cached_vm)->DetachCurentThread(cached_vm);
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