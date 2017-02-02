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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

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
 */
public class SensibilityActivity extends Activity {

    public final String TAG = "SensibilityActivity";

    // XXX REPLACE!!
    private String DOWNLOAD_URL =
            "https://alpha-ch.poly.edu/cib/87fb8a4763eb8bc76d058123f629986e85c65f7a/installers/android";

    private String ALPHA_CIB_CERTIFICATE;
    private String FILES_ROOT;
    private String SEATTLE_ZIP;
    private String PYTHON;
    private String PYTHON_LIB;
    private String PYTHON_SCRIPTS;

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
    private void installPython() {
        Log.i(TAG, String.format("Unpacking python archive to %s", FILES_ROOT));
        try {
            Utils.unzip(getResources().openRawResource(R.raw.python_lib), FILES_ROOT, true);
        } catch (IOException e) {
            Log.i(TAG, "Couldn't unpack python archive");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void installSeattle() {
        Log.i(TAG, String.format("Unpacking python zip archive to %s", FILES_ROOT));
        try {
            Utils.unzip(getResources().openRawResource(R.raw.seattle_android), FILES_ROOT, true);
        } catch (IOException e) {
            Log.i(TAG, "Couldn't unpack python archive");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Starting seattleinstaller.py");
        String[] python_args = {"seattleinstaller.py",
                "--percent", "50",
                "--disable-startup-script",
                "True"};
        PythonInterpreterService.startService(python_args, getBaseContext());
    }

    private SSLContext getSSLContextForSelfSignedCertificate(InputStream cert_stream)
            throws CertificateException, IOException, KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {

        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");


        Certificate ca;
        try {
            ca = cf.generateCertificate(cert_stream);
        } finally {
            cert_stream.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext ssl_context = SSLContext.getInstance("TLS");
        ssl_context.init(null, tmf.getTrustManagers(), null);

        return ssl_context;
    }

    private void downloadAndInstallSeattle() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, String.format("Downloading seattle from %s to %s", DOWNLOAD_URL, SEATTLE_ZIP));
                    // Download seattle installer package and unpack to internal storage
                    InputStream input;
                    OutputStream output;

                    // Use example code from
                    // https://developer.android.com/training/articles/security-ssl.html#HttpsExample
                    // to handle self-signed certificate
                    URL url = new URL(DOWNLOAD_URL);

                    HttpsURLConnection connection;
                    connection = (HttpsURLConnection) url.openConnection();

                    InputStream alpha_cib_cert = getResources().openRawResource(R.raw.alpha_cib);
                    SSLContext ssl_context = getSSLContextForSelfSignedCertificate(alpha_cib_cert);
                    connection.setSSLSocketFactory(ssl_context.getSocketFactory());

                    connection.connect();

                    if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                        Log.i(TAG, connection.getResponseMessage());
                        return;
                    }

                    input = connection.getInputStream();
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
                    installSeattle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void startSeattle() {
        String[] python_args = {"nmmain.py", "--foreground"};
        PythonInterpreterService.startService(python_args, getBaseContext());
    }

    private void killSeattle() {
        Log.i(TAG, "Killing not implemented");
    }

    private void initializeSimpleLayout() {
        setContentView(R.layout.simple_layout);

        //Initialize buttons
        final Button btn_install_python = (Button) findViewById(R.id.install_python);
        final Button btn_install_seattle_ref = (Button) findViewById(R.id.install_seattle_referrer);
        final Button btn_install_seattle_zip = (Button) findViewById(R.id.install_seattle_zip);
        final Button btn_start = (Button) findViewById(R.id.start);
        final Button btn_kill = (Button) findViewById(R.id.kill);

        // Define listeners for buttons

        btn_install_python.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installPython();
            }
        });
        btn_install_seattle_ref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAndInstallSeattle();
            }
        });
        btn_install_seattle_zip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installSeattle();
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSeattle();
            }
        });
        btn_kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killSeattle();
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
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{permission}, SENSIBILITY_RUNTIME_PERMISSIONS);
            }
        }
    }

    /*
     * Initialize "constant" global paths which cannot be initialized in class scope because they
     * need a context.
     */
    private void initializePaths() {
        FILES_ROOT = getApplicationContext().getFilesDir().getPath() + "/";
        SEATTLE_ZIP = FILES_ROOT + "seattle_android.zip";
        PYTHON = FILES_ROOT + "python/";
        PYTHON_LIB = FILES_ROOT + "python/lib/python2.7/";
        PYTHON_SCRIPTS = FILES_ROOT + "scripts/";
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
        checkRequestPermissions();
        initializeSimpleLayout();
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
        super.onCreate(savedInstanceState);
        initializePaths();
        Log.i(TAG, "Into onCreate");
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
