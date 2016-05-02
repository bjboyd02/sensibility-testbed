package com.snakei;

import com.sensibility_testbed.SensibilityApplication;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.IBinder;
import android.util.Log;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lukas on 4/20/16.
 *
 *
 * Pseudo Service class that fetches a list of all available Sensors and
 * transforms into Sensor objects into a list of dictionaries of sensor info
 *
 * This class is a Singleton using the thread safe
 * Java Initialization on Demand Holder pattern
 * A Singleton is chosen to provide a single point of access
 * to all Sensors for all concurrent threads.
 * This has the advantage that we don't have to pass through references to
 * repy.
 *
 * Start sensing method
 *      This will register a SensorEventListener
 *      Only the listener callback writes values to instance attributes
 *      XXX: Should only register if it is not already registered
 *
 * Stop sensing method
 *      This will unregister the SensorEventListener
 *      XXX: Unless other threads still need the listener
 *      XXX: Maybe this accounting can be done in the sandbox hypervisor
 *
 * Get sensor values
 *     Reads from instance attributes
 *
 * Is called via JNI C code
 * Todo:
 *   - Consider Refactor Name, this is not an Android service
 *   - Find out if we need to worry about memory leaks
 *   - Make sure sensors are wake-up sensors
 *   - Consider better readability vs. redundancy
 */

public class SensorService implements SensorEventListener  {
    static final String TAG = "SensorService";

    static final int CUSTOM_TYPE_ACCELEROMETER = 1;
    static final int CUSTOM_TYPE_AMBIENT_TEMPERATURE = 2;
    static final int CUSTOM_TYPE_GAME_ROTATION_VECTOR = 3;
    static final int CUSTOM_TYPE_GEOMAGNETIC_ROTATION_VECTOR = 4;
    static final int CUSTOM_TYPE_GRAVITY = 5;
    static final int CUSTOM_TYPE_GYROSCOPE = 6;
    static final int CUSTOM_TYPE_GYROSCOPE_UNCALIBRATED = 7;
    static final int CUSTOM_TYPE_HEART_RATE = 8;
    static final int CUSTOM_TYPE_LIGHT = 9;
    static final int CUSTOM_TYPE_LINEAR_ACCELERATION = 10;
    static final int CUSTOM_TYPE_MAGNETIC_FIELD = 11;
    static final int CUSTOM_TYPE_MAGNETIC_FIELD_UNCALIBRATED = 12;
    static final int CUSTOM_TYPE_PRESSURE = 13;
    static final int CUSTOM_TYPE_PROXIMITY = 14;
    static final int CUSTOM_TYPE_RELATIVE_HUMIDITY = 15;
    static final int CUSTOM_TYPE_ROTATION_VECTOR = 16;
    static final int CUSTOM_TYPE_STEP_COUNTER = 17;

    private SensorManager sensor_manager;

    // We need all of those sensors to register listener on them
    // Most of the devices won't support most of the sensors
    // But hey, if Android does, so do we
    // Note:
    // Using sensors that only fire an event when something interesting happened
    // are omitted: TYPE_SIGNIFICANT_MOTION, TYPE_STEP_DETECTOR
    private Sensor accelerometer;
    private Sensor ambient_temperature;
    private Sensor game_rotation_vector;
    private Sensor geomagnetic_rotation_vector;
    private Sensor gravity;
    private Sensor gyroscope;
    private Sensor gyroscope_uncalibrated;
    private Sensor heart_rate;
    private Sensor light;
    private Sensor linear_acceleration;
    private Sensor magnetic_field;
    private Sensor magnetic_field_uncalibrated;
    private Sensor pressure;
    private Sensor proximity;
    private Sensor relative_humidity;
    private Sensor rotation_vector;
    private Sensor step_counter;

    // For each Sensor we store the Event that we received upon
    // a previously registered ``onSensorChange''
    // New Events overwrite older Events
    // Events are eventually transformed to double arrays of timestamps
    // and sensor values and returned to C/Python when called
    private SensorEvent accelerometer_event;
    private SensorEvent ambient_temperature_event;
    private SensorEvent game_rotation_vector_event;
    private SensorEvent geomagnetic_rotation_vector_event;
    private SensorEvent gravity_event;
    private SensorEvent gyroscope_event;
    private SensorEvent gyroscope_uncalibrated_event;
    private SensorEvent heart_rate_event;
    private SensorEvent light_event;
    private SensorEvent linear_acceleration_event;
    private SensorEvent magnetic_field_event;
    private SensorEvent magnetic_field_uncalibrated_event;
    private SensorEvent pressure_event;
    private SensorEvent proximity_event;
    private SensorEvent relative_humidity_event;
    private SensorEvent rotation_vector_event;
    private SensorEvent step_counter_event;

    /* See Initialization on Demand Holder pattern */
    private static class SensorServiceHolder {
        private static final SensorService instance = new SensorService();
    }

    /* Classic Singleton Instance Getter */
    public static SensorService getInstance(){
        return SensorServiceHolder.instance;
    }

    /*
     * Singleton Constructor
     * Fetches context from static application function
     * Get's default sensors
     */
    private SensorService() {
        // Fetch the context
        // XXX: This is a hack, consider changing it
        Context app_context = SensibilityApplication.getAppContext();
        sensor_manager = (SensorManager)app_context.getSystemService(app_context.SENSOR_SERVICE);

        // I guess we can get all the sensors right away
        accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ambient_temperature = sensor_manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        game_rotation_vector = sensor_manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        geomagnetic_rotation_vector = sensor_manager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gravity = sensor_manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gyroscope = sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroscope_uncalibrated = sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        heart_rate = sensor_manager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        light = sensor_manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        linear_acceleration = sensor_manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetic_field = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magnetic_field_uncalibrated = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        pressure = sensor_manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        proximity = sensor_manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        relative_humidity = sensor_manager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        rotation_vector = sensor_manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        step_counter = sensor_manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    /*
     * Fetches the list of Sensors supported on the device
     */
    public Sensor[] get_sensor_list() {
        List<Sensor> sensorList = sensor_manager.getSensorList(Sensor.TYPE_ALL);
        //Transform list to built-in array, because C knows Java arrays
        Sensor[] sensorArray = new Sensor[sensorList.size()];
        sensorList.toArray(sensorArray);
        return sensorArray;
    }

    /*
     * Generic function that creates an array out of
     * Sensor Event Data:
     * [<current time>, <time at event propagation>,
     * <sensor value[0]>, ..., <sensor value[n-1]>]
     *
     * Todo:
     * Find out if precision is an issue for long -> double cast
     *      System.currentTimeMillis (milliseconds)
     *      SensorEvent.timestamp (nanoseconds)
     * Find out if reading and writing to SensorEvents concurrently
     * requires us to use a Lock
     */

    private double[] _getSensorTimeAndValues(SensorEvent event) {
        // Return null if function is called before a SensorEvent
        // was came in
        if (event == null) {
            return null;
        }
        // Having one data type makes it easier to give it back to C
        // Double should be okay for time and sensor values
        double[] result = new double[event.values.length + 2];
        // Losing precision here
        result[0] = (double) System.currentTimeMillis();
        // Losing even more precision here
        result[1] = (double) event.timestamp / 1000000;

        // Note: we always get float arrays but the length varies
        // from Sensor to Sensor
        for (int i = 0; i < event.values.length; i++){
            result[i+2] = (double) event.values[i];
        }
        return result;
    }

    /*
     * Subsequent calls require preceding call to ``start_sensing''
     */
    public double[] getAcceleration() {
        return _getSensorTimeAndValues(accelerometer_event);
    }
    public double[] getAmbientTemperature() {
        return _getSensorTimeAndValues(ambient_temperature_event);
    }
    public double[] getGameRotationVector() {
        return _getSensorTimeAndValues(game_rotation_vector_event);
    }
    public double[] getGeomagneticRotationVector() {
        return _getSensorTimeAndValues(geomagnetic_rotation_vector_event);
    }
    public double[] getGravity() {
        return _getSensorTimeAndValues(gravity_event);
    }
    public double[] getGyroscope() {
        return _getSensorTimeAndValues(gyroscope_event);
    }
    public double[] getGyroscopeUncalibrated() {
        return _getSensorTimeAndValues(gyroscope_uncalibrated_event);
    }
    public double[] getHeartRate() {
        return _getSensorTimeAndValues(heart_rate_event);
    }
    public double[] getLight() {
        return _getSensorTimeAndValues(light_event);
    }
    public double[] getLinearAcceleration() {
        return _getSensorTimeAndValues(linear_acceleration_event);
    }
    public double[] getMagneticField() {
        return _getSensorTimeAndValues(magnetic_field_event);
    }
    public double[] getMagneticFieldUncalibrated() {
        return _getSensorTimeAndValues(magnetic_field_uncalibrated_event);
    }
    public double[] getPressure() {
        return _getSensorTimeAndValues(pressure_event);
    }
    public double[] getProximity() {
        return _getSensorTimeAndValues(proximity_event);
    }
    public double[] getRelativeHumidity() {
        return _getSensorTimeAndValues(relative_humidity_event);
    }
    public double[] getRotationVector() {
        return _getSensorTimeAndValues(rotation_vector_event);
    }
    public double[] getStepCounter() {
        return _getSensorTimeAndValues(step_counter_event);
    }

    /*
     * Register EventListener for a sensor of a specific type
     */
    public int start_sensing(int sensor_type) {
//        Log.i(TAG, "Starting sensor");

        Sensor tmp_sensor;
        if (sensor_type == CUSTOM_TYPE_ACCELEROMETER) {
            tmp_sensor = accelerometer;
        } else if (sensor_type == CUSTOM_TYPE_AMBIENT_TEMPERATURE) {
            tmp_sensor = ambient_temperature;
        } else if (sensor_type == CUSTOM_TYPE_GAME_ROTATION_VECTOR) {
            tmp_sensor = game_rotation_vector;
        } else if (sensor_type == CUSTOM_TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            tmp_sensor = geomagnetic_rotation_vector;
        } else if (sensor_type == CUSTOM_TYPE_GRAVITY) {
            tmp_sensor = gravity;
        } else if (sensor_type == CUSTOM_TYPE_GYROSCOPE) {
            tmp_sensor = gyroscope;
        } else if (sensor_type == CUSTOM_TYPE_GYROSCOPE_UNCALIBRATED) {
            tmp_sensor = gyroscope_uncalibrated;
        } else if (sensor_type == CUSTOM_TYPE_HEART_RATE) {
            tmp_sensor = heart_rate;
        } else if (sensor_type == CUSTOM_TYPE_LIGHT) {
            tmp_sensor = light;
        } else if (sensor_type == CUSTOM_TYPE_LINEAR_ACCELERATION) {
            tmp_sensor = linear_acceleration;
        } else if (sensor_type == CUSTOM_TYPE_MAGNETIC_FIELD) {
            tmp_sensor = magnetic_field;
        } else if (sensor_type == CUSTOM_TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
            tmp_sensor = magnetic_field_uncalibrated;
        } else if (sensor_type == CUSTOM_TYPE_PRESSURE) {
            tmp_sensor = pressure;
        } else if (sensor_type == CUSTOM_TYPE_PROXIMITY) {
            tmp_sensor = proximity;
        } else if (sensor_type == CUSTOM_TYPE_RELATIVE_HUMIDITY) {
            tmp_sensor = relative_humidity;
        } else if (sensor_type == CUSTOM_TYPE_ROTATION_VECTOR) {
            tmp_sensor = rotation_vector;
        } else if (sensor_type == CUSTOM_TYPE_STEP_COUNTER) {
            tmp_sensor = step_counter;
        } else {
            tmp_sensor = null;
        }

        // Existing sensor should available because they were created in this class' constructor
        // If a Sensor is not available this means it doesn't exist at all on the device or we
        // don't have the necessary permissions
        if (tmp_sensor == null) {
            Log.i(TAG, "Sensor does not exist or app has not the necessary permissions.");
            // XXX What should we return if the sensor does not exist?
            return 0;
        }
        // Apparently android checks that listener are only registered once
        if (sensor_manager.registerListener(this, tmp_sensor, SensorManager.SENSOR_DELAY_NORMAL))
            return 1;
        else
            return 0;
    }
    /*
     * Unregister EventListener for a sensor of a specific type
     *
     * Todo:
     *      Only unregister if nobody else is using it
     */
    public int stop_sensing(int sensor_type) {
//        Log.i(TAG, "Unregistering sensor");

        Sensor tmp_sensor;
        if (sensor_type == CUSTOM_TYPE_ACCELEROMETER) {
            tmp_sensor = accelerometer;
        } else if (sensor_type == CUSTOM_TYPE_AMBIENT_TEMPERATURE) {
            tmp_sensor = ambient_temperature;
        } else if (sensor_type == CUSTOM_TYPE_GAME_ROTATION_VECTOR) {
            tmp_sensor = game_rotation_vector;
        } else if (sensor_type == CUSTOM_TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            tmp_sensor = geomagnetic_rotation_vector;
        } else if (sensor_type == CUSTOM_TYPE_GRAVITY) {
            tmp_sensor = gravity;
        } else if (sensor_type == CUSTOM_TYPE_GYROSCOPE) {
            tmp_sensor = gyroscope;
        } else if (sensor_type == CUSTOM_TYPE_GYROSCOPE_UNCALIBRATED) {
            tmp_sensor = gyroscope_uncalibrated;
        } else if (sensor_type == CUSTOM_TYPE_HEART_RATE) {
            tmp_sensor = heart_rate;
        } else if (sensor_type == CUSTOM_TYPE_LIGHT) {
            tmp_sensor = light;
        } else if (sensor_type == CUSTOM_TYPE_LINEAR_ACCELERATION) {
            tmp_sensor = linear_acceleration;
        } else if (sensor_type == CUSTOM_TYPE_MAGNETIC_FIELD) {
            tmp_sensor = magnetic_field;
        } else if (sensor_type == CUSTOM_TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
            tmp_sensor = magnetic_field_uncalibrated;
        } else if (sensor_type == CUSTOM_TYPE_PRESSURE) {
            tmp_sensor = pressure;
        } else if (sensor_type == CUSTOM_TYPE_PROXIMITY) {
            tmp_sensor = proximity;
        } else if (sensor_type == CUSTOM_TYPE_RELATIVE_HUMIDITY) {
            tmp_sensor = relative_humidity;
        } else if (sensor_type == CUSTOM_TYPE_ROTATION_VECTOR) {
            tmp_sensor = rotation_vector;
        } else if (sensor_type == CUSTOM_TYPE_STEP_COUNTER) {
            tmp_sensor = step_counter;
        } else {
            tmp_sensor = null;
        }
        if (tmp_sensor == null) {
            Log.i(TAG, "Can't unregister a Sensor that does not exist.");
            // XXX: What should we return if the sensor does not exist?
            return 0;
        }
        sensor_manager.unregisterListener(this, tmp_sensor);
        // XXX: don't just return 1, check if really unregistered
        return 1;

    }

    /*
     *  Assigns most recent SensorEvent for every Sensor
     *  the Sensor's respective event attribute of this Service class
     *
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometer_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            ambient_temperature_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
            game_rotation_vector_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
            geomagnetic_rotation_vector_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
            gravity_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyroscope_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED){
            gyroscope_uncalibrated_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE){
            heart_rate_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            light_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            linear_acceleration_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            magnetic_field_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED){
            magnetic_field_uncalibrated_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE){
            pressure_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            proximity_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){
            relative_humidity_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            rotation_vector_event = event;
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            step_counter_event = event;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
