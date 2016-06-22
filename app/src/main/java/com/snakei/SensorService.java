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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lukas on 4/20/16.
 *
 * Pseudo Service class that facades Android Sensors and
 * provides methods for starting and stopping Sensors
 * polling their values and getting information about them.
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

    // XXX: These values are hardcoded in C!
    // If we change them here, we have to change them there as well
    // Look for functions that get passed integers and compare them
    // with below constants
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

    // For each Sensor we store the data (timestamp + values that we received upon
    // a previously registered ``onSensorChange''
    // New arrays overwrite older
    // Data is eventually returned to C/Python when called
    private JSONArray accelerometer_json;
    private JSONArray ambient_temperature_json;
    private JSONArray game_rotation_vector_json;
    private JSONArray geomagnetic_rotation_vector_json;
    private JSONArray gravity_json;
    private JSONArray gyroscope_json;
    private JSONArray gyroscope_uncalibrated_json;
    private JSONArray heart_rate_json;
    private JSONArray light_json;
    private JSONArray linear_acceleration_json;
    private JSONArray magnetic_field_json;
    private JSONArray magnetic_field_uncalibrated_json;
    private JSONArray pressure_json;
    private JSONArray proximity_json;
    private JSONArray relative_humidity_json;
    private JSONArray rotation_vector_json;
    private JSONArray step_counter_json;

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
    public String getSensorList() throws JSONException {
        List<Sensor> sensors = sensor_manager.getSensorList(Sensor.TYPE_ALL);
        if (sensors.size() > 0) {
            JSONArray sensors_json = new JSONArray();
            for (Sensor sensor : sensors) {
                JSONObject sensor_json = new JSONObject();
                sensor_json.put("fifo_max_event_count", sensor.getFifoMaxEventCount());
                sensor_json.put("fifo_reserved_event_count", sensor.getFifoReservedEventCount());
                sensor_json.put("max_delay", sensor.getMaxDelay());
                sensor_json.put("max_range", sensor.getMaximumRange());
                sensor_json.put("min_delay", sensor.getMinDelay());
                sensor_json.put("name", sensor.getName());
                sensor_json.put("power", sensor.getPower());
                sensor_json.put("reporting_mode", sensor.getReportingMode());
                sensor_json.put("resolution", sensor.getResolution());
                sensor_json.put("string_type", sensor.getStringType());
                sensor_json.put("type", sensor.getType());
                sensor_json.put("vendor", sensor.getVendor());
                sensor_json.put("version", sensor.getVersion());
                sensor_json.put("is_wakeup_sensor", sensor.isWakeUpSensor());

                sensors_json.put(sensor_json);
            }
            return sensors_json.toString();
        }
        return null;
    }

    /*
     * Subsequent calls require preceding call to ``start_sensing''
     */
    public String getAcceleration() {
        if (accelerometer_json != null) {
            return accelerometer_json.toString();
        }
        return null;
    }
    public String getAmbientTemperature() {
        if (ambient_temperature_json != null) {
            return ambient_temperature_json.toString();
        }
        return null;
    }
    public String getGameRotationVector() {
        if (game_rotation_vector_json != null) {
            return game_rotation_vector_json.toString();
        }
        return null;
    }
    public String getGeomagneticRotationVector() {
        if (geomagnetic_rotation_vector_json != null) {
            return geomagnetic_rotation_vector_json.toString();
        }
        return null;
    }
    public String getGravity() {
        if (gravity_json != null) {
            return gravity_json.toString();
        }
        return null;
    }
    public String getGyroscope() {
        if (gyroscope_json != null) {
            return gyroscope_json.toString();
        }
        return null;
    }
    public String getGyroscopeUncalibrated() {
        if (gyroscope_uncalibrated_json != null) {
            return gyroscope_uncalibrated_json.toString();
        }
        return null;
    }
    public String getHeartRate() {
        if (heart_rate_json != null) {
            return heart_rate_json.toString();
        }
        return null;
    }
    public String getLight() {
        if (light_json != null) {
            return light_json.toString();
        }
        return null;
    }
    public String getLinearAcceleration() {
        if (linear_acceleration_json != null) {
            return linear_acceleration_json.toString();
        }
        return null;
    }
    public String getMagneticField() {
        if (magnetic_field_json != null) {
            return magnetic_field_json.toString();
        }
        return null;
    }
    public String getMagneticFieldUncalibrated() {
        if (magnetic_field_uncalibrated_json != null) {
            return magnetic_field_uncalibrated_json.toString();
        }
        return null;
    }
    public String getPressure() {
        if (pressure_json != null) {
            return pressure_json.toString();
        }
        return null;
    }
    public String getProximity() {
        if (proximity_json != null) {
            return proximity_json.toString();
        }
        return null;
    }
    public String getRelativeHumidity() {
        if (relative_humidity_json != null) {
            return relative_humidity_json.toString();
        }
        return null;
    }
    public String getRotationVector() {
        if (rotation_vector_json != null) {
            return rotation_vector_json.toString();
        }
        return null;
    }
    public String getStepCounter() {
        if (step_counter_json != null) {
            return step_counter_json.toString();
        }
        return null;
    }

    /*
     * Register EventListener for a sensor of a specific type
     */
    public void start_sensing(int sensor_type) {
//        Log.i(TAG, "Starting sensor");

        Sensor tmp_sensor = null;

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
            Log.i(TAG, "Sensor does not exist or app has not the necessary permissions.");
        }

        // Existing sensor should available because they were created in this class' constructor
        // If a Sensor is not available this means it doesn't exist at all on the device or we
        // don't have the necessary permissions
        // Apparently android checks that listener are only registered once
        if (tmp_sensor != null) {
            sensor_manager.registerListener(this, tmp_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    /*
     * Unregister EventListener for a sensor of a specific type
     *
     * Todo:
     *      Only unregister if nobody else is using it
     */
    public void stop_sensing(int sensor_type) {
//        Log.i(TAG, "Unregistering sensor");

        Sensor tmp_sensor = null;
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
            // XXX: Maybe raise an exception here
            Log.i(TAG, "Can't unregister a Sensor that does not exist.");
        }
        if (tmp_sensor != null) {
            sensor_manager.unregisterListener(this, tmp_sensor);
        }
    }

    /*
     * Creates an array out of Sensor Event Data:
     * [<current time>, <time at event propagation>,
     * <sensor value[0]>, ..., <sensor value[n-1]>]
     *  assigns array to the Sensor's respective data attribute of this Service class
     *
     * Todo:
     * Find out if precision is an issue for long -> double cast
     *      System.currentTimeMillis (milliseconds)
     *      SensorEvent.timestamp (nanoseconds)
     * Find out if reading and writing to SensorEvents concurrently
     * requires us to use a Lock
     *
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Having one data type makes it easier to give it back to C
        JSONArray result = new JSONArray();

        result.put(System.currentTimeMillis());
        result.put(event.timestamp / 1000000);

        // Note: we always get float arrays but the length varies
        // from Sensor to Sensor
        for (int i = 0; i < event.values.length; i++){
            try {
                result.put(event.values[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometer_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            ambient_temperature_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
            game_rotation_vector_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
            geomagnetic_rotation_vector_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
            gravity_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyroscope_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED){
            gyroscope_uncalibrated_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE){
            heart_rate_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            light_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            linear_acceleration_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            magnetic_field_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED){
            magnetic_field_uncalibrated_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE){
            pressure_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            proximity_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){
            relative_humidity_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            rotation_vector_json = result;
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            step_counter_json = result;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
