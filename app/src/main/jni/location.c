//
// Created by lukas on 5/4/16.
//

#include "location.h"

static struct location_cache {
    jclass class;
    jmethodID get_instance;
    jmethodID start_location;
    jmethodID stop_location;
    jmethodID get_location;
    jmethodID get_lastknown_location;
    jmethodID get_geolocation;
} m_cached;


void location_start_location() {
    jh_call(m_cached.class, m_cached.get_instance, jh_callVoidMethod, m_cached.start_location);
}

void location_stop_location() {
    jh_call(m_cached.class, m_cached.get_instance, jh_callVoidMethod, m_cached.stop_location);
}

/*
 * Calls get location for different location providers, i.e.
 * GPS, Network and Fused (uses Google Play Services),
 * and returns one dictionary to Python
 */
PyObject* location_get_location() {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_location);
}
/*
 * Calls get last known location for different location providers, i.e.
 * GPS, Network and Fused (uses Google Play Services),
 * and returns one dictionary to Python
 */
PyObject* location_get_lastknown_location() {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_lastknown_location);
}


PyObject* location_get_geolocation(PyObject *self, PyObject *args) {

    jdouble latitude, longitude;
    jint max_results;

    if (!PyArg_ParseTuple(args, "ddi", &latitude, &longitude, &max_results)){
        LOGI("Wrong arguments. I wonder if I should raise an Exception.");
        Py_RETURN_NONE;
    }

    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                                m_cached.get_geolocation, latitude, longitude, max_results);
}

static PyMethodDef AndroidLocationMethods[] = {
    {"get_location", (PyCFunction) location_get_location, METH_NOARGS,
        "Get locations from GPS, Network and Fused"},
    {"get_lastknown_location", (PyCFunction) location_get_lastknown_location, METH_NOARGS,
        "Get last known locations from GPS, Network and Fused"},
    {"get_geolocation", (PyCFunction) location_get_geolocation, METH_VARARGS,
        "Get address(es) from latitude and longitude"},
    {NULL, NULL, 0, NULL} // This is the end-of-array marker
};

//PyMODINIT_FUNC initlocation(void) {
void initlocation() {
    Py_InitModule("location", AndroidLocationMethods);
    jclass class = jh_getClass( "com/snakei/LocationService");

    m_cached = (struct location_cache){
            .class = class,
            .start_location = jh_getMethod(class, "start_location", "()V"),
            .stop_location = jh_getMethod(class, "stop_location", "()V"),
            .get_instance = jh_getGetter(class, "()Lcom/snakei/LocationService;"),
            .get_location = jh_getMethod(class, "getLocation",
                                              "()Ljava/lang/String;"),
            .get_lastknown_location = jh_getMethod(class,"getLastKnownLocation",
                                      "()Ljava/lang/String;"),
            .get_geolocation = jh_getMethod(class, "getGeoLocation",
                                            "(DDI)Ljava/lang/String;")};
}