package com.snakei;

import com.sensibility_testbed.SensibilityApplication;
import android.content.Context;
import android.hardware.Sensor;
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
 * Is called via JNI C code
 * Todo:
 *   - Find a way to return complex data to calling c code via JNI
 *   - Refactor Name, this is not an Android service
 *   - Find out if we need to worry about memory leaks
 */

public class SensorService {
    static final String TAG = "SensorService";

    public Sensor[] get_sensor_list() {
        Log.i(TAG, "Wham, bam, thank you sensor man-ager!");
        // Fetch the context
        // XXX: Do we have to worry for memory leaks?
        Context app_context = SensibilityApplication.getAppContext();

        // Create a SensorManager and let it fetch a list of sensors
        SensorManager sensorManager =(SensorManager)SensibilityApplication.getAppContext().getSystemService(app_context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i(TAG, "Java says we have " + sensorList.size() +" sensors");
        Sensor[] sensorArray = new Sensor[sensorList.size()];
        sensorList.toArray(sensorArray);
        return sensorArray;

        // Figure out how to return complex data types. Ideas:
        // - maybe there is a way to exchange complex datatypes in JNI
        // - convert to JSON and serialze to string
        // - use jni glue like SWIG
        // - find a workaround to only pass simple data types (naaaaah!!)
        //return null;
    }

}
