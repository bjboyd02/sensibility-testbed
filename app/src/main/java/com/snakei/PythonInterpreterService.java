package com.snakei;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Implement a sensor-enabled Python interpreter.
 */
public class PythonInterpreterService extends Service {
    private native void startNativePythonInterpreter(String env, String args);

    private class PythonInterpreterThread extends Thread {
        private String environment;
        private String commandLineArguments;

        private PythonInterpreterThread(String environment, String commandLineArguments) {
            this.environment = environment;
            this.commandLineArguments = commandLineArguments;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            System.loadLibrary("python2.7");
            System.loadLibrary("snakei");
            Log.i(this.getName(), "Before start native");
            startNativePythonInterpreter(this.environment, this.commandLineArguments);
            Log.i(this.getName(), "After start native");
        }
    };

    /**
     * For every call to start this service,
     * - extract command-line arguments and environment from `intent`, and
     * - create a new Python interpreter.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String env = intent.getStringExtra("com.snakei.PythonInterpreterService.environment");
        String args = intent.getStringExtra("com.snakei.PythonInterpreterService.commandLineArguments");
        PythonInterpreterThread pythonInterpreterThread = new PythonInterpreterThread(env, args);
        pythonInterpreterThread.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Don't provide means to bind this service
        return null;
    }

}
