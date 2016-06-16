//
// Created by lukas on 6/3/16.
//

#include "miscinfo.h"

static struct miscinfo_cache {
    jclass class;
    jmethodID get_instance;
    jmethodID is_wifi_enabled;
    jmethodID get_wifi_state;
    jmethodID get_wifi_connection_info;
    jmethodID get_battery_info;
    jmethodID get_wifi_scan_info;
    jmethodID get_bluetooth_info;
    jmethodID get_bluetooth_scan_info;
    jmethodID get_network_info;
    jmethodID get_cellular_provider_info;
    jmethodID get_cell_info;
    jmethodID get_sim_info;
    jmethodID get_phone_info;
    jmethodID get_mode_settings;
    jmethodID get_display_info;
    jmethodID get_volume_info;
} m_cached;

void miscinfo_start_miscinfo() {
    JNIEnv *jni_env;
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jclass class = jh_getClass(jni_env, "com/snakei/MiscInfoService");
    jmethodID get_instance = jh_getGetter(jni_env, class, "()Lcom/snakei/MiscInfoService;");
    jobject instance = jh_getInstance(jni_env, class, get_instance);

    m_cached = (struct miscinfo_cache){
            .class = class,
            .get_instance = get_instance,
            .is_wifi_enabled = jh_getMethod(jni_env, class, "isWifiEnabled", "()Z"),
            .get_wifi_state = jh_getMethod(jni_env, class, "getWifiState", "()I"),
            .get_wifi_connection_info = jh_getMethod(jni_env, class, "getWifiConnectionInfo", "()Ljava/lang/String;"),
            .get_wifi_scan_info = jh_getMethod(jni_env, class, "getWifiScanInfo", "()Ljava/lang/String;"),
            .get_bluetooth_scan_info = jh_getMethod(jni_env, class, "getBluetoothScanInfo", "()Ljava/lang/String;"),
            .get_bluetooth_info = jh_getMethod(jni_env, class, "getBluetoothInfo", "()Ljava/lang/String;"),
            .get_battery_info = jh_getMethod(jni_env, class,  "getBatteryInfo", "()Ljava/lang/String;"),
            .get_network_info = jh_getMethod(jni_env, class, "getNetworkInfo", "()Ljava/lang/String;"),
            .get_cellular_provider_info = jh_getMethod(jni_env, class, "getCellularProviderInfo", "()Ljava/lang/String;"),
            .get_cell_info = jh_getMethod(jni_env, class, "getCellInfo", "()Ljava/lang/String;"),
            .get_sim_info = jh_getMethod(jni_env, class, "getSimInfo", "()Ljava/lang/String;"),
            .get_phone_info = jh_getMethod(jni_env, class, "getPhoneInfo", "()Ljava/lang/String;"),
            .get_mode_settings = jh_getMethod(jni_env, class, "getModeSettings", "()Ljava/lang/String;"),
            .get_display_info = jh_getMethod(jni_env, class, "getDisplayInfo", "()Ljava/lang/String;"),
            .get_volume_info = jh_getMethod(jni_env, class, "getVolumeInfo", "()Ljava/lang/String;")
    };
}

PyObject* _miscinfo_get_info(PyObject* (*jh_call)(JNIEnv*, jobject, jmethodID, ...), jmethodID cached_method, ...) {
    JNIEnv *jni_env;
    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);

    // Get the instance
    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);

    // Call the JNI function in
    PyObject* info;
    va_list args;
    va_start(args, cached_method);
    info = (*jh_call)(jni_env, instance, cached_method, args);
    va_end(args);

    (*jni_env)->DeleteLocalRef(jni_env, instance);

    return info;
}

PyObject* miscinfo_get_bluetooth_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_bluetooth_info);
}

PyObject* miscinfo_get_bluetooth_scan_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_bluetooth_scan_info);
}

PyObject* miscinfo_is_wifi_enabled(PyObject *self) {
    return _miscinfo_get_info(jh_callBooleanMethod, m_cached.is_wifi_enabled);
}

PyObject* miscinfo_get_wifi_state(PyObject *self) {
    return _miscinfo_get_info(jh_callIntMethod, m_cached.get_wifi_state);
}
PyObject* miscinfo_get_wifi_connection_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_wifi_connection_info);
}

PyObject* miscinfo_get_wifi_scan_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_wifi_scan_info);
}

PyObject* miscinfo_get_network_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_network_info);
}

PyObject* miscinfo_get_cellular_provider_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_cellular_provider_info);
}

PyObject* miscinfo_get_cell_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_cell_info);
}

PyObject* miscinfo_get_sim_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_sim_info);
}

PyObject* miscinfo_get_phone_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_phone_info);
}

PyObject* miscinfo_get_mode_settings(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_mode_settings);
}

PyObject* miscinfo_get_display_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_display_info);
}

PyObject* miscinfo_get_volume_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_volume_info);
}

PyObject* miscinfo_get_battery_info(PyObject *self) {
    return _miscinfo_get_info(jh_callJsonStringMethod, m_cached.get_battery_info);
}