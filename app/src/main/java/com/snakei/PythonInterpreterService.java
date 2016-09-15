package com.snakei;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static android.os.Process.myUid;

/**
 *Created by
 * albert.rafetseder@univie.ac.at
 * lukas.puehringer@nyu.edu
 * on 3/28/16.
 *
 * Implements an Android Service that runs in the background and starts
 * a Python interpreter in a separate thread
 *
 * Performs the following tasks:
 *  - Declares a native Java method that is implemented in C and called from
 *    the JVM using JNI
 *  - sets environment variables and passes them to native Python interpreter,
 *    e.g. the directory we want Python to write files to
 *  - Loads shared libraries python2.7 and snakei (sensor extensions)
 *  - Copies files from the assets folder to a place where Python can
 *    access them
 *
 * Note:
 * We have to decide where Python should be allowed to write files to
 * and also keep in mind that external storage can become unavailable
 * during runtime
 *
 *
 */
public class PythonInterpreterService extends Service {

    static String TAG = "Interpreter";

    // Todo: don't hardcode here
    private static String python_home =
            "/data/data/com.sensibility_testbed/files/python";
    private static String python_path =
            "/data/data/com.sensibility_testbed/files/seattle/seattle_repy";

    private static Class[] enabled_services = {
            PythonInterpreterService0.class,
            PythonInterpreterService1.class,
            PythonInterpreterService2.class,
            PythonInterpreterService3.class,
            PythonInterpreterService4.class,
            PythonInterpreterService5.class,
            PythonInterpreterService6.class,
            PythonInterpreterService7.class,
            PythonInterpreterService8.class,
            PythonInterpreterService9.class};


    static {
        System.loadLibrary("python2.7");
        System.loadLibrary("snakei");
    }

    // python_args = {"nmmain.py", "--foreground"};
    // python_args = {"repyV2/repy.py", "restrictions.default", "test.r2py"};
    public static void startService(String[] python_args, Context context) {
        Class idle_service_class = getIdleServiceClass(context);

        // XXX What should we do if there is no idle service around?
        // Try until we find one?
        // Throw an exception?
        // Return not 0 ?
        if (idle_service_class == null) {
            Log.i(TAG, "No idle Service");
            return;
        }

        // Fork new process !!!!!!!!
        Intent intent = new Intent(context, idle_service_class);
        intent.putExtra("python_args", python_args);
        context.startService(intent);
    }

    /* XXX Todo: this is a critical section, take care of it!! */
    public static Class getIdleServiceClass(Context context) {

        ActivityManager manager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);

        // Enabled but idle service
        Class idle_service = null;

        // Currently running service per ActivityManager
        List<ActivityManager.RunningServiceInfo> running_services =
                manager.getRunningServices(Integer.MAX_VALUE);

        find_idle_service_loop:
        for (Class enabled_service : enabled_services) {
            for (ActivityManager.RunningServiceInfo running_service : running_services) {

                if (enabled_service.getName().equals(running_service.service.getClassName())) {
                    Log.i(TAG, String.format("Service '%s' is not idle", enabled_service.getName()));

                    // If we find the enabled service in the list of running services
                    // we can continue checking the next enabled service (outer loop)
                    continue find_idle_service_loop;
                }
            }

            // The enabled service was not found in the list of running services
            // ergo it is idle and we can use it
            idle_service = enabled_service;
            break;
        }

        return idle_service;

    }

    public void runScript(String[] python_args) {
        runScript(python_args, python_home, python_path);
    }

    public native void runScript(String[] python_args,
                                 String python_home, String python_path);

    /**
     * For every call to start this service,
     * - extract the future  sys.path, PYTHONHOME, the script file name,
     *   and the command-line arguments  from `intent`, and
     * - create a new Python interpreter.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log.i(this.getPackageName(), "**** UID is " + Integer.toString(myUid()));

        String[] python_args = intent.getStringArrayExtra("python_args");
        runScript(python_args);

        /* Once the script has run the service should stop (go idle)
         * so that we can reuse it. */
        stopSelf();

        // TODO: For a plain Python interpreter that needs arguments,
        // starting STICKY makes no sense as Android's restart attempt
        // only sends a `null` Intent
        // For an encapsulated nodemanager/softwareupdater,
        // it would make sense OTOH

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Don't provide means to bind this service
        return null;
    }

}
