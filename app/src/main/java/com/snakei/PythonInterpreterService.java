package com.snakei;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static android.os.Process.myUid;

/**
 * Implement a sensor-enabled Python interpreter.
 */
public class PythonInterpreterService extends Service {
    private native void startNativePythonInterpreter(String path, String home, String script, String args);

    private class PythonInterpreterThread extends Thread {
        private String python_path;
        private String python_home;
        private String python_script;
        private String python_arguments;

        private PythonInterpreterThread(String python_path, String python_home,
                String python_script, String python_arguments) {
            this.python_path = python_path;
            this.python_home = python_home;
            this.python_script = python_script;
            this.python_arguments = python_arguments;
        }

        @Override
        public void run() {
            System.loadLibrary("python2.7");
            System.loadLibrary("snakei");
            Log.i(this.getName(), "Before start native");
            startNativePythonInterpreter(this.python_path, this.python_home,
                    this.python_script, this.python_arguments);
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

        Log.i(this.getPackageName(), "Getting intent and stuff");
        String path = intent.getStringExtra("com.snakei.PythonInterpreterService.python_path");
        String home = intent.getStringExtra("com.snakei.PythonInterpreterService.python_home");
        String script = intent.getStringExtra("com.snakei.PythonInterpreterService.python_script");
        String args = intent.getStringExtra("com.snakei.PythonInterpreterService.python_arguments");
        Log.i(this.getPackageName(), "`new` thread");
        PythonInterpreterThread pythonInterpreterThread = new PythonInterpreterThread(path,
                home, script, args);
        Log.i(this.getPackageName(), "Starting thread");
        pythonInterpreterThread.start();
        Log.i(this.getPackageName(), "Started");
        /* TODO: For a plain Python interpreter that needs arguments,
         * starting STICKY makes no sense as Android's restart attempt
         * only sends a `null` Intent.
         * (For an encapsulated nodemanager/softwareupdater, it would make sense OTOH).
         */
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Don't provide means to bind this service
        return null;
    }

}
