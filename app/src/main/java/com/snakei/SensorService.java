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
 */

public class SensorService implements SensorEventListener  {
    static final String TAG = "SensorService";

    private SensorManager sensor_manager;


    // We need all of those sensors to register listener on them
    // Most of the devices won't support most of the sensors
    // But hey, Android does it, so do we
    private Sensor accelerometer;
    private SensorEvent accelerometer_event;
//    private Sensor ambient_temperature;
//    private Sensor game_rotation_vector;
//    private Sensor geomagnetic_rotation_vector;
//    private Sensor gravity;
//    private Sensor gyroscope;
//    private Sensor gyroscope_uncalibrated;
//    private Sensor heart_rate;
//    private Sensor light;
//    private Sensor linear_acceleration;
//    private Sensor magnetic_field;
//    private Sensor magnetic_field_uncalibrated;
//    private Sensor pressure;
//    private Sensor proximity;
//    private Sensor relative_humidity;
//    private Sensor rotation_vector;
//    private Sensor significant_motion;
//    private Sensor step_counter;
//    private Sensor step_detector;

    // For each Sensor we write the values that we get in the
    // listener callback to the according value array.
    // Those arrays are eventually polled by C/Python
    // XXX: We maybe should also store timestamp and accuracy
    // Note:
    // Some sensors we can't use with our current polling strategy
    // because they don't fire events with interesting values
    // but only tell that something happened
    // TYPE_SIGNIFICANT_MOTION
    // TYPE_STEP_DETECTOR
//    private float[] accelerometer_values;
//    private float[] ambient_temperature_values;
//    private float[] game_rotation_vector_values;
//    private float[] geomagnetic_rotation_vector_values;
//    private float[] gravity_values;
//    private float[] gyroscope_values;
//    private float[] gyroscope_uncalibrated_values;
//    private float[] heart_rate_values;
//    private float[] light_values;
//    private float[] linear_acceleration_values;
//    private float[] magnetic_field_values;
//    private float[] magnetic_field_uncalibrated_values;
//    private float[] pressure_values;
//    private float[] proximity_values;
//    private float[] relative_humidity_values;
//    private float[] rotation_vector_values;
//    private float[] step_counter_values;

    /* See Initialization on Demand Holder pattern */
    private static class SensorServiceHolder {
        private static final SensorService instance = new SensorService();
    }

    /* Classic Singleton Instance Getter */
    public static SensorService getInstance(){
        return SensorServiceHolder.instance;
    }

    private SensorService() {
        // Fetch the context
        // XXX: This is a hack, consider changing it
        Context app_context = SensibilityApplication.getAppContext();
        sensor_manager = (SensorManager)app_context.getSystemService(app_context.SENSOR_SERVICE);

        // I guess we can get all the sensors right away
        accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

//        ambient_temperature = sensor_manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//        game_rotation_vector = sensor_manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
//        geomagnetic_rotation_vector = sensor_manager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
//        gravity = sensor_manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
//        gyroscope = sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        gyroscope_uncalibrated = sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
//        heart_rate = sensor_manager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
//        light = sensor_manager.getDefaultSensor(Sensor.TYPE_LIGHT);
//        linear_acceleration = sensor_manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        magnetic_field = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        magnetic_field_uncalibrated = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
//        pressure = sensor_manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
//        proximity = sensor_manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//        relative_humidity = sensor_manager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
//        rotation_vector = sensor_manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        step_counter = sensor_manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Initialize empty float arrays, because someone could them before
        // the listener was registered / has written values to them
        // Note: we always get float arrays but the length varies
        // from sensor to sensor
//        accelerometer_values = new float[3];
//        ambient_temperature_values = new float[1];
//        game_rotation_vector_values = new float[4];
//        geomagnetic_rotation_vector_values = new float[5]; //XXX: Verify length of array
//        gravity_values = new float[3];
//        gyroscope_values = new float[3];
//        gyroscope_uncalibrated_values = new float[6];
//        heart_rate_values = new float[1]; //XXX: Verify length of array
//        light_values = new float[1];
//        linear_acceleration_values = new float[3];
//        magnetic_field_values = new float[3];
//        magnetic_field_uncalibrated_values = new float[6];
//        pressure_values = new float[1];
//        proximity_values = new float[1];
//        relative_humidity_values = new float[1];
//        rotation_vector_values = new float[5];
//        step_counter_values = new float[1];
    }

    public Sensor[] get_sensor_list() {
        // Fetch a list of sensors
        List<Sensor> sensorList = sensor_manager.getSensorList(Sensor.TYPE_ALL);
//        Log.i(TAG, "Java says we have " + sensorList.size() +" sensors");
        Sensor[] sensorArray = new Sensor[sensorList.size()];
        sensorList.toArray(sensorArray);
        return sensorArray;
    }


    private double[] _getSensorResult(SensorEvent event) {
        if (event == null) {
            return null;
        }
        // Reading and writing event objects is done concurrently
        // XXX: I am pretty sure we will need a ReadWrite lock
        // We return sensor values and two timestamps
        double[] result = new double[event.values.length + 2];
        result[0] = (double) System.currentTimeMillis(); // XXX Losing precision
        result[1] = (double) event.timestamp / 1000000; // XXX Losing more precision
        for (int i = 0; i < event.values.length; i++){
            result[i+2] = (double) event.values[i];
        }
        Log.i(TAG, "oida");
        return result;
    }

    /*
     * Return last accelerometer values
     * as stored in event
     * The array is updated by the event listener's
     * onSensorChanged callback and can be empty
     * The listener gets registered by calling
     * start_sensing
     *
     */
    public double[] getAcceleration() {
        return _getSensorResult(accelerometer_event);
    }
    /*
     * Register Accelerometer EventListener
     *
     * Todo:
     *      Change to support other Sensors as well
     *      Only register if it hasn't been registered yet
     *
     *
     */
    public int start_sensing() {
//        Log.i(TAG, "Starting sensor");
        // The sensor should exist because it was created in the class constructor
        // If it doesn't that means it does not exist on the device or we don't have
        // the necessary permissions
        if (accelerometer == null) {
            Log.i(TAG, "sensor does not exist, or app has not the necessary permissions");
            // XXX What should we return if the sensor does not exist?
            return 0;
        }
        // Apparently android checks that listener are only registered once
        if (sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL))
            return 1;
        else
            return 0;
    }
    /*
     * Unregister Accelerometer EventListener
     *
     * Todo:
     *      Change to support other Sensors as well
     *      Only unregister if nobody else is using it
     */
    public int stop_sensing() {
//        Log.i(TAG, "Stopping sensor");
        sensor_manager.unregisterListener(this, accelerometer);
        // XXX: don't just return 1, check if really unregistered
        return 1;

    }

    /*
     * XXX: Check which Sensor this Event belongs to, it could be called be
     * multiple listeners
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
//        Log.i(TAG, "Sensor has changed");
          accelerometer_event = event;
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            accelerometer_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
//            ambient_temperature_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
//            game_rotation_vector_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
//            geomagnetic_rotation_vector_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
//            gravity_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
//            gyroscope_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED){
//            gyroscope_uncalibrated_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE){
//            heart_rate_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT){
//            light_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
//            linear_acceleration_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//            magnetic_field_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED){
//            magnetic_field_uncalibrated_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE){
//            pressure_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY){
//            proximity_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY){
//            relative_humidity_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
//            rotation_vector_values = event.values;
//        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
//            step_counter_values = event.values;
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
