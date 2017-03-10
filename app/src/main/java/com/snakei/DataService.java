package com.snakei;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PatternMatcher;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukp on 3/10/17.
 */

public class DataService {
    static final String TAG = "DataService";

    public static final String SEND_DATA_ACTION = "com.snakei.SEND_DATA";
    public static final String SEND_DATA_SCHEME = "sensi";
    public static final String SEND_DATA_PATH = "send.data";

    private Context cached_context;
    private BroadcastReceiver uriReceiver;

    JSONArray all_data_json = new JSONArray();

    /* See Initialization on Demand Holder pattern */
    private static class DataServiceHolder {
        private static final DataService instance = new DataService();
    }

    /* Classic Singleton Instance Getter */
    public static DataService getInstance() {
        return DataServiceHolder.instance;
    }

    public void init(Context context) {
        Log.d(TAG, "Entering init");
        cached_context = context;
    }

    /*
     * Registers an broadcast receiver with an intent filter for actions of custom type
     * SEND_DATA_ACTION with URIS of format SEND_DATA_SCHEME://SEND_DATA_PATH
     *
     * Appends the received data to a JSONArray attribute.
     *
     */
    public void start_data() {
        Log.d(TAG, "Entering start_uri_listener");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SEND_DATA_ACTION);
        intentFilter.addDataScheme(SEND_DATA_SCHEME);
        intentFilter.addDataPath(SEND_DATA_PATH, PatternMatcher.PATTERN_LITERAL);

        uriReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Entering onReceive");

                // Get URI data...
                Uri uri = intent.getData();

                // The URI should be of format
                // SEND_DATA_SCHEME://SEND_DATA_PATH

                // Is null possible, given above intent filters?
                if (uri != null) {
                    Log.d(TAG, uri.toString());
                    JSONArray result = new JSONArray();
                    result.put(System.currentTimeMillis());

                    JSONObject data = new JSONObject();
                    // ... we are only interested in the query parameters
                    for (String name : uri.getQueryParameterNames()) {
                        try {
                            data.put(name, uri.getQueryParameter(name));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    result.put(data);
                    all_data_json.put(result);
                }
            }
        };
        cached_context.registerReceiver(uriReceiver, intentFilter);
    }

    public void stop_data() {
        Log.d(TAG, "Entering stop_uri_listener");
        cached_context.unregisterReceiver(uriReceiver);
        uriReceiver = null;
    }

    public String getMostRecentData() throws JSONException {
        int len = all_data_json.length();
        if (len > 0) {
            return all_data_json.get(len - 1).toString();
        }
        return null;
    }

    public String getAllData() {
        if (all_data_json.length() > 0) {
            return all_data_json.toString();
        }
        return null;
    }
}
