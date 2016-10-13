/*
 * Created by lukas.puehringer@nyu.edu
 * on 5/4/16.
 *
 * C Python module uses LocationService.java via JNI to return location
 * values
 *
 * Usage:
 * Module initialization - call initlocation() from C
 *  - Initializes Python module (location)
 *
 * Location initialization - call location_start_location from C
 *  - Calls start_location Java Method to register location update listener
 *      on location provider
 *
 * Get location values - call location.get_* from Python
 *  - Calls the according get* Method in Java
 *  - Return values are documented in LocationService.java
 *
 * Location de-initialization - call location_stop_location from C
 *  - Calls stop_location Java Method to unregister location update listener in
 *  - order to free resources
 *
 *  Note:
 *  Cf. sensors.c notes
 *
 */

#include "location.h"
/*
 * Calls Java to connect to Google API and register LocationListeners for GPS,
 * network and fused location provider
 *
 * Not in Python module - needs to be called from C!
 *
 */
void location_start_location() {
    jni_py_call(_void,
            cached_location_class, cached_location_get_instance,
            cached_location_start_location);
}


/*
 * Calls Java to unregister LocationListeners for GPS, network and fused
 * location provider and disconnect from Google API
 *
 * Not in Python module - needs to be called from C!
 *
 */
void location_stop_location() {
    jni_py_call(_void,
            cached_location_class, cached_location_get_instance,
            cached_location_stop_location);
}



/*
 * Cf. getLocation() in LocationService.java for details
 */
PyObject* location_get_location() {
    return jni_py_call(_json,
            cached_location_class, cached_location_get_instance,
            cached_location_get_location);
}


/*
 * Cf. getLastKnownLocation() in LocationService.java for details
 */
PyObject* location_get_lastknown_location() {
    return jni_py_call(_json,
            cached_location_class, cached_location_get_instance,
            cached_location_get_lastknown_location);
}


/*
 * Cf. getGeoLocation() in LocationService.java for details
 *
 * Arguments
 *   latitude (double)
 *   longitude (double)
 *   Max length of returned addresses list (int)
 *
 */
PyObject* location_get_geolocation(PyObject *self, PyObject *args) {

    jdouble latitude, longitude;
    jint max_results;

    if (!PyArg_ParseTuple(args, "ddi", &latitude, &longitude, &max_results)){
        LOGI("Wrong arguments. I wonder if I should raise an Exception.");
        Py_RETURN_NONE;
    }

    return jni_py_call(_json,
            cached_location_class, cached_location_get_instance,
            cached_location_get_lastknown_location, latitude, longitude, max_results);
}


/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidLocationMethods[] = {
    {"get_location", (PyCFunction) location_get_location, METH_NOARGS,
        "Get locations from GPS, Network and Fused"},
    {"get_lastknown_location",
        (PyCFunction) location_get_lastknown_location, METH_NOARGS,
        "Get last known locations from GPS, Network and Fused"},
    {"get_geolocation",
        (PyCFunction) location_get_geolocation, METH_VARARGS,
        "Get max count of address(es) from latitude and longitude"},
    {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


/*
 * Initializes Python module (location), looks up Java class and Java Methods
 * used to poll location values and stores them to cache
 *
 * Note:
 * If we wanted to build the module as .so or .dll we could
 * would have to change the signature to
 * PyMODINIT_FUNC initlocation(void)
 *
 */
void initlocation() {
    Py_InitModule("location", AndroidLocationMethods);
}