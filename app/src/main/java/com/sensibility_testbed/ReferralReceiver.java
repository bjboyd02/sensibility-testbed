package com.sensibility_testbed;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by
 * albert.rafetseder@univie.ac.at
 * lukas.puehringer@nyu.edu
 *
 * Implements an Android Broadcast Receiver that filters a com.android.vending.INSTALL_REFERRER
 * intent, passed via the app installation Google Play URL.
 *
 * Although this mechanism is intended for market analysis we use it to pass the unique
 * custom installer URL that points to the installer with the device owner's keys.
 *
 * The URL is expected to be the value of the optional parameter: "utm_content", the other
 * parameters are ignored.
 *
 * Once the URL is received we store it to a static variable, which can be accessed from other
 * components, e.g. SensibilityActivity, using a static method.
 *
 * Here is a shell script to broadcast a test referral intent (remove asterisks and backticks):
 *   ```
 *
 *   #! /bin/bash
 *
 *   utm_source="cib"
 *   utm_medium="cib"
 *   utm_term="cib"
 *   utm_campaign="cib"
 *   utm_content="https://alpha-ch.poly.edu/cib/85663d3d84850a435f6169ff571c2464c02484af/installers/android/"
 *
 *   echo "am broadcast \
 *   -a com.android.vending.INSTALL_REFERRER \
 *   -n com.sensibility_testbed/com.sensibility_testbed.ReferralReceiver \
 *   --es referrer \
 *   \"utm_source=${utm_source}&utm_medium=${utm_medium}&utm_term=${utm_term}&utm_content=${utm_content}&utm_campaign=${utm_campaign}\"; \
 *   exit" | adb shell
 *
 *   ```
 *
 */

public class ReferralReceiver extends BroadcastReceiver {

    private static final String TAG = "ReferralReceiver";
    private static URL customInstallerUrl;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d(TAG, String.format("Received Referral Intent"));

            String referrer = intent.getStringExtra("referrer");
            referrer = URLDecoder.decode(referrer, "utf-8");

            // Parse the query string, extracting the relevant data
            String[] params = referrer.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("utm_content")) {
                    customInstallerUrl = new URL(pair[1]);
                    Log.d(TAG, String.format("Received custom installer URL %s",
                            customInstallerUrl.toString()));
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, String.format("Bad referrer encoding: %s", e.getMessage()));
        } catch (MalformedURLException e) {
            Log.d(TAG, String.format("Malformed custom installer URL: %s", e.getMessage()));
        } catch (Exception e) {
            Log.d(TAG, String.format("Problem in custom installer ReferralReceiver: %s",
                    e.getMessage()));
        }
    }

    public static URL getCustomInstallerReferralUrl() {
        return customInstallerUrl;
    }
}
