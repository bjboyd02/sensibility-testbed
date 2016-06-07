//
// Created by lukas on 6/3/16.
//

#include "miscinfo.h"

static struct miscinfo_cache {
    jclass class;
    jmethodID get_instance;
    jmethodID is_wifi_enabled;
    jmethodID get_wifi_state;
    jmethodID get_battery_info;
} m_cached;

//PyObject* miscinfo_jsontest(PyObject *self) {
//    char *jsonstring = "[{\"lastName\": \"Doe\", \"age\": 45, \"somethingfloat\": 1.55, \"firstName\": \"John\"}, {\"lastName\": \"Smith\", \"age\": 18, \"somethingfloat\": 0.5, \"firstName\": \"Anna\"}]";
//    PyObject *obj = JSON_decode_c(jsonstring);
//    return obj;
//}

void miscinfo_start_miscinfo() {
    JNIEnv *jni_env;
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jclass class = jh_getClass(jni_env, "com/snakei/MiscInfoService");

    jmethodID get_instance = jh_getGetter(jni_env, class, "()Lcom/snakei/MiscInfoService;");
    jobject instance = jh_getInstance(jni_env, class, get_instance);

    jmethodID is_wifi_enabled = jh_getMethod(jni_env, class, "isWifiEnabled", "()Z");
    jmethodID get_wifi_state = jh_getMethod(jni_env, class, "getWifiState", "()I");
    jmethodID get_battery_info = jh_getMethod(jni_env, class, "getBatteryInfo", "()Ljava/lang/String;");

    m_cached = (struct miscinfo_cache){.class = class, .get_instance = get_instance,
            .is_wifi_enabled = is_wifi_enabled, .get_wifi_state = get_wifi_state,
            .get_battery_info = get_battery_info};
}

PyObject* miscinfo_is_wifi_enabled(PyObject *self) {
    JNIEnv *jni_env;
    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
    PyObject* is_wifi_enabled = jh_callBooleanMethod(jni_env, instance, m_cached.is_wifi_enabled);

    (*jni_env)->DeleteLocalRef(jni_env, instance);

    return is_wifi_enabled;
}

PyObject* miscinfo_get_wifi_state(PyObject *self) {
    JNIEnv *jni_env;
    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
    PyObject* is_wifi_enabled = jh_callIntMethod(jni_env, instance, m_cached.get_wifi_state);

    (*jni_env)->DeleteLocalRef(jni_env, instance);

    return is_wifi_enabled;
}

PyObject* miscinfo_get_battery_info(PyObject *self) {
    JNIEnv *jni_env;
    // Use the cached JVM pointer to get a new environment
    (*cached_vm)->AttachCurrentThread(cached_vm, &jni_env, NULL);
    jobject instance = jh_getInstance(jni_env, m_cached.class, m_cached.get_instance);
    PyObject* battery_info = jh_callJsonStringMethod(jni_env, instance, m_cached.get_battery_info);

    (*jni_env)->DeleteLocalRef(jni_env, instance);

    return battery_info;
}