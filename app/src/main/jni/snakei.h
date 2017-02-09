#ifndef _SNAKEI_H_
#define _SNAKEI_H_

#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <Python.h>
#include <android/log.h>
#include "interpreter.h"
#include "outputs.h"
#include "sensors.h"
#include "pyhelper.h"
#include "miscinfo.h"

// Define a "convenience" macro for simple logging (see `adb logcat`)
// (taken from the Android NDK samples)
# define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, __FILE__, __VA_ARGS__))

/* Following the suggestions in
 * https://developer.android.com/training/articles/perf-jni.html#native_libraries,
 * we pre-cache the `JNIEnv` and classes that we will use in C code.
 * First, declare a few references we will populate soon and then reuse
 * in the course of our runtime.
 *
 * XXX:
 *   we need to cache the JNIEnv per thread, i.e. we shouldn't cache
 *   it here, because this gets called once by the parent thread that will
 *   spawn off other threads
 *
 *   The vm pointer on the other hand can be cached shared by all threads
 *   and therefor cached here
 *
 *   Java classes and methods are currently cached in the each extension module
 *   maybe this can be done here
 */
pthread_key_t jni_thread_key;

JavaVM* cached_vm;
jobject cached_context;

jclass cached_popen_class;
jmethodID cached_popen_python;

jclass cached_miscinfo_class;
jmethodID cached_miscinfo_get_instance;
jmethodID cached_miscinfo_init;
jmethodID cached_miscinfo_is_wifi_enabled;
jmethodID cached_miscinfo_get_wifi_state;
jmethodID cached_miscinfo_get_wifi_connection_info;
jmethodID cached_miscinfo_get_wifi_scan_info;
jmethodID cached_miscinfo_get_bluetooth_scan_info;
jmethodID cached_miscinfo_get_bluetooth_info;
jmethodID cached_miscinfo_get_battery_info;
jmethodID cached_miscinfo_get_network_info;
jmethodID cached_miscinfo_get_cellular_provider_info;
jmethodID cached_miscinfo_get_cell_info;
jmethodID cached_miscinfo_get_sim_info;
jmethodID cached_miscinfo_get_phone_info;
jmethodID cached_miscinfo_get_mode_settings;
jmethodID cached_miscinfo_get_display_info;
jmethodID cached_miscinfo_get_volume_info;

jclass cached_sensor_class;
jmethodID cached_sensor_get_instance;
jmethodID cached_sensor_init;
jmethodID cached_sensor_start_sensing;
jmethodID cached_sensor_stop_sensing;
jmethodID cached_sensor_get_sensor_list;
jmethodID cached_sensor_get_acceleration;
jmethodID cached_sensor_get_ambient_temperature;
jmethodID cached_sensor_get_game_rotation_vector;
jmethodID cached_sensor_get_geomagnetic_rotation_vector;
jmethodID cached_sensor_get_gravity;
jmethodID cached_sensor_get_gyroscope;
jmethodID cached_sensor_get_gyroscope_uncalibrated;
jmethodID cached_sensor_get_heart_rate;
jmethodID cached_sensor_get_light;
jmethodID cached_sensor_get_linear_acceleration;
jmethodID cached_sensor_get_magnetic_field;
jmethodID cached_sensor_get_magnetic_field_uncalibrated;
jmethodID cached_sensor_get_pressure;
jmethodID cached_sensor_get_proximity;
jmethodID cached_sensor_get_relative_humidity;
jmethodID cached_sensor_get_rotation_vector;
jmethodID cached_sensor_get_step_counter;

jclass cached_location_class;
jmethodID cached_location_start_location;
jmethodID cached_location_stop_location;
jmethodID cached_location_get_instance;
jmethodID cached_location_init;
jmethodID cached_location_get_location;
jmethodID cached_location_get_geolocation;
jmethodID cached_location_get_lastknown_location;

jclass cached_media_class;
jmethodID cached_media_start_media;
jmethodID cached_media_stop_media;
jmethodID cached_media_get_instance;
jmethodID cached_media_init;
jmethodID cached_media_microphone_record;
jmethodID cached_media_tts_speak;
jmethodID cached_media_is_tts_speaking;
jmethodID cached_media_is_media_playing;

jclass cached_output_class;
jmethodID cached_output_toast;
jmethodID cached_output_prompt;
jmethodID cached_output_notify;


#endif /* defined _SNAKEI_H_ */

