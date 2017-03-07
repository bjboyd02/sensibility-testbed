package com.sensibility_testbed;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import com.snakei.PythonInterpreterService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;

import static com.sensibility_testbed.ReferralReceiver.getCustomInstallerReferralUrl;

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
            "https://alpha-ch.poly.edu/cib/85663d3d84850a435f6169ff571c2464c02484af/installers/android/";

    // RELATIVE PATH constants
    // Need to be prefixed by App's data directory path
    // Use static filesRoot(Context ctx)

    static String SEATTLE_ZIP = "seattle_android.zip";
    public static String PYTHON_HOME = "python";
    public static String PYTHON_PATH = "seattle/seattle_repy";

    // Used to check if Seattle is installed
    static String SEATTLE_PATH = "seattle/seattle_repy";
    static String VESSEL_PATH = "seattle/seattle_repy/v1/";

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
        final TextView seattleDownloaded = (TextView) findViewById(R.id.seattleDownload);
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

                if (isSeattleDownloaded()) {
                    seattleDownloaded.setText("\u2713 Custom Installer Downloaded");
                    seattleDownloaded.setTextColor(green);
                } else {
                    seattleDownloaded.setText("\u2715 Custom Installer Downloaded");
                    seattleDownloaded.setTextColor(red);
                }

                if (isSeattleInstalled()){
                    seattleInstalled.setText("\u2713 Custom Installer Installed");
                    seattleInstalled.setTextColor(green);

                } else {
                    seattleInstalled.setText("\u2715 Custom Installer Installed");
                    seattleInstalled.setTextColor(red);
                }

                //FIXME: find out if nm is running
                nodemanagerRunning.setText("\u2713 Nodemanager running (FIXME)");
                nodemanagerRunning.setTextColor(gray);
            }
        });
    }


    /*
     * Get progress dialog spinning wheel
     * Use progress.setMessage() to add an additional message and
     * progress.show() and progress.dismiss() to show and hide the progress dialog
     *
     */
    private ProgressDialog getSpinningWheel() {
        Log.d(TAG, "Entering showSpinningWheel");

        // Create new ProgressDialog in the app's context
        ProgressDialog progress = new ProgressDialog(this);
        // No total percent just spinning
        progress.setIndeterminate(true);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Disable user input to cancel the dialog
        progress.setCancelable(false);

        return progress;
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
     * Seattle is Downloaded if there is a seattle/seattle_repy directory in the app's data dir
     */
    private boolean isSeattleDownloaded() {
        Log.d(TAG, "Checking if Seattle is downloaded/unpacked");
        Context ctx = getApplicationContext();

        File vesselDir = new File(filesRoot(ctx) + SEATTLE_PATH);
        if(vesselDir.isDirectory()) {
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

    //FIXME: Dedicate process to nodemanager and check if that process is running using ActivityManager
    private boolean isSeattleRunning() {
        return true;
    }


    /*
     * Install Python
     * Unzip the contents of res/raw/python_lib.zip into the app's files directory
     */
    private void installPython() {
        Log.d(TAG, "Entering installPython");
        final Context ctx = getApplicationContext();

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

        // Get the user's desired download URL, if any
        EditText editText = (EditText) findViewById(R.id.url_edittext);
        String user_download_url = editText.getText().toString();

        if (user_download_url.isEmpty()) {
            Log.i(TAG, "No URL passed downloading from zip");
            return null;
        } else {
            Log.i(TAG, "Downloading from LINK: " + user_download_url);
            return new URL(user_download_url);
        }
    }

    /*
     * Download Sensibility Custom Installer
     */
    private void downloadCustomInstallerFromURL(final URL download_url) {
        Log.d(TAG, "Entering downloadAndInstallSeattle");
        final Context ctx = getApplicationContext();
        final String seattleZIP = filesRoot(ctx) + SEATTLE_ZIP;

        try {
            if(download_url == null){
                return;
            }

            Log.d(TAG,String.format("Downloading custom installer from %s to %s",
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
            Log.d(TAG, String.format("Downloading custom installer failed: %s", e.getMessage()));
        }
    }


    /*
     * Alternatively to downloading Seattle from the Custom Installer builder you can
     * build the app with a seattle_android.zip in res/raw, which is used as source in this function.
     */
    private void copySeattleFromRaw(int seattleRawResourceId) {
        Log.d(TAG, "Entering rawResourceInstallSeattle");
        Context ctx = getApplicationContext();

        Log.d(TAG, String.format("Unpacking seattle from raw resources to %s", filesRoot(ctx)));

        try {
            // The raw resource might not exist
            Utils.unzip(getResources().openRawResource(seattleRawResourceId), filesRoot(ctx), true);
        } catch (Exception e) {
            Log.d(TAG, String.format("Could not unpack seattle: %s", e.getMessage()));
            Log.d(TAG, "Aborting installation");
            return;
        }
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

        // Developer Install UI "Start Without Affix (no NAT) checkbox
        CheckBox affix_checkbox = (CheckBox)findViewById(R.id.with_affix);
        boolean with_affix = affix_checkbox.isChecked();
        Log.d(TAG, String.format("Affix Checkbox:  %b", with_affix));

        String[] python_args = {"nmmain.py", "--foreground"};

        Log.d(TAG, String.format(
                "Calling PythonInterpreterService.startService with args %s", (Object[])python_args));
        PythonInterpreterService.startService(python_args, getBaseContext());
    }

    /*
     * A single Asynchronous Task that copies Python, Downloads Seattle from a pasted URL
     * or if not specified from res/raw and runs seattleinstaller.py forcefully, i.e. independently
     * of the state of the app.
     */
    private void developInstall() {
        // Get a progress dialog with a spinning wheel
        final ProgressDialog progress = getSpinningWheel();

        new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {

                publishProgress("Copying Python...");
                installPython();

                // Fetch Custom Installer URL
                URL url = null;
                try {
                    url = getDownloadUrl();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                _trySleep(1000);

                // If URL was not specified use debug installer from res raw
                // Abort if no debug instsaller zip is in res/raw
                if (url == null) {
                    int seattleRawResourceId = getResources()
                            .getIdentifier("seattle_android", "raw", getPackageName());

                    if(seattleRawResourceId == 0) {
                        Log.d(TAG, "Could not download from zip");
                        publishProgress("No URL was specified, and no installer was " +
                                "found in res raw, aborting installation ...");
                        _trySleep(3000);
                        return false;

                    } else {
                        publishProgress("No URL was specified, copying Custom Installer from debug zip...");
                        copySeattleFromRaw(seattleRawResourceId);
                    }

                // If URL was specified download Custom Installer from URL
                } else {
                    publishProgress(String.format("Downloading Custom Installer from %s...", url.toString()));
                    downloadCustomInstallerFromURL(url);
                }

                _trySleep(1000);

                // Start seattleinstaller.py in background process if not yet installed
                publishProgress("Starting seattleinstaller.py...");
                startSeattleInstaller();

                _trySleep(1000);

                return true;
            }
            protected void onPreExecute() {
                progress.show();
            }

            protected void onProgressUpdate(String... progressMessage) {
                progress.setMessage(progressMessage[0]);
            }

            protected void onPostExecute(Boolean result) {
                progress.dismiss();
            }
        }.execute();
    }

    private void developStart() {
        final ProgressDialog progress = getSpinningWheel();

        new AsyncTask<Void, String, Boolean>() {
            boolean with_affix;

            @Override
            protected Boolean doInBackground(Void... voids) {
                startSeattleNodemanager();
                _trySleep(1000);
                return true;
            }
            protected void onPreExecute() {
                progress.show();

                CheckBox affix_checkbox = (CheckBox)findViewById(R.id.with_affix);
                with_affix = affix_checkbox.isChecked();
                progress.setMessage(
                        String.format("Starting Nodemanager (%s affix)",
                                (with_affix ? "with" : "without")));
            }

            protected void onProgressUpdate(String... progressMessage) {
                progress.setMessage(progressMessage[0]);
            }

            protected void onPostExecute(Boolean result) {
                progress.dismiss();
            }
        }.execute();
    }


    /*
     * A single Asynchronous Task that executes all install tasks sequentially
     * while updating the UI and giving progress feedback. The tasks are:
     * - Copying over Python library files from res/raw
     * - Downloading custom installer from CIB website using passed referrer
     * - Starting seattleinstaller.py
     * - Starting nodemanager.py
     *
     */
    private void autoInstall() {

        // Get a progress dialog with a spinning wheel
        final ProgressDialog progress = getSpinningWheel();

        new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {

                // Install Python if not installed
                if (! isPythonInstalled()) {
                    publishProgress("Installing Python...");
                    installPython();

                    _trySleep(1000);

                    if (isPythonInstalled()) {
                        publishProgress("Successfully installed Python!");
                        updateHome();
                    } else {
                        publishProgress("Python could not be installed!");
                        return false;
                    }
                    _trySleep(1000);
                }


                // Download Custom Installer if not downloaded
                if (! isSeattleDownloaded()) {
                    publishProgress("Downloading Custom Installer...");

                    URL url = getCustomInstallerReferralUrl();

                    if (url == null) {
                        publishProgress("No Custom Installer URL available. " +
                                "Please switch to the DEVELOP tab and provide the URL manually!");
                        _trySleep(3000);
                        return false;
                    }
                    downloadCustomInstallerFromURL(url);

                    if (isSeattleDownloaded()) {
                        publishProgress("Successfully downloaded Custom Installer!");
                        updateHome();

                    } else {
                        publishProgress("Custom Installer could not be downloaded!");
                        return false;
                    }
                    _trySleep(1000);
                }


                // Start seattleinstaller.py in background process if not yet installed
                if (! isSeattleInstalled()) {
                    publishProgress("Installing Custom Installer...");
                    startSeattleInstaller();

                    // Wait at most 20 seconds for installation to finish
                    int waitForSec = 20;
                    // FIXME: Think of a better way to do this
                    for (int i = 0; i < waitForSec; i++) {
                        _trySleep(1000);

                        if (isSeattleInstalled()) {
                            publishProgress("Successfully installed Custom Installer!");
                            updateHome();
                            break;
                        }
                    }

                    if (! isSeattleInstalled()) {
                        publishProgress("Custom Installer was not installed in a timely manner!");
                        return false;
                    }
                    _trySleep(1000);
                }


                // Start nodemanager
                if (! isSeattleRunning()) {
                    publishProgress("Starting Nodemanager...");
                    startSeattleNodemanager();

                    if (isSeattleRunning()) {
                        publishProgress("Successfully started Nodemanager!");
                        updateHome();

                    } else {
                        publishProgress("Nodemanager could not be started!");
                        return false;
                    }
                    _trySleep(1000);
                }

                updateHome();
                return true;
            }
            protected void onPreExecute() {
                progress.show();
            }

            protected void onProgressUpdate(String... progressMessage) {
                progress.setMessage(progressMessage[0]);
            }

            protected void onPostExecute(Boolean result) {
                progress.dismiss();
            }
        }.execute();
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
        tabspec.setIndicator("Auto");
        tabhost.addTab(tabspec);

        tabspec = tabhost.newTabSpec("manual_tab_tag");
        tabspec.setContent(R.id.manual);
        tabspec.setIndicator("Develop");
        tabhost.addTab(tabspec);

        Log.d(TAG,"add tab listeners");
        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
               if (tabhost.getCurrentTab() == 0) {
                   updateHome();
               }
            }
        });
    }

    /*
     * Setup the buttons for the manual fragment
     */
    private void initializeButtons() {
        Log.d(TAG, "Entering initializing buttons");

        final Button btn_auto_install = (Button) findViewById(R.id.auto_install);
        final Button btn_install_seattle = (Button) findViewById(R.id.install_seattle);
        final Button btn_start = (Button) findViewById(R.id.start);
        final Button btn_kill = (Button) findViewById(R.id.kill);

        // Define listeners for buttons
        Log.d(TAG, "Defining button listeners");

        btn_auto_install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked 'Install' button");
                autoInstall();
            }
        });

        btn_install_seattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked 'Install Seattle (download)' button");
                developInstall();
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked 'Start' button");
                developStart();
            }
        });

        btn_kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked 'Kill' button");
                stopSeattle();
            }
        });
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

        // Update Auto view
        updateHome();
    }


    /*
     * Sets the user interface
     *
     * Called by system after:
     *  - Activity launched
     *  - App process was killed and user navigates back to the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Entering onCreate");
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Calling checkRequestPermissions");
        checkRequestPermissions();

        Log.d(TAG, "Calling setupTabLayout");
        setupTabLayout();

        Log.d(TAG, "Calling initializeButtons");
        initializeButtons();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Entering onResume");
        super.onResume();
    }

}
