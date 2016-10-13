/*
 * Created by
 * albert.rafetseder@univie.ac.at
 * lukas.puehringer@nyu.edu
 * on 5/29/16
 *
 * C Python module uses SensorService.java via JNI to return sensor
 * values
 *
 * Usage:
 * Module initialization - call initsensor() from C
 *  - Initializes Python module (sensor)
 *
 * Sensor initialization - call sensor_start_sensing from C
 *  - Calls start_sensing Java Method for a specific Sensor to register Sensor
 *    update listener
 *  - Cf. SensorService.java constants CUSTOM_TYPE_* for a list of all sensors
 *
 * Get Sensor values - call sensor.get_* from Python
 *  - Calls the according get* Method in Java
 *  - Return values are documented in SensorService.java
 *
 * Sensor de-initialization - call sensor_stop_sensing from C
 *  - Calls stop_sensing Java Method for specific Sensor to unregister
 *    Sensor update listener in order to free resources
 *
 * Note:
 * We need to think of
 *  - acquiring and releasing resources
 *  - dealing with multiple sensors for one type
 *  - dealing with multiple threads concurrently accessing a sensor
 *
 * Possible other approaches:
 *  - One function for all sensors, like in current Repy Sensor API [1]
 *  - Pass callback functions (cool but harder) that receive the sensor values
 *    from Java on update
 *    e.g.:
 *      start_sensing(TYPE_ACCELEROMETER, callback_fn_receives_sensor_values)
 *      stop_sensing(TYPE_ACCELEROMETER)
 *  - Go OO, like Yocto Python API[2] (not really pythonic, is it?)
 *    e.g.:
 *      sensor = find_sensor(TYPE_ACCELEROMETER)
 *      sensor.start()
 *      sensor.getValue()
 *      sensor.stop()
 *
 * [1] https://sensibilitytestbed.com/projects/project/wiki/sensors#Sensors
 * [2] http://www.yoctopuce.com/EN/products/yocto-meteo/doc/
 *              METEOMK1.usermanual.html#CHAP14
 *
 */
#include "sensors.h"

/*
 * Calls Java to register a SensorEventListener for a specific
 * sensor to receive Sensor updates
 * Cf. SensorService.java constants CUSTOM_TYPE_* for a list of all sensors
 *
 * Not in Python module - needs to be called from C!
 *
 */
void sensor_start_sensing(int sensor_type) {
    jni_py_call(_void,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_start_sensing, (jint) sensor_type);
}


/*
 * Calls Java to unregister the SensorEventListener of a specific
 * sensor to stop receiving Sensor updates
 * Cf. SensorService.java constants CUSTOM_TYPE_* for a list of all sensors
 *
 * Not in Python module - needs to be called from C!
 */
void sensor_stop_sensing(int sensor_type) {
    jni_py_call(_void,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_stop_sensing, (jint) sensor_type);
}


/*
 * Cf. getSensorList() in SensorService.java for details
 */
PyObject* sensor_get_sensor_list(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_sensor_list);
}


/*
 * Cf. getAcceleration() in SensorService.java for details
 */
PyObject* sensor_get_acceleration(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_acceleration);
}


/*
 * Cf. getAmbientTemperature() in SensorService.java for details
 */
PyObject* sensor_get_ambient_temperature(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_ambient_temperature);
}


/*
 * Cf. getGameRotationVector() in SensorService.java for details
 */
PyObject* sensor_get_game_rotation_vector(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_game_rotation_vector);
}


/*
 * Cf. getGeomagneticRotationVector() in SensorService.java for details
 */
PyObject* sensor_get_geomagnetic_rotation_vector(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_geomagnetic_rotation_vector);
}


/*
 * Cf. getGravity() in SensorService.java for details
 */
PyObject* sensor_get_gravity(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_gravity);
}


/*
 * Cf. getGyroscope() in SensorService.java for details
 */
PyObject* sensor_get_gyroscope(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_gyroscope);
}


/*
 * Cf. getGyroscopeUncalibrated() in SensorService.java for details
 */
PyObject* sensor_get_gyroscope_uncalibrated(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_gyroscope_uncalibrated);
}


/*
 * Cf. getHeartRate() in SensorService.java for details
 */
PyObject* sensor_get_heart_rate(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_heart_rate);
}


/*
 * Cf. getLight() in SensorService.java for details
 */
PyObject* sensor_get_light(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_light);
}


/*
 * Cf. getLinearAcceleration() in SensorService.java for details
 */
PyObject* sensor_get_linear_acceleration(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_linear_acceleration);
}


/*
 * Cf. getMagneticField() in SensorService.java for details
 */
PyObject* sensor_get_magnetic_field(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_magnetic_field);
}


/*
 * Cf. getMagneticFieldUncalibrated() in SensorService.java for details
 */
PyObject* sensor_get_magnetic_field_uncalibrated(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_magnetic_field_uncalibrated);
}


/*
 * Cf. getPressure() in SensorService.java for details
 */
PyObject* sensor_get_pressure(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_pressure);
}


/*
 * Cf. getProximity() in SensorService.java for details
 */
PyObject* sensor_get_proximity(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_proximity);
}


/*
 * Cf. getRelativeHumidity() in SensorService.java for details
 */
PyObject* sensor_get_relative_humidity(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_relative_humidity);
}


/*
 * Cf. getRotationVector() in SensorService.java for details
 */
PyObject* sensor_get_rotation_vector(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_rotation_vector);
}


/*
 * Cf. getStepCounter() in SensorService.java for details
 */
PyObject* sensor_get_step_counter(PyObject *self) {
    return jni_py_call(_json,
            cached_sensor_class, cached_sensor_get_instance,
            cached_sensor_get_step_counter);
}


/*
 * Maps C functions to Python module methods
 */
static PyMethodDef AndroidSensorMethods[] = {
        {"get_sensor_list", (PyCFunction) sensor_get_sensor_list, METH_NOARGS,
         "Returns list of sensor info dictionaries"},
        {"get_acceleration",
        (PyCFunction) sensor_get_acceleration, METH_NOARGS,
         "Returns list of acceleration values"},
        {"get_ambient_temperature",
        (PyCFunction) sensor_get_ambient_temperature, METH_NOARGS,
         "Returns list of ambient temperature values"},
        {"get_game_rotation_vector",
        (PyCFunction) sensor_get_game_rotation_vector, METH_NOARGS,
         "Returns list of rotation values"},
        {"get_geomagnetic_rotation_vector",
        (PyCFunction) sensor_get_geomagnetic_rotation_vector, METH_NOARGS,
         "Returns list of geomagnetic rotation vector values"},
        {"get_gravity", (PyCFunction) sensor_get_gravity, METH_NOARGS,
         "Returns list of gravity values"},
        {"get_gyroscope", (PyCFunction) sensor_get_gyroscope, METH_NOARGS,
         "Return list of gyroscope values"},
        {"get_gyroscope_uncalibrated",
        (PyCFunction) sensor_get_gyroscope_uncalibrated, METH_NOARGS,
         "Return list of uncalibrated gyroscope values"},
        {"get_heart_rate", (PyCFunction) sensor_get_heart_rate, METH_NOARGS,
         "Return list of heart rate values"},
        {"get_light", (PyCFunction) sensor_get_light, METH_NOARGS,
         "Return list of light values"},
        {"get_linear_acceleration",
        (PyCFunction) sensor_get_linear_acceleration, METH_NOARGS,
         "Return list of linear acceleration values"},
        {"get_magnetic_field",
        (PyCFunction) sensor_get_magnetic_field, METH_NOARGS,
         "Return list of magnetic field values"},
        {"get_magnetic_field_uncalibrated",
        (PyCFunction) sensor_get_magnetic_field_uncalibrated, METH_NOARGS,
         "Return list of uncalibrated magnetic field values"},
        {"get_pressure", (PyCFunction) sensor_get_pressure, METH_NOARGS,
         "Return list of pressure values"},
        {"get_proximity", (PyCFunction) sensor_get_proximity, METH_NOARGS,
         "Return list of proximity values"},
        {"get_relative_humidity",
        (PyCFunction) sensor_get_relative_humidity, METH_NOARGS,
         "Return list of relative humidity values"},
        {"get_rotation_vector",
        (PyCFunction) sensor_get_rotation_vector, METH_NOARGS,
         "Return list of rotation vector values"},
        {"get_step_counter",
        (PyCFunction) sensor_get_step_counter, METH_NOARGS,
         "Return list of step counter values"},
        {NULL, NULL, 0, NULL} // This is the end-of-array marker
};


/*
 * Initializes Python module (sensor)
 *
 * Note:
 * If we wanted to build the module as .so or .dll we could
 * would have to change the signature to
 * PyMODINIT_FUNC initsensor(void)
 *
 */
void initsensor() {
    Py_InitModule("sensor", AndroidSensorMethods);
}
