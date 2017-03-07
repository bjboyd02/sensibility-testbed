package com.snakei;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by lukp on 9/15/16.
 *
 * PythonNodemanagerService should be reserved for the nodemanager "nmmain.py".
 *
 * It is not listed when PythonInterpreterService.startService searches for idle services, to use
 * for e.g. the seattleinstaller.py or repy.py
 *
 * Nevertheless it can be called with any other Python args TODO: Do we want to prevent this?
 *
 * If the service is already running it
 *
 */

public class PythonNodemanagerService extends PythonInterpreterService {

    public static void startService(String[] python_args, Context context) {

        Class thisClass = PythonNodemanagerService.class;

        Log.d(TAG, String.format("Starting interpreter service with args: %s", (Object[])python_args));
        Intent intent = new Intent(context, thisClass);

        if (isServiceRunning(context, thisClass)) {
            Log.d(TAG, "Tried re-starting existing Nodemanager Service Process.");
        } else {
            intent.putExtra("python_args", python_args);
            context.startService(intent);
        }
    }
}
