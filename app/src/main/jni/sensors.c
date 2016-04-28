#include "sensors.h"

/*
 *
 * Python extension to get a list of sensor info
 * for each available sensor.
 *
 * Returns
 *  [{"name" : <name>, "vendor":<vendor>, ...},..]
 *
 * TODO:
 *      call rest of Android Sensor info methods
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
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Find SensorService class and instantiate object
    sensor_service_class = (*jni_env)->FindClass(jni_env, "com/snakei/SensorService");
    sensor_service_constructor = (*jni_env)->GetMethodID(jni_env, sensor_service_class, "<init>", "()V");
    sensor_service_object = (*jni_env)->NewObject(jni_env, sensor_service_class, sensor_service_constructor);

    // Find SensorService method to get a list of Android Sensors
    sensor_service_method = (*jni_env)->GetMethodID(jni_env, sensor_service_class, "get_sensor_list", "()[Landroid/hardware/Sensor;");
    jobjectArray sensor_list = (*jni_env)->CallObjectMethod(jni_env, sensor_service_object, sensor_service_method);
    int sensor_list_count = (*jni_env)->GetArrayLength(jni_env, sensor_list);
//    LOGI("C says we have %i sensors", (*jni_env)->GetArrayLength(jni_env, sensor_list));

    // For each sensor,
    // call its info functions and save them to a python dict
    PyObject *py_sensor_list = PyList_New(sensor_list_count);
    PyObject *py_sensor_info;
    jclass sensor_class = (*jni_env)->FindClass(jni_env, "android/hardware/Sensor");
    int i;
    for (i = 0; i < sensor_list_count; i++) {
        py_sensor_info = PyDict_New();
        jobject sensor = (*jni_env)->GetObjectArrayElement(jni_env, sensor_list, i);

        // Find and call Sensor get name method
        jmethodID sensor_method = (*jni_env)->GetMethodID(jni_env, sensor_class, "getName", "()Ljava/lang/String;");
        jstring java_sensor_name = (*jni_env)->CallObjectMethod(jni_env, sensor, sensor_method);

        // Convert java string to c char
        const char *sensor_name = (*jni_env)->GetStringUTFChars(jni_env, java_sensor_name, 0);

        // Create dict entry for the name
        PyDict_SetItemString(py_sensor_info, "name", PyString_FromString(sensor_name));

        // Release the chars!!!!
        (*jni_env)->ReleaseStringUTFChars(jni_env, java_sensor_name, sensor_name);

        // XXX Todo do the same for all the other sensors
        // ...

        // Add info dict to python sensor list
        PyList_SetItem(py_sensor_list, i, py_sensor_info);
    }
    (*cached_vm)->DetachCurrentThread(cached_vm);
    return py_sensor_list;
}

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