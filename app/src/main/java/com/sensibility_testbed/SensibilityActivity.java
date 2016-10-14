package com.sensibility_testbed;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.android_scripting.FileUtils;
import com.snakei.PythonInterpreterService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import static android.os.Process.myUid;

/**
 * Created by lukas.puehringer@nyu.edu on 7/15/16
 *
 * Main activity of Sensibility app, provides
 *
 *
 * - Installation path constants
 *
 * - Installation User Interface to trigger
 *   - Python libs installation
 *     Maybe this can be moved to the actual app installation step, because
 *     we just need to copy over static files from res/raw to internal or external
 *     storage. If not, this should happen in a background Service to not
 *     block the UI
 *
 *   - Seattle download & installation
 *     Seattle needs to be downloaded and installed during app runtime,
 *     because we receive a referrer intent from the app store that
 *     points at the appropriate seattle custom installer
 *     (containing the appropriate keys), in case the user downloaded the app
 *     using the link provided by Sensibility Testbed Custom Installer Builder.
 *     Should also happen in a background service
 *
 * - De-installation interface
 *
 * - Start/Stop Seattle interface
 *
 * - Privacy settings
 *
 *
 * Note:
 * Functionality will be successively be copied over from "ScriptActivity" to
 * finally replace it
 *
 * I think we should Fragments for each of above
 *
 * Permissions:
 * In API > 23 we have to ask for permissions at runtime and permissions can also be revoked
 * But we cannot (verify this) request permissions from a background service process, therefor
 * I suggest we request permissions when the activity starts and just pass on the exceptions
 * in the services (if a needed permission was not granted or revoked)
 */
public class SensibilityActivity extends Activity {

    public final String TAG = "SensibilityActivity";

    public final String FILES_ROOT = SensibilityApplication.getAppContext().getFilesDir().getPath() + "/";
    public final String SEATTLE_ZIP = FILES_ROOT + "seattle_android.zip";
    //Todo: this is definitely not going to be defined here
    String DOWNLOAD_URL =
//            "https://sensibilityclearinghouse.poly.edu/geni/download/altruistic/seattle_android.zip";
            "https://sensibilityclearinghouse.poly.edu/custominstallerbuilder/e3978fcf1a421d92d4d8b34313c829c52ed81da4/installers/android/";
    public final String PYTHON = FILES_ROOT + "python/";
    public final String PYTHON_LIB = FILES_ROOT + "python/lib/python2.7/";
    public final String PYTHON_SCRIPTS = FILES_ROOT + "scripts/";


    // See document docstring - Permissions
    public final int SENSIBILITY_RUNTIME_PERMISSIONS = 1;

    /*
     * Downloads zipped Seattle custom installer package from Clearinghouse
     * from passed url to passed destination.
     * Unzips archive to same destination and removes archive.
     *
     * Todo:
     *   Move this out of Activity to InstallerService
     *   Add download function to Utils
     *   Add progress bar and user feedback
     *   Add logging
     *   Error handling
     */
    private void installSeattle() {


        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, String.format("Downloading seattle from %s to %s", DOWNLOAD_URL, SEATTLE_ZIP));
                    // Download seattle installer package and unpack to internal storage
                    InputStream input = null;
                    OutputStream output = null;

                    URL url = new URL(DOWNLOAD_URL);

                    HttpsURLConnection connection;
                    connection = (HttpsURLConnection) url.openConnection();

                    connection.connect();

                    if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                        Log.i(TAG, connection.getResponseMessage());
                        return;
                    }

                    input = connection.getInputStream();
                    // Todo: don't hardcode file name
                    output = new FileOutputStream(SEATTLE_ZIP);

                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }


                    if (input != null) {
                        input.close();
                    }

                    if (output != null) {
                        output.close();
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }


                    Log.i(TAG, String.format("Unpacking seattle zip to %s", FILES_ROOT));
                    Utils.unzip(new FileInputStream(SEATTLE_ZIP), FILES_ROOT, true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    // Show install layout
    private void showBasicInstallLayout() {
        setContentView(R.layout.basic_install);
//
        final Button installButton = (Button) findViewById(R.id.basicinstallbutton);
        final Button startButton = (Button) findViewById(R.id.showadvancedoptionsbutton);

        // XXX Todo: Disable if already installed
        installButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Install python
//                Log.i(TAG, String.format("Unpacking python archive to %s", FILES_ROOT));
//                try {
//                    Utils.unzip(getResources().openRawResource(R.raw.python_lib), FILES_ROOT, true);
//                } catch (IOException e) {
//                    Log.i(TAG, "Couldn't unpack python archive");
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                // Download Seattle
//                // XXX for now we just copy over a zip with the WIP seattle files
                Log.i(TAG, String.format("Unpacking python archive to %s", FILES_ROOT));
                try {
                    Utils.unzip(getResources().openRawResource(R.raw.seattle_android), FILES_ROOT, true);
                } catch (IOException e) {
                    Log.i(TAG, "Couldn't unpack python archive");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Install seattle
                String[] python_args = {"seattleinstaller.py", "--percent", "50", "--disable-startup-script", "True"};
                PythonInterpreterService.startService(python_args, getBaseContext());
            }
        });

        // XXX Todo: Disable if already running
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start a new python interpreter service process that runs nmmain.py
                // (foreground skips daemonizing)
                String[] python_args = {"nmmain.py", "--foreground"};
                PythonInterpreterService.startService(python_args, getBaseContext());
            }
        });
    }

    // See document docstring - Permissions
    protected void checkRequestPermission(String perm) {
        String[] perms = new String[] {perm};
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, perms, SENSIBILITY_RUNTIME_PERMISSIONS);
        }
    }

    /*
     * Shows the User Interface
     * Called by system,
     *   - onCreate()
     *   - onRestart()
     */
    @Override
    protected void onStart() {
        //Check all permissions
        checkRequestPermission(Manifest.permission.INTERNET);
        checkRequestPermission(Manifest.permission.ACCESS_WIFI_STATE);
        checkRequestPermission(Manifest.permission.CHANGE_WIFI_STATE);
        checkRequestPermission(Manifest.permission.BLUETOOTH);
        checkRequestPermission(Manifest.permission.BLUETOOTH_ADMIN);
        checkRequestPermission(Manifest.permission.READ_PHONE_STATE);
        checkRequestPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED);
        checkRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        checkRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkRequestPermission(Manifest.permission.ACCESS_NETWORK_STATE);
        checkRequestPermission(Manifest.permission.BODY_SENSORS);
        checkRequestPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        checkRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        checkRequestPermission(Manifest.permission.RECORD_AUDIO);
        super.onStart();
        showBasicInstallLayout();
    }

    /*
     * Sets the user interface
     *
     * Called by system after:
     *  - Activity launched
     *  - App process was killed and user navigates back to the activity
     *
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Common.LOG_TAG, "Into onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    // See document docstring - Permissions
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case SENSIBILITY_RUNTIME_PERMISSIONS: {
//                if (grantResults.length <= 0
//                        || grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                    //Disable the functionality that depends on this permission?
//                }
//            }
//        }
//        return;
//
//    }



}
