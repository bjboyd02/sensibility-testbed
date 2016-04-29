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

    private SensorManager sensorManager;

    private float[] accelerometer_values;
    private Sensor accelerometer;

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
        sensorManager = (SensorManager)app_context.getSystemService(app_context.SENSOR_SERVICE);

        // I guess we can get all the sensors right away
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometer_values = new float[3];
    }

    public Sensor[] get_sensor_list() {
        // Fetch a list of sensors
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i(TAG, "Java says we have " + sensorList.size() +" sensors");
        Sensor[] sensorArray = new Sensor[sensorList.size()];
        sensorList.toArray(sensorArray);
        return sensorArray;
    }

    /*
     * Return accelerometer float array
     *
     * The array is updated by the event listener's
     * onSensorChanged callback and can be empty
     * The listener gets registered by calling
     * start_sensing
     *
     */
    public float[] getAcceleration() {
        return accelerometer_values;
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
        if (sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL))
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
        sensorManager.unregisterListener(this, accelerometer);
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometer_values = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
