package com.sensibility_testbed;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.graphics.Color;


import com.snakei.PythonInterpreterService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
 */
public class SensibilityActivity extends FragmentActivity {

    public static final String TAG = "SensibilityActivity";

    // TODO This should point to an "altruistic" installer, not @lukpueh's!
     private String DEFAULT_DOWNLOAD_URL =
            "https://alpha-ch.poly.edu/cib/7e861c52f72c5e16e49b93cf60a84b4273a859d3/installers/android/";

    // RELATIVE PATH constants
    // Need to be prefixed by App's data directory path
    // Use static filesRoot(Context ctx)

    static String SEATTLE_ZIP = "seattle_android.zip";
    public static String PYTHON_HOME = "python";
    public static String PYTHON_PATH = "seattle/seattle_repy";

    // Used to check if Seattle is installed
    static String VESSEL_PATH = "seattle/seattle_repy/v1/";

    private boolean DEV; /* check if been to dev mode before */

    // A code to filter permission requests issued by us in the permission request callback
    public final int SENSIBILITY_RUNTIME_PERMISSIONS = 1;

    private void _trySleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public static String filesRoot(Context ctx) {
        return ctx.getFilesDir().getPath() + "/";
    }

    /*
     * About Home screen that shows if Python was installed, Seattle was installed
     * and Nodemanager is running
     */
    private void updateHome() {

        final TextView pythonInstalled = (TextView) findViewById(R.id.pythonSetup);
        final TextView seattleInstalled = (TextView) findViewById(R.id.seattleSetup);
        final TextView nodemanagerRunning = (TextView) findViewById(R.id.nodeRunning);

        final int red =  getResources().getColor(android.R.color.holo_red_dark);
        final int green = getResources().getColor(android.R.color.holo_green_dark);
        final int gray = getResources().getColor(android.R.color.darker_gray);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isPythonInstalled()) {
                    pythonInstalled.setText("\u2713 Python Installed");
                    pythonInstalled.setTextColor(green);
                } else {
                    pythonInstalled.setText("\u2715 Python Installed");
                    pythonInstalled.setTextColor(red);
                }

                if (isSeattleInstalled()){

                    seattleInstalled.setText("\u2713 Seattle Installed");
                    seattleInstalled.setTextColor(green);

                } else {
                    seattleInstalled.setText("\u2715 Seattle Installed");
                    seattleInstalled.setTextColor(red);
                }

                //FIXME: find out if nm is running
                nodemanagerRunning.setText("\u2713 Nodemanager running (FIXME)");
                nodemanagerRunning.setTextColor(gray);
            }
        });
    }


    /*
     * method to check if user needs to install any required packages and installs it
     *
     */
    private void installRequired(){
        Log.d(TAG,"Checking install requirements");

        /* setup progress spinner */
        final ProgressDialog progress = new ProgressDialog(this); /* set the context to the app's context */
        progress.setMessage("Checking for requirements...");      /* set a default message */
        progress.setIndeterminate(true);                          /* no total percent just spinning */
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);                            /* can't close the dialog */
        progress.show();                                          /* show the progress dialog */


        /* Thread to install components and update the progress dialog */
        Thread requireThread = new Thread() {
            @Override
            public void run() {

                // Install Python if not installed
                if (! isPythonInstalled()) {
                    /* run UI thread for installing component and updating dialog */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setMessage("Setting up python..."); /* update dialog */
                            installPython();                             /* install python */
                        }
                    });

                    // Give some time for user feedback
                    _trySleep(1000);
                    updateHome();
                }

                // Install Seattle if not installed
                if(! isSeattleInstalled()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setMessage("Setting up seattle..");   /* update dialog */
                            try {
                                URL def = new URL(DEFAULT_DOWNLOAD_URL);
                                installSeattleFromURL(def);                    /* install seattle from default link */
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    // Give some time for user feedback
                    _trySleep(1000);
                    updateHome();
                }

                // Start Seattle if not installed
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setMessage("Starting Seattle...");   /* update dialog */
                        startSeattleNodemanager();                    /* start seattle after installing */
                    }
                });

                // Give some time for user feedback
                _trySleep(1000);
                updateHome();

                progress.dismiss(); /* close progress dialog at the end */
            }
        };
        requireThread.start(); /* start the thread */
    }





    /* ##########################################################################################
     * Installation
     * ##########################################################################################
     */

    /*
     * Python installation means unzipping the contents of res/raw/python_lib.zip
     * into the app's files directory.
     *
     * If the app's files directory has a subdirectory called Python we assume that
     * Python is installed.
     *
     */
    private boolean isPythonInstalled() {
        Log.d(TAG, "Checking if Python installed");
        Context ctx = getApplicationContext();

        File pythonDir = new File(filesRoot(ctx) + PYTHON_HOME);
        if(pythonDir.isDirectory()) {
            return true;
        }
        return false;
    }

    /*
     * Seattle installation means downloading the seattle android installer from the custom
     * installer builder (CIB), either by taking the CIB URL from the App Installation referrer or
     * by specifying a CIB URL in the manual fragment or by copying it over from
     * res/raw/seattle_installer.zip and running seattleinstaller.py
     *
     * In any case we expect a directory "seattle/seattle_repy/v1" to exist in the app's
     * data directory, if the installation was successful.
     *
     * If the app's files directory has a subdirectory called Python we assume that
     * Python is installed.
     *
     */
    private boolean isSeattleInstalled() {
        Log.d(TAG, "Checking if Seattle is installed");
        Context ctx = getApplicationContext();

        File vesselDir = new File(filesRoot(ctx) + VESSEL_PATH);
        if(vesselDir.isDirectory()) {
            return true;
        }
        return false;
    }


    private boolean isSeattleRunning() {
        return false;
    }


    /*
     * Unzip the contents of res/raw/python_lib.zip into the app's files directory.
     */
    private void installPython() {
        Log.d(TAG, "Entering installPython");
        Context ctx = getApplicationContext();

        Log.d(TAG, String.format("Unpacking python to %s", filesRoot(ctx)));
        try {
            Utils.unzip(getResources().openRawResource(R.raw.python_lib), filesRoot(ctx), true);
        } catch (Exception e) {
            Log.d(TAG, String.format("Couldn't unpack python archive: %s", e.getMessage()));
        }
    }


    /*
     * Return the download URL for the Sensibility/Repy installer.
     * It is either provided by the user through the `url_edittext`
     * textbox, or (if that is empty) taken from the hardcoded default.
     */
    private URL getDownloadUrl() throws MalformedURLException {

        Context ctx = getApplicationContext();

        // Get the user's desired download URL, if any
        EditText editText = (EditText) findViewById(R.id.url_edittext);
        String user_download_url = editText.getText().toString();

        if (user_download_url.isEmpty()) {
            Log.i(TAG, "No URL passed downloading from zip");
            Toast.makeText(ctx, "Downloading from zip...", Toast.LENGTH_SHORT);
            return null;
        } else {
            Log.i(TAG, "Downloading from LINK: " + user_download_url);
            Toast.makeText(ctx, "Downloading from: " + user_download_url, Toast.LENGTH_SHORT);
            return new URL(user_download_url);
        }
    }

    private void installSeattleFromURL(final URL download_url) {
        Log.d(TAG, "Entering downloadAndInstallSeattle");
        final Context ctx = getApplicationContext();
        final String seattleZIP = filesRoot(ctx) + SEATTLE_ZIP;

        Thread t = new Thread() {
            @Override
            public void run() {
                try {

                    if(download_url == null){
                        installSeattleFromRaw();
                        return;
                    }

                    Log.d(TAG,String.format("Downloading installer from %s to %s",
                            download_url.toString(), seattleZIP));

                    // Download seattle installer package and unpack to internal storage
                    InputStream input;
                    OutputStream output;

                    HttpsURLConnection connection;
                    connection = (HttpsURLConnection) download_url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                        Log.d(TAG, String.format("Connection failed, Code: %d, Message: %s",
                                connection.getResponseCode(), connection.getResponseMessage()));
                        Log.d(TAG, "Aborting installation");
                        Toast.makeText(ctx, "Could not download from URL!",Toast.LENGTH_SHORT);
                        return;
                    }

                    input = connection.getInputStream();
                    output = new FileOutputStream(seattleZIP);

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
                    Log.d(TAG, String.format("Unpacking downloaded seattle to %s", filesRoot(ctx)));

                    Utils.unzip(new FileInputStream(seattleZIP), filesRoot(ctx), true);

                } catch (Exception e) {
                    Log.d(TAG, String.format("Download failed: %s", e.getMessage()));
                    Log.d(TAG, "Aborting installation");
                    return;
                }
                startSeattleInstaller();
            }
        };
        t.start();
    }


    private void installSeattleFromRaw() {
        Log.d(TAG, "Entering rawResourceInstallSeattle");
        Context ctx = getApplicationContext();

        int seattleRawResourceId = getResources()
                .getIdentifier("seattle_android", "raw", getPackageName());

        if(seattleRawResourceId == 0){
            Log.d(TAG, "Could not download from zip");
            Toast.makeText(ctx, "Could not download from zip", Toast.LENGTH_SHORT);
            return;
        }

        Log.d(TAG, String.format("Unpacking seattle from raw resources to %s", filesRoot(ctx)));

        try {
            // The raw resource might not exist
            Utils.unzip(getResources().openRawResource(seattleRawResourceId), filesRoot(ctx), true);
        } catch (Exception e) {
            Log.d(TAG, String.format("Could not unpack seattle: %s", e.getMessage()));
            Log.d(TAG, "Aborting installation");
            return;
        }
        startSeattleInstaller();
    }


    /*
     * Start Python Interpreter Service Process and execute seattleinstaller.py
     */
    private void startSeattleInstaller() {
        Log.d(TAG, "Entering installSeattle");

        // Get free disk space
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long availableDiskSpace = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();

        String[] python_args = {
                "seattleinstaller.py", "--percent", "50",
                "--disable-startup-script",
                "--diskused", Long.toString(availableDiskSpace)};
        Log.d(TAG, String.format("Calling PythonInterpreterService.startService with args %s", (Object[])python_args));

        PythonInterpreterService.startService(python_args, getBaseContext());
    }


    /*
     * Start Python Interpreter Service Process and execute nmmain.py
     * Runs infinitely
     */
    private void startSeattleNodemanager() {
        Log.d(TAG, "Entering startSeattle");
        String[] python_args = {"nmmain.py", "--foreground"};
        Log.d(TAG, String.format(
                "Calling PythonInterpreterService.startService with args %s", (Object[])python_args));
        PythonInterpreterService.startService(python_args, getBaseContext());
    }


    private void stopSeattle() {
        Log.d(TAG, "Killing not implemented");
    }

    /* ##########################################################################################
     * Setup
     * ##########################################################################################
     */


    private void setupTabLayout(){
        Log.d(TAG, "Entering setupTabLayout");

        setContentView(R.layout.tabs);
        final TabHost tabhost = (TabHost) findViewById(android.R.id.tabhost);
        tabhost.setup();

        Log.d(TAG, "Adding tabs to layout");
        TabHost.TabSpec tabspec = tabhost.newTabSpec("home_tab_tag");
        tabspec.setContent(R.id.home);
        tabspec.setIndicator("Home");
        tabhost.addTab(tabspec);

        tabspec = tabhost.newTabSpec("manual_tab_tag");
        tabspec.setContent(R.id.manual);
        tabspec.setIndicator("Manual");
        tabhost.addTab(tabspec);

        installRequired();

        Log.d(TAG,"add tab listeners");
        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                int i = tabhost.getCurrentTab();
                Log.d("TAG", String.format("Clicked tab number: %s", i));

                if (i == 0) {
                    installRequired();
                } else if (i == 1) {
                    initializeButtons();
                    DEV = true;
                }
            }
        });
    }

    /*
     * Setup the buttons for the manual fragment
     */
    private void initializeButtons() {
        Log.d(TAG, "Entering initializing buttons");

        if(!DEV){ /* check if been to dev mode before (if not then setup the buttons */
            final Button btn_install_seattle = (Button) findViewById(R.id.install_seattle);
            final Button btn_start = (Button) findViewById(R.id.start);
            final Button btn_kill = (Button) findViewById(R.id.kill);

            // Define listeners for buttons
            Log.d(TAG, "Defining button listeners");

            btn_install_seattle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Clicked 'Install Seattle (download)' button");
                    try {
                        URL downloadFrom = getDownloadUrl();
                        installSeattleFromURL(downloadFrom);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });

            btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Clicked 'Start' button");
                    startSeattleNodemanager();
                }
            });

            btn_kill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Clicked 'Stop' button");
                    stopSeattle();
                }
            });
        }
    }


    /*
     * Check every required permission and request in case we don't have it yet
     * In API > 23 we have to ask for permissions at runtime and permissions can also be revoked
     * But we cannot (verify this) request permissions from a background service process, therefor
     * I suggest we request permissions when the activity starts and just pass on the exceptions
     * in the services (if a needed permission was not granted or revoked)
     */
    protected void checkRequestPermissions() {
        Log.d(TAG, "Entering checkRequestPermissions");
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };
        for (String permission : permissions) {
            Log.d(TAG, String.format("Checking permission %s", permission));
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, String.format("Requesting permission %s", permission));
                ActivityCompat.requestPermissions(
                        this, new String[]{permission}, SENSIBILITY_RUNTIME_PERMISSIONS);
            } else {
                Log.d(TAG, String.format("Permission %s already granted", permission));
            }
        }
    }

/*    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "Entering onRequestPermissionsResult");
        switch (requestCode) {
            case SENSIBILITY_RUNTIME_PERMISSIONS: {
                Log.d(TAG, "Permission was requested by Sensibility");

                Log.d(TAG, String.format("Requested %d permission(s)", permissions.length));
                Log.d(TAG, String.format("Received %d permission request result(s)", grantResults.length));

                for (int grantResult : grantResults){
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "Permission denied by user");

                    } else if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Permission granted by user");
                    } else {
                        Log.d(TAG, "Permission result unknown");
                    }
                }
//                if (grantResults.length <= 0 || permissions.length <= 0 ||
//                        grantResults[0] == PackageManager.PERMISSION_DENIED) {
//
//                }
            }
        }
        return;
    }*/

    /* ##########################################################################################
     * Activity Lifecycle Callbacks
     * ##########################################################################################
     */


    /*
     * Shows the User Interface
     * Called by system,
     *   - onCreate()
     *   - onRestart()
     */
    @Override
    protected void onStart() {
        Log.d(TAG, "Entering onStart");
        super.onStart();

        Log.d(TAG, "Calling checkRequestPermissions");
        checkRequestPermissions();

    }


    /*
     * Sets the user interface
     *
     * Called by system after:
     *  - Activity launched
     *  - App process was killed and user navigates back to the activity
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Entering onCreate");
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Calling initializePaths");

        DEV = false; /* set dev mode to false to start */

        Log.d(TAG, "Calling setupTabLayout");
        setupTabLayout();
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "Entering onResume");
        super.onResume();
    }

}
