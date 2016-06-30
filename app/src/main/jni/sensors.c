#include "sensors.h"
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

static struct sensor_cache {
    jclass class;
    jmethodID get_instance;
    jmethodID start_sensing;
    jmethodID stop_sensing;
    jmethodID get_sensor_list;
    jmethodID get_acceleration;
    jmethodID get_ambient_temperature;
    jmethodID get_game_rotation_vector;
    jmethodID get_geomagnetic_rotation_vector;
    jmethodID get_gravity;
    jmethodID get_gyroscope;
    jmethodID get_gyroscope_uncalibrated;
    jmethodID get_heart_rate;
    jmethodID get_light;
    jmethodID get_linear_acceleration;
    jmethodID get_magnetic_field;
    jmethodID get_magnetic_field_uncalibrated;
    jmethodID get_pressure;
    jmethodID get_proximity;
    jmethodID get_relative_humidity;
    jmethodID get_rotation_vector;
    jmethodID get_step_counter;
} m_cached;

/*
 * Start a sensor by registering a SensorEventHandler in Java
 */
void sensor_start_sensing(int sensor_type) {
//    LOGI("Let's fire the sensor up...");
    jh_call(m_cached.class, m_cached.get_instance, jh_callVoidMethod,
                   m_cached.start_sensing, (jint) sensor_type);
}
/*
 * Stop a sensor by un-registering a SensorEventHandler in Java
 */
void sensor_stop_sensing(int sensor_type) {
//    LOGI("Let's shut down the sensor...");
    jh_call(m_cached.class, m_cached.get_instance, jh_callVoidMethod,
                   m_cached.stop_sensing, (jint) sensor_type);
}

/*
 *
 * Python extension to get a list of sensor info
 * for each available sensor.
 *
 * Returns
 *  [{"name" : <name>, "vendor":<vendor>, ...},..]
 *
 */

PyObject* sensor_get_sensor_list(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
            m_cached.get_sensor_list);
}

PyObject* sensor_get_acceleration(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_acceleration);
}

PyObject* sensor_get_ambient_temperature(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_ambient_temperature);
}

PyObject* sensor_get_game_rotation_vector(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_game_rotation_vector);
}

PyObject* sensor_get_geomagnetic_rotation_vector(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_geomagnetic_rotation_vector);
}

PyObject* sensor_get_gravity(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_gravity);
}

PyObject* sensor_get_gyroscope(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_gyroscope);
}

PyObject* sensor_get_gyroscope_uncalibrated(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_gyroscope_uncalibrated);
}

PyObject* sensor_get_heart_rate(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_heart_rate);
}

PyObject* sensor_get_light(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_light);
}
PyObject* sensor_get_linear_acceleration(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_linear_acceleration);
}

PyObject* sensor_get_magnetic_field(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_magnetic_field);
}

PyObject* sensor_get_magnetic_field_uncalibrated(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_magnetic_field_uncalibrated);
}

PyObject* sensor_get_pressure(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_pressure);
}

PyObject* sensor_get_proximity(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_proximity);
}

PyObject* sensor_get_relative_humidity(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_relative_humidity);
}

PyObject* sensor_get_rotation_vector(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_rotation_vector);
}

PyObject* sensor_get_step_counter(PyObject *self) {
    return jh_call(m_cached.class, m_cached.get_instance, jh_callJsonStringMethod,
                   m_cached.get_step_counter);
}


// Only functions taking two PyObject* arguments are PyCFunction
// where this is not the case we need to cast
// Todo: write descriptions
static PyMethodDef AndroidSensorMethods[] = {
        {"get_sensor_list", (PyCFunction) sensor_get_sensor_list, METH_NOARGS,
         "Get a list of sensor info dictionaries."},
        {"get_acceleration", (PyCFunction) sensor_get_acceleration, METH_NOARGS,
         "Get list of accelerator values. [sample ts, poll ts, x, y, z]"},
        {"get_ambient_temperature", (PyCFunction) sensor_get_ambient_temperature, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_game_rotation_vector", (PyCFunction) sensor_get_game_rotation_vector, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_geomagnetic_rotation_vector", (PyCFunction) sensor_get_geomagnetic_rotation_vector, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_gravity", (PyCFunction) sensor_get_gravity, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_gyroscope", (PyCFunction) sensor_get_gyroscope, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_gyroscope_uncalibrated", (PyCFunction) sensor_get_gyroscope_uncalibrated, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_heart_rate", (PyCFunction) sensor_get_heart_rate, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_light", (PyCFunction) sensor_get_light, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_linear_acceleration", (PyCFunction) sensor_get_linear_acceleration, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_magnetic_field", (PyCFunction) sensor_get_magnetic_field, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_magnetic_field_uncalibrated", (PyCFunction) sensor_get_magnetic_field_uncalibrated, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_pressure", (PyCFunction) sensor_get_pressure, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_proximity", (PyCFunction) sensor_get_proximity, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_relative_humidity", (PyCFunction) sensor_get_relative_humidity, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_rotation_vector", (PyCFunction) sensor_get_rotation_vector, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {"get_step_counter", (PyCFunction) sensor_get_step_counter, METH_NOARGS,
         "XXXXXXXXXXXXXXXXXXXXXx"},
        {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


//PyMODINIT_FUNC initsensor(void) {
void initsensor() {
    Py_InitModule("sensor", AndroidSensorMethods);
    jclass class = jh_getClass("com/snakei/SensorService");

    m_cached = (struct sensor_cache){
            .class = class,
            .get_instance = jh_getGetter(class, "()Lcom/snakei/SensorService;"),
            .start_sensing = jh_getMethod(class, "start_sensing", "(I)V"),
            .stop_sensing = jh_getMethod(class, "stop_sensing", "(I)V"),
            .get_sensor_list = jh_getMethod(class, "getSensorList", "()Ljava/lang/String;"),
            .get_acceleration = jh_getMethod(class, "getAcceleration", "()Ljava/lang/String;"),
            .get_ambient_temperature = jh_getMethod(class, "getAmbientTemperature",
                                                    "()Ljava/lang/String;"),
            .get_game_rotation_vector = jh_getMethod(class, "getGameRotationVector",
                                                     "()Ljava/lang/String;"),
            .get_geomagnetic_rotation_vector = jh_getMethod(class, "getGeomagneticRotationVector",
                                                            "()Ljava/lang/String;"),
            .get_gravity = jh_getMethod(class, "getGravity", "()Ljava/lang/String;"),
            .get_gyroscope = jh_getMethod(class, "getGyroscope", "()Ljava/lang/String;"),
            .get_gyroscope_uncalibrated = jh_getMethod(class, "getGyroscopeUncalibrated",
                                                       "()Ljava/lang/String;"),
            .get_heart_rate = jh_getMethod(class, "getHeartRate", "()Ljava/lang/String;"),
            .get_light = jh_getMethod(class, "getLight", "()Ljava/lang/String;"),
            .get_linear_acceleration = jh_getMethod(class, "getLinearAcceleration",
                                                    "()Ljava/lang/String;"),
            .get_magnetic_field = jh_getMethod(class, "getMagneticField",
                                               "()Ljava/lang/String;"),
            .get_magnetic_field_uncalibrated = jh_getMethod(class, "getMagneticFieldUncalibrated",
                                                            "()Ljava/lang/String;"),
            .get_pressure = jh_getMethod(class, "getPressure", "()Ljava/lang/String;"),
            .get_proximity = jh_getMethod(class, "getProximity", "()Ljava/lang/String;"),
            .get_relative_humidity = jh_getMethod(class, "getRelativeHumidity",
                                                  "()Ljava/lang/String;"),
            .get_rotation_vector = jh_getMethod(class, "getRotationVector",
                                                "()Ljava/lang/String;"),
            .get_step_counter = jh_getMethod(class, "getStepCounter", "()Ljava/lang/String;")};
}
