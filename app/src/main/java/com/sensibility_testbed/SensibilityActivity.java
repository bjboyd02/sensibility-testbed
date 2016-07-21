package com.sensibility_testbed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.android_scripting.FileUtils;

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
 * I think we should Fragments for each of above items
 *
 */
public class SensibilityActivity extends Activity {

    public final String TAG = "SensibilityActivity";

    public final String FILES_ROOT = SensibilityApplication.getAppContext().getFilesDir().getPath() + "/";
    public final String SEATTLE_ZIP = FILES_ROOT + "seattle_android.zip";
    //Todo: this is definitely not going to be defined here
    String DOWNLOAD_URL =
            "https://sensibilityclearinghouse.poly.edu/geni/download/altruistic/seattle_android.zip";
    public final String PYTHON = FILES_ROOT + "python/";
    public final String PYTHON_LIB = FILES_ROOT + "python/lib/python2.7/";
    public final String PYTHON_SCRIPTS = FILES_ROOT + "scripts/";


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

        final Button buttonInstall = (Button) findViewById(
                R.id.basicinstallbutton);

        buttonInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Todo: Move this out of Activity to InstallerService

                // Copy Python modules from res/raw to internal storage
                // Todo: Check and copy only if files are not yet
                Log.i(TAG, String.format("Unpacking python archive to %s", FILES_ROOT));
                try {
                    Utils.unzip(getResources().openRawResource(R.raw.raw), FILES_ROOT, true);
                } catch (IOException e) {
                    Log.i(TAG, "Couldn't unpack python archive");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //installSeattle();
            }
        });


        // Ha I say I am an advanced button, but I am a start process button
        final Button buttonAdvanced = (Button) findViewById(R.id.showadvancedoptionsbutton);
        buttonAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), com.snakei.PythonInterpreterService.class);
                intent.putExtra("com.snakei.PythonInterpreterService.python_scripts", PYTHON_SCRIPTS);
                startService(intent);
            }
        });
    }


    /*
     * Shows the User Interface
     * Called by system,
     *   - onCreate()
     *   - onRestart()
     */
    @Override
    protected void onStart() {
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
}
