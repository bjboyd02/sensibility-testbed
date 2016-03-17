/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.sensibility_testbed;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.InetSocketAddress;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

/*
import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.ForegroundService;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
*/
import com.sensibility_testbed.process.SeattleScriptProcess;

/**
 *
 * SeattleOnAndroid Nodemanager and Softwareupdater Service
 *
 * Sets up environment variables, launches the softwareupdater
 * and nodemanager,
 *
 * Loosely based on the Service found in the ScriptForAndroidTemplate package in
 * SL4A
 *
 * modified to allow embedded python interpreter and scripts in the APK
 *
 * based off Anthony Prieur & Daniel Oppenheim work
 * https://code.google.com/p/android-python27/
 *
 */
public class ScriptService {

    // private String AbsolutePath =
    // "/mnt/sdcard/Android/data/com.sensibility_testbed/files";

    // booleans used in shutting down the service
    private boolean killMe, isRestarting;

    // updater and nodemanager processes
    //private SeattleScriptProcess updaterProcess;
    //private SeattleScriptProcess seattlemainProcess;

    // workaround to make sure the service does not get restarted
    // when the system kills this service
    //public static boolean serviceInitiatedByUser = false;



    // on destroy
    public void onDestroy() {
        Log.i(Common.LOG_TAG, Common.LOG_INFO_MONITOR_SERVICE_SHUTDOWN);
    }

    public ScriptService() {
    }

    // on creation
    // checks, whether the start was initiated by the user or someone else
    public void onCreate() {
    }

    // Starts the updater process
    private void startUpdater() {

    }

    // Starts the nodemanager process
    private void startSeattleMain() {
    }

  private void killProcesses() {
    // Set kill flag, stop processes
    Log.i(Common.LOG_TAG, Common.LOG_INFO_KILLING_SCRIPTS);
  }

  // executed after each startService() call
  public void onStart(Intent intent, final int startId) {
    Log.i(Common.LOG_TAG, Common.LOG_INFO_MONITOR_SERVICE_STARTED);
    Bundle b = intent.getExtras();
    if (b != null
        && b.getBoolean("KILL_SERVICE")
        || !Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED)) {
      // Media not mounted correctly, or service is set to be killed
      killProcesses();
      //stopSelf();
      return;
    }
    //super.onStart(intent, startId);
    //instance = this;

    // Init flags
    killMe = false;
    isRestarting = false;

    // Start Seattle
    //startSeattleMain();
    //startUpdater();

  }

  // Create notification icon
  protected Notification createNotification() {
    //Notification notification = new Notification(R.drawable.ic_launcher,
    //    this.getString(R.string.loading), System.currentTimeMillis());

    // set OnClick intent
    //Intent intent = new Intent(this, ScriptActivity.class);
    //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

    //notification.setLatestEventInfo(this, this.getString(R.string.app_name),
    //    this.getString(R.string.loading), contentIntent);
    //notification.flags = Notification.FLAG_AUTO_CANCEL;

    return new Notification();
  }
}
