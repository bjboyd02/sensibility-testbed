package com.snakei;
// TODO Should we make this a proper Library Module? https://developer.android.com/tools/projects/index.html#LibraryModules

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Output service for Snakei
 * XXX Why should this be a service?
 *      It is never started as a service in the app
 *      Python interpreter is a service started by the app
 *      which executes this via python->c->jni
 *
 * This class hosts methods that let a Sensibility experiment
 * output information to the device owner.
 */
//public class OutputService extends Service {
public class OutputService {
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // TODO I assume that my methods will be available for calling into directly
//        // TODO Therefor I don't set up anything
//        return START_STICKY;
//    }

    public static void logMessage(String message) {
        // This logs into the debug log...
        Log.i("######## Foo: ", message);
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        // Don't provide means to bind this service
//        return null;
//    }
}
