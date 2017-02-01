#include "snakei.h"

/*
 * This function is called when the Java code does `System.loadLibrary()`.
 * See https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html#JNI_OnLoad
 *
 * We use it to cache the native reference to the JVM which will be
 * used by all snakei extensions
 */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *jni_env;

    // Cache vm
    cached_vm = vm;

    // Define thread "destructor"
    if (pthread_key_create(&jni_thread_key, jni_detach_current_thread) != 0) {
        LOGI( "Error initializing pthread key");
    }

    /* Cache popen */
    cached_popen_class = jni_find_class_as_global(
        "com/snakei/PythonInterpreterService");
    cached_popen_python = jni_find_static_method(
            cached_popen_class, "startService",
            "([Ljava/lang/String;Landroid/content/Context;)V");

    /* Cache miscinfo */
    cached_miscinfo_class = jni_find_class_as_global(
            "com/snakei/MiscInfoService");
    cached_miscinfo_get_instance = jni_find_getter(
            cached_miscinfo_class, "()Lcom/snakei/MiscInfoService;");
    cached_miscinfo_init = jni_find_method(
            cached_miscinfo_class,"init","(Landroid/content/Context;)V");
    cached_miscinfo_is_wifi_enabled = jni_find_method(
            cached_miscinfo_class,"isWifiEnabled","()Z");
    cached_miscinfo_get_wifi_state = jni_find_method(
            cached_miscinfo_class, "getWifiState", "()I");
    cached_miscinfo_get_wifi_connection_info = jni_find_method(
            cached_miscinfo_class, "getWifiConnectionInfo",
            "()Ljava/lang/String;");
    cached_miscinfo_get_wifi_scan_info = jni_find_method(
            cached_miscinfo_class, "getWifiScanInfo", "()Ljava/lang/String;");
    cached_miscinfo_get_bluetooth_scan_info = jni_find_method(
            cached_miscinfo_class, "getBluetoothScanInfo",
            "()Ljava/lang/String;");
    cached_miscinfo_get_bluetooth_info = jni_find_method(
            cached_miscinfo_class, "getBluetoothInfo", "()Ljava/lang/String;");
    cached_miscinfo_get_battery_info = jni_find_method(
            cached_miscinfo_class, "getBatteryInfo",
            "()Ljava/lang/String;");
    cached_miscinfo_get_network_info = jni_find_method(
            cached_miscinfo_class, "getNetworkInfo", "()Ljava/lang/String;");
    cached_miscinfo_get_cellular_provider_info = jni_find_method(
            cached_miscinfo_class, "getCellularProviderInfo",
            "()Ljava/lang/String;");
    cached_miscinfo_get_cell_info = jni_find_method(
            cached_miscinfo_class, "getCellInfo", "()Ljava/lang/String;");
    cached_miscinfo_get_sim_info = jni_find_method(
            cached_miscinfo_class, "getSimInfo", "()Ljava/lang/String;");
    cached_miscinfo_get_phone_info = jni_find_method(
            cached_miscinfo_class, "getPhoneInfo", "()Ljava/lang/String;");
    cached_miscinfo_get_mode_settings = jni_find_method(
            cached_miscinfo_class, "getModeSettings", "()Ljava/lang/String;");
    cached_miscinfo_get_display_info = jni_find_method(
            cached_miscinfo_class, "getDisplayInfo", "()Ljava/lang/String;");
    cached_miscinfo_get_volume_info = jni_find_method(
            cached_miscinfo_class, "getVolumeInfo", "()Ljava/lang/String;");

    /* Cache sensor */
    cached_sensor_class = jni_find_class_as_global("com/snakei/SensorService");
    cached_sensor_get_instance = jni_find_getter(
            cached_sensor_class, "()Lcom/snakei/SensorService;");
    cached_sensor_init = jni_find_method(
            cached_sensor_class,"init","(Landroid/content/Context;)V");
    cached_sensor_start_sensing = jni_find_method(
            cached_sensor_class, "start_sensing", "(I)V");
    cached_sensor_stop_sensing = jni_find_method(
            cached_sensor_class, "stop_sensing", "(I)V");
    cached_sensor_get_sensor_list = jni_find_method(
            cached_sensor_class, "getSensorList", "()Ljava/lang/String;");
    cached_sensor_get_acceleration = jni_find_method(
            cached_sensor_class, "getAcceleration", "()Ljava/lang/String;");
    cached_sensor_get_ambient_temperature = jni_find_method(
            cached_sensor_class,"getAmbientTemperature",
            "()Ljava/lang/String;");
    cached_sensor_get_game_rotation_vector = jni_find_method(
            cached_sensor_class, "getGameRotationVector",
            "()Ljava/lang/String;");
    cached_sensor_get_geomagnetic_rotation_vector = jni_find_method(
            cached_sensor_class, "getGeomagneticRotationVector",
            "()Ljava/lang/String;");
    cached_sensor_get_gravity = jni_find_method(
            cached_sensor_class, "getGravity", "()Ljava/lang/String;");
    cached_sensor_get_gyroscope = jni_find_method(
            cached_sensor_class, "getGyroscope", "()Ljava/lang/String;");
    cached_sensor_get_gyroscope_uncalibrated = jni_find_method(
            cached_sensor_class, "getGyroscopeUncalibrated",
            "()Ljava/lang/String;");
    cached_sensor_get_heart_rate = jni_find_method(
            cached_sensor_class, "getHeartRate", "()Ljava/lang/String;");
    cached_sensor_get_light = jni_find_method(
            cached_sensor_class, "getLight", "()Ljava/lang/String;");
    cached_sensor_get_linear_acceleration = jni_find_method(
            cached_sensor_class, "getLinearAcceleration",
            "()Ljava/lang/String;");
    cached_sensor_get_magnetic_field = jni_find_method(
            cached_sensor_class, "getMagneticField","()Ljava/lang/String;");
    cached_sensor_get_magnetic_field_uncalibrated = jni_find_method(
            cached_sensor_class, "getMagneticFieldUncalibrated",
            "()Ljava/lang/String;");
    cached_sensor_get_pressure = jni_find_method(
            cached_sensor_class, "getPressure", "()Ljava/lang/String;");
    cached_sensor_get_proximity = jni_find_method(
            cached_sensor_class, "getProximity", "()Ljava/lang/String;");
    cached_sensor_get_relative_humidity = jni_find_method(
            cached_sensor_class, "getRelativeHumidity","()Ljava/lang/String;");
    cached_sensor_get_rotation_vector = jni_find_method(
            cached_sensor_class, "getRotationVector","()Ljava/lang/String;");
    cached_sensor_get_step_counter = jni_find_method(
            cached_sensor_class, "getStepCounter", "()Ljava/lang/String;");

    /* Cache location */
    cached_location_class = jni_find_class_as_global(
            "com/snakei/LocationService");
    cached_location_start_location = jni_find_method(
            cached_location_class, "start_location", "()V");
    cached_location_stop_location = jni_find_method(
            cached_location_class, "stop_location", "()V");
    cached_location_init = jni_find_method(
            cached_location_class,"init","(Landroid/content/Context;)V");
    cached_location_get_instance = jni_find_getter(
            cached_location_class, "()Lcom/snakei/LocationService;");
    cached_location_get_location = jni_find_method(
            cached_location_class, "getLocation", "()Ljava/lang/String;");
    cached_location_get_geolocation = jni_find_method(
            cached_location_class, "getGeoLocation", "(DDI)Ljava/lang/String;");
    cached_location_get_lastknown_location = jni_find_method(
            cached_location_class, "getLastKnownLocation",
            "()Ljava/lang/String;");

    /* Cache media */
    cached_media_class = jni_find_class_as_global("com/snakei/MediaService");
    cached_media_start_media = jni_find_method(
            cached_media_class, "start_media", "()V");
    cached_media_stop_media = jni_find_method(
            cached_media_class, "stop_media", "()V");
    cached_media_get_instance = jni_find_getter(
            cached_media_class, "()Lcom/snakei/MediaService;");
    cached_media_init = jni_find_method(
            cached_media_class,"init","(Landroid/content/Context;)V");
    cached_media_microphone_record = jni_find_method(
            cached_media_class, "microphoneRecord","(Ljava/lang/String;I)V");
    cached_media_tts_speak = jni_find_method(
            cached_media_class,"ttsSpeak","(Ljava/lang/String;)V");
    cached_media_is_tts_speaking = jni_find_method(
            cached_media_class, "isTtsSpeaking", "()Z");
    cached_media_is_media_playing = jni_find_method(
            cached_media_class, "isMediaPlaying", "()Z");

    /* Cache outputs */
    cached_output_class = jni_find_class_as_global("com/snakei/OutputService");
    cached_output_toast = jni_find_static_method(
            cached_output_class, "toastMessage",
            "(Landroid/content/Context;Ljava/lang/String;)V");
    cached_output_prompt = jni_find_static_method(
            cached_output_class, "promptMessage",
            "(Landroid/content/Context;Ljava/lang/String;)Z");

    return JNI_VERSION_1_6;
}


