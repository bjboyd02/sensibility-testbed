package com.snakei;
// TODO Should we make this a proper Library Module? https://developer.android.com/tools/projects/index.html#LibraryModules

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Output service for Snakei
 *
 * This class hosts methods that let a Sensibility experiment
 * output information to the device owner.
 */
public class OutputService extends Service {

    public void logMessage(String message) {
        // This logs into the debug log...
        Log.i("Foo!", message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Don't provide means to bind this service
        return null;
    }
}
