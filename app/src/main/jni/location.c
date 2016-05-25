//
// Created by lukas on 5/4/16.
//

#include "location.h"

void _start_stop_location(const char* location_service_method_name) {
    JNIEnv* jni_env;
    jclass service_class;
    jmethodID service_getter;
    jobject service_object;
    jmethodID service_method;

    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    // Find LocationService class and get singleton instance
    service_class = (*jni_env)->FindClass(jni_env, "com/snakei/LocationService");
    service_getter = (*jni_env)->GetStaticMethodID(jni_env, service_class, "getInstance", "()Lcom/snakei/LocationService;");
    service_object = (*jni_env)->CallStaticObjectMethod(jni_env, service_class, service_getter);

    service_method = (*jni_env)->GetMethodID(jni_env, service_class, location_service_method_name, "()V");
    (*jni_env)->CallVoidMethod(jni_env, service_object, service_method);
    // XXX: Only detach if AttachCurrentThread wasn't a no-op
    //(*cached_vm)->DetachCurrentThread(cached_vm);
}

void location_start_location() {
    _start_stop_location("start_location");
}

void location_stop_location() {
    _start_stop_location("stop_location");
}



/*
 * Generic method to call get_location_values methods in Java
 * for different providers
 *
 * Returns location values double array for called provider
 *
 * XXX This is pretty much the same as _get_sensor_values()
 * Maybe we can merge these
 *
 */
PyObject* _get_location_values(const char *location_provider_method_name) {
    JNIEnv* jni_env;
    jclass service_class;
    jmethodID service_getter;
    jobject service_object;
    jmethodID service_method;

    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Find LocationService class and get singleton instance
    service_class = (*jni_env)->FindClass(jni_env, "com/snakei/LocationService");
    service_getter = (*jni_env)->GetStaticMethodID(jni_env, service_class, "getInstance", "()Lcom/snakei/LocationService;");
    service_object = (*jni_env)->CallStaticObjectMethod(jni_env, service_class, service_getter);

    service_method = (*jni_env)->GetMethodID(jni_env, service_class, location_provider_method_name, "()[D");
    jdoubleArray location_values = (jdoubleArray) (*jni_env)->CallObjectMethod(jni_env, service_object, service_method);
    if (location_values == NULL) {
        LOGI("NULL");
        (*jni_env)->DeleteLocalRef(jni_env, location_values);
        (*jni_env)->DeleteLocalRef(jni_env, service_class);
        (*jni_env)->DeleteLocalRef(jni_env, service_object);
        Py_RETURN_NONE;
    }

    int location_values_cnt = (*jni_env)->GetArrayLength(jni_env, location_values);
    jdouble *location_values_ptr = (*jni_env)->GetDoubleArrayElements(jni_env, location_values, 0);

    PyObject *py_location_values = PyDict_New();

    // Create dict entry for the name
    PyDict_SetItemString(py_location_values, "time_polled", Py_BuildValue("d", location_values_ptr[0]));
    PyDict_SetItemString(py_location_values, "time_sample", Py_BuildValue("d", location_values_ptr[1]));
    PyDict_SetItemString(py_location_values, "accuracy", Py_BuildValue("d", location_values_ptr[2]));
    PyDict_SetItemString(py_location_values, "altitude", Py_BuildValue("d", location_values_ptr[3]));
    PyDict_SetItemString(py_location_values, "bearing", Py_BuildValue("d", location_values_ptr[4]));
    PyDict_SetItemString(py_location_values, "latitude", Py_BuildValue("d", location_values_ptr[5]));
    PyDict_SetItemString(py_location_values, "longitude", Py_BuildValue("d", location_values_ptr[6]));
    PyDict_SetItemString(py_location_values, "speed", Py_BuildValue("d", location_values_ptr[7]));

    (*jni_env)->ReleaseDoubleArrayElements(jni_env, location_values, location_values_ptr, 0);

    // XXX: Only detach if AttachCurrentThread wasn't a no-op
    //(*cached_vm)->DetachCurrentThread(cached_vm);
    (*jni_env)->DeleteLocalRef(jni_env, location_values);
    (*jni_env)->DeleteLocalRef(jni_env, service_class);
    (*jni_env)->DeleteLocalRef(jni_env, service_object);

    return py_location_values;

}

/*
 * Calls get location for different location providers, i.e.
 * GPS, Network and Fused (uses Google Play Services),
 * and returns one dictionary to Python
 */
PyObject* location_get_location() {
    PyObject *py_location_providers = PyDict_New();
    PyObject *values_gps = _get_location_values("getLocationValuesGPS");
    if (values_gps != Py_None) {
        PyDict_SetItemString(py_location_providers, "gps", values_gps);
    }
    PyObject *values_network = _get_location_values("getLocationValuesNetwork");
    if (values_network != Py_None) {
        PyDict_SetItemString(py_location_providers, "network", values_network);
    }
    PyObject *values_fused = _get_location_values("getLocationValuesFused");
    if (values_fused != Py_None) {
        PyDict_SetItemString(py_location_providers, "fused", values_fused);
    }
    return py_location_providers;
}
/*
 * Calls get last known location for different location providers, i.e.
 * GPS, Network and Fused (uses Google Play Services),
 * and returns one dictionary to Python
 */
PyObject* location_get_lastknown_location() {
    PyObject *py_location_providers = PyDict_New();
    PyObject *values_gps = _get_location_values("getLastKnownLocationValuesGPS");
    if (values_gps != Py_None) {
        PyDict_SetItemString(py_location_providers, "gps", values_gps);
    }
    PyObject *values_network = _get_location_values("getLastKnownLocationValuesNetwork");
    if (values_network != Py_None) {
        PyDict_SetItemString(py_location_providers, "network", values_network);
    }
    PyObject *values_fused = _get_location_values("getLastKnownLocationValuesFused");
    if (values_fused != Py_None) {
        PyDict_SetItemString(py_location_providers, "fused", values_fused);
    }
    return py_location_providers;
}