/*
 * Created by lukas.puehringer@nyu.edu
 * on 6/3/16.
 *
 * C Python module uses MiscInfoService.java via JNI to return miscellaneous
 * information about the Android device
 *
 * Usage:
 * Module initialization - call initmiscinfo() from C
 *  - Initializes Python module (miscinfo)
 *
 * Get miscinfo values - call miscinfo.get_* from Python
 *  - Calls the according get* method in Java
 *  - Return values are documented in MiscInfoService.java
 *
 *  Note:
 *  Cf. sensors.c notes
 *
 */

#include "miscinfo.h"

PyObject* miscinfo_init() {
    jni_py_call(_void,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_init, cached_context);
}

/*
 * Cf. getBluetoothInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_bluetooth_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_bluetooth_info);
}


/*
 * Cf. getBluetoothScanInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_bluetooth_scan_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_bluetooth_scan_info);
}


/*
 * Cf. isWifiEnabled() in MiscInfoService.java for details
 */
PyObject* miscinfo_is_wifi_enabled(PyObject *self) {
    return jni_py_call(_boolean,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_is_wifi_enabled);
}


/*
 * Cf. getWifiState() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_wifi_state(PyObject *self) {
    return jni_py_call(_int,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_wifi_state);
}


/*
 * Cf. getWifiConnectionInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_wifi_connection_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_wifi_connection_info);
}


/*
 * Cf. getWifiScanInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_wifi_scan_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_wifi_scan_info);
}


/*
 * Cf. getNetworkInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_network_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_network_info);
}


/*
 * Cf. getCellularProviderInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_cellular_provider_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_cellular_provider_info);
}


/*
 * Cf. getCellInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_cell_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_cell_info);
}


/*
 * Cf. getSimInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_sim_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_sim_info);
}


/*
 * Cf. getPhoneInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_phone_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_phone_info);
}


/*
 * Cf. getModeSettings() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_mode_settings(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_mode_settings);
}


/*
 * Cf. getDisplayInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_display_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_display_info);
}


/*
 * Cf. getVolumeInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_volume_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_volume_info);
}


/*
 * Cf. getBatteryInfo() in MiscInfoService.java for details
 */
PyObject* miscinfo_get_battery_info(PyObject *self) {
    return jni_py_call(_json,
            cached_miscinfo_class, cached_miscinfo_get_instance,
            cached_miscinfo_get_battery_info);
}


/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidMiscinfoMethods[] = {
        {"is_wifi_enabled",
         (PyCFunction) miscinfo_is_wifi_enabled, METH_NOARGS,
         "True if WiFi is enabled, False otherwise"},
        {"get_wifi_state", (PyCFunction) miscinfo_get_wifi_state, METH_NOARGS,
         "Returns state of WiFi"},
        {"get_wifi_connection_info",
         (PyCFunction) miscinfo_get_wifi_connection_info, METH_NOARGS,
         "Returns info about WiFi currently connected WiFi "},
        {"get_wifi_scan_info",
         (PyCFunction) miscinfo_get_wifi_scan_info, METH_NOARGS,
         "Performs WiFi scan and returns info about scanned WiFis"},
        {"get_bluetooth_info",
         (PyCFunction) miscinfo_get_bluetooth_info, METH_NOARGS,
         "Returns info about bluetooth interface on the device"},
        {"get_bluetooth_scan_info",
         (PyCFunction) miscinfo_get_bluetooth_scan_info, METH_NOARGS,
         "Performs BT discovery and returns info about discovered devices"},
        {"get_network_info",
         (PyCFunction) miscinfo_get_network_info, METH_NOARGS,
         "Returns network info (WiFi, GPRS, UMTS, etc.)"},
        {"get_cellular_provider_info",
         (PyCFunction) miscinfo_get_cellular_provider_info, METH_NOARGS,
         "Returns cellular provider info"},
        {"get_cell_info", (PyCFunction) miscinfo_get_cell_info, METH_NOARGS,
         "Returns observed cell information from all radios on the device"},
        {"get_sim_info", (PyCFunction) miscinfo_get_sim_info, METH_NOARGS,
         "Returns Subscriber Identity Module (SIM)"},
        {"get_phone_info", (PyCFunction) miscinfo_get_phone_info, METH_NOARGS,
         "Returns info about the phone"},
        {"get_mode_settings",
         (PyCFunction) miscinfo_get_mode_settings, METH_NOARGS,
         "Returns ringer mode and whether airplane mode is on or off"},
        {"get_display_info",
         (PyCFunction) miscinfo_get_display_info, METH_NOARGS,
         "Returns info about the device display"},
        {"get_volume_info",
         (PyCFunction) miscinfo_get_volume_info, METH_NOARGS,
         "Returns info about media and ringer volume"},
        {"get_battery_info",
         (PyCFunction) miscinfo_get_battery_info, METH_NOARGS,
         "Returns info about device battery"},
        {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


/*
 * Initializes Python module (miscinfo)
 *
 * Note:
 * If we wanted to build the module as .so or .dll we could
 * would have to change the signature to
 * PyMODINIT_FUNC initmiscinfo(void)
 *
 */
void miscinfo_init_pymodule() {
    Py_InitModule("miscinfo", AndroidMiscinfoMethods);
}