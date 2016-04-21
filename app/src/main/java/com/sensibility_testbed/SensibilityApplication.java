package com.sensibility_testbed;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by lukas on 4/21/16.
 *
 * Application subclass is used to maintain global
 * application state.
 *
 * Can also be used to provide a static reference
 * to the application context. [1]
 * Although the preferred way to access context in static functions or
 * singletons is to pass the context to the function. [2]
 *
 * XXX:
 * Nevertheless we need the context in a sensor (pseudo) service class, called by C code via JNI
 * and I did not find any other way to access the context from there
 *
 *
 * [1] http://stackoverflow.com/questions/2002288/static-way-to-get-context-on-android
 * [2] http://developer.android.com/reference/android/app/Application.html
 *
 */
public class SensibilityApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        SensibilityApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SensibilityApplication.context;
    }
}
