package com.snakei;

import com.sensibility_testbed.SensibilityApplication;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
 *   - Find a way to return complex data to calling c code via JNI
 *   - Consider Refactor Name, this is not an Android service
 *   - Find out if we need to worry about memory leaks
 */

public class SensorService implements SensorEventListener  {
    static final String TAG = "SensorService";

    private float[] accelerometer_data = new float[3];
    private SensorManager sensorManager;
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
     * Return static accelerometer float array
     *
     * The array is updated by the event listener's
     * onSensorChanged callback and can be empty
     * The listener gets registered by calling
     * start_sensing
     *
     */
    public float[] getAccelerometerData() {
        return accelerometer_data;
    }

    /*
     * Register Accelerometer EventListener
     *
     * Todo:
     *      Change to support other Sensors as well
     */
    public void start_sensing() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    /*
     * Unregister Accelerometer EventListener
     *
     * Todo:
     *      Change to support other Sensors as well
     */
    public void stop_sensing() {
        sensorManager.unregisterListener(this, accelerometer);
    }

    /*
     * XXX: Check which Sensor this Event belongs to, it could be called be
     * multiple listeners
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
