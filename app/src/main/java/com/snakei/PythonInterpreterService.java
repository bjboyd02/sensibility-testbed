package com.snakei;

import android.app.Service;
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
    private native void startNativePythonInterpreter(String python_scripts);

    private class PythonInterpreterThread extends Thread {
        private String python_scripts;


        public PythonInterpreterThread(String python_scripts) {
            this.python_scripts = python_scripts;
        }

        @Override
        public void run() {

            System.loadLibrary("python2.7");
            System.loadLibrary("snakei");
            Log.i(this.getName(), "Before start native");
            startNativePythonInterpreter(this.python_scripts);
            Log.i(this.getName(), "After start native");
        }
    };

    /**
     * For every call to start this service,
     * - extract the future  sys.path, PYTHONHOME, the script file name,
     *   and the command-line arguments  from `intent`, and
     * - create a new Python interpreter.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(this.getPackageName(), "**** UID is " + Integer.toString(myUid()));

//        Log.i(this.getPackageName(), "Getting intent and stuff");
//        String path = intent.getStringExtra(
//                "com.snakei.PythonInterpreterService.python_path");
//        String home = intent.getStringExtra(
//                "com.snakei.PythonInterpreterService.python_home");
        String python_scripts = intent.getStringExtra(
                "com.snakei.PythonInterpreterService.python_scripts");
//        String args = intent.getStringExtra(
//                "com.snakei.PythonInterpreterService.python_arguments");
        Log.i(this.getPackageName(), "`new` thread");
        PythonInterpreterThread pythonInterpreterThread =
                new PythonInterpreterThread(python_scripts);
        Log.i(this.getPackageName(), "Starting thread");
        pythonInterpreterThread.start();
        Log.i(this.getPackageName(), "Started");

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
