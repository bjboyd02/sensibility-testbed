package com.snakei;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sensibility_testbed.SensibilityApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ObjectInput;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by lukas on 6/3/16.
 *
 * A pseudo Service that facades misc Android Info calls
 *
 * Provides info for
 *   - WiFi
 *   - Cellular network
 *   - Bluetooth
 *   - Battery
 *   - Settings
 * Todo:
 *   - Wifi
 *      do_wifi_scan should be changed to getWifiInfo
 *   - Cellular network
 *      there could be multiple networks
 *        active network - currently connected to
 *
 *   Exceptions should be raised
 *
 *
 *
 *
 */
public class MiscInfoService extends BroadcastReceiver {
    static final String TAG = "MiscInfoService";
    Context app_context;
    ContentResolver content_resolver;
    ConnectivityManager connectivity_manager;
    TelephonyManager telephony_manager;
    WifiManager wifi_manager;
    Object wifi_sync;
    AudioManager audio_manager;
    DisplayManager display_manager;




    /* See Initialization on Demand Holder pattern */
    private static class MiscInfoServiceHolder {
        private static final MiscInfoService instance = new MiscInfoService();
    }

    /* Classic Singleton Instance Getter */
    public static MiscInfoService getInstance(){
        return MiscInfoServiceHolder.instance;
    }


    public MiscInfoService() {
        app_context = SensibilityApplication.getAppContext();
        content_resolver = app_context.getContentResolver();
        connectivity_manager = (ConnectivityManager) app_context.getSystemService(app_context.CONNECTIVITY_SERVICE);
        telephony_manager = (TelephonyManager) app_context.getSystemService(app_context.TELEPHONY_SERVICE);
        wifi_manager = (WifiManager) app_context.getSystemService(app_context.WIFI_SERVICE);
        wifi_sync = new Object();
        audio_manager = (AudioManager)app_context.getSystemService(app_context.AUDIO_SERVICE);
        display_manager = (DisplayManager)app_context.getSystemService(app_context.DISPLAY_SERVICE);
    }


    /*
     * ###################################################
     * Cellular
     * ###################################################
     */

    /*
     * We can have several networks
     */
//    public boolean isRoaming() {
//        Network[] networks = connectivity_manager.getAllNetworks();
//        for (Network network: networks) {
//            NetworkInfo connectivity_manager.
//        }
//        return true;
//    }

//    /*
//     * e.g. {‘network_operator’: 310260, ‘network_operator_name’: ‘T-Mobile’}.
//     */
//    public ?? getCellularProviderInfo() {
//
//    }
//
//    /*
//     * e.g. {‘cellID’: {‘lac’: 32115, ‘cid’: 26742}, ‘neighboring_cell’: [{‘rssi’: 11, ‘cid’: 26741},
//     * {‘rssi’: 9, ‘cid’: 40151}, {‘rssi’: 5, ‘cid’: 40153}]}.
//     */
//    public ?? getCellInfo() {
//
//
//    }
//
    /*
     * e.g. {‘SIM_operator’: 310260, ‘SIM_operator_name’: ‘’, ‘SIM_country_code’: ‘us’, ‘SIM_state’: ‘ready’}
     */
    public String getSimInfo() {
        JSONObject sim_info_json = new JSONObject();

        telephony_manager.getSimState();
        telephony_manager.


        return sim_info_json.toString();
    }

    public String getCellInfo
//
//    /*
//     * e.g. {‘phone_state’: {‘incomingNumber’: ‘’, ‘state’: ‘idle’},
//     * ‘phone_type’: ‘gsm’, ‘network_type’: 'edge'}. When no SIM card is available,
//     * the phone info dict would be, e.g., {‘phone_state’: {}, ‘phone_type’: ‘gsm’, ‘network_type’: 'unknown'}
//     */
//    public ?? getPhoneInfo() {
//
//    }
//
//    /*
//     * e.g. {"gsm_signal_strength": 8, "evdo_ecio": -1, "gsm_bit_error_rate": -1, "cdma_ecio": -1, "cdma_dbm": -1, "evdo_dbm": -1}
//     */
//    public ?? getCellularSignalStrengths() {
//
//    }
//
    /*
     * ###################################################
     * WiFi
     * ###################################################
     */

    public boolean isWifiEnabled() {
        return wifi_manager.isWifiEnabled();
    }
    public int getWifiState() {
        return wifi_manager.getWifiState();
    }

    /*
     * {
     *  "ssid": network SSID (string),
     *  "bssid": network BSSID, i.e. MAC address (string),
     *  "rssi": received signal strength in dBm (negative int),
     *  "supplicant_state": current WPA association state (string),
     *  "link_speed": link speed in MBps (int),
     *  "mac_address": this device's WiFi interface MAC (string),
     *  XXX "ip_address": this device's IP address (XXX int, byte quadruples reversed!),
     *  XXX "network_id": XXX (int),
     *  "hidden_ssid": True if the SSID is not broadcast (bool)
     *  }
     */
    public String getWifiConnectionInfo() throws JSONException {
        JSONObject wifi_info_json = new JSONObject();
        WifiInfo wifi_info = wifi_manager.getConnectionInfo();

        wifi_info_json.put("ssid",  wifi_info.getSSID());
        wifi_info_json.put("hidden_ssid", wifi_info.getHiddenSSID());
        wifi_info_json.put("bssid", wifi_info.getBSSID());
        wifi_info_json.put("rssi", wifi_info.getRssi());
        wifi_info_json.put("supplicant_state", wifi_info.getSupplicantState().name());
        wifi_info_json.put("link_speed", wifi_info.getLinkSpeed());
        wifi_info_json.put("mac_address", wifi_info.getMacAddress());
        wifi_info_json.put("ip_address", wifi_info.getIpAddress());
        wifi_info_json.put("network_id", wifi_info.getNetworkId());
        wifi_info_json.put("frequency", wifi_info.getFrequency());

        // Dump JSON to string and return
        return wifi_info_json.toString();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Scan returned something");
        synchronized(wifi_sync) {
            wifi_sync.notify();
        }
    }

    /*
     * [{
     *     "ssid": network SSID (string),
     *     "bssid": network BSSID, i.e. MAC address (string),
     *     "frequency": frequency in MHz (int),
     *     "level": received signal strength in dBm (negative int),
     *     "capabilities": security features supported by the network (string)
     *   }, ...]
     * Todo:
     *      I think getWifiScanInfo would be a better name
     */
    public String getWifiScanInfo() throws InterruptedException, JSONException {

        // Register "onReceive" for scan results (gets called from main thread)
        app_context.registerReceiver(this,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // If scan was started wait until onReceive notifies us via
        // the wifi_sync object that scan results are available
        if (wifi_manager.startScan()) {
            synchronized (wifi_sync) {
                wifi_sync.wait();
                JSONArray wifi_json_array = new JSONArray();

                // Scan results are available now
                // Let's call them and convert them to JSON
                for (ScanResult result : wifi_manager.getScanResults()) {
                    JSONObject wifi_json = new JSONObject();
                    wifi_json.put("bssid", result.BSSID);
                    wifi_json.put("ssid", result.SSID);
                    wifi_json.put("capabilities", result.capabilities);
                    wifi_json.put("frequency", result.frequency);
                    wifi_json.put("rssi", result.level);

                    wifi_json_array.put(wifi_json);
                }
                return wifi_json_array.toString();
            }
        }
        app_context.unregisterReceiver(this);

        return null;
    }
//
//    /*
//     * ###################################################
//     * Bluetooth
//     * ###################################################
//     */
//
//
    /*
     * {'state': True, 'scan_mode': 3, 'local_name': 'GT-P1000'}
     */
    public String getBluetoothInfo() {
        JSONObject bluetooth_info_json = new JSONObject();

        return bluetooth_info_json.toString();
    }

    /*
     * ###################################################
     * Battery
     * ###################################################
     */

    /*
     * Returns JSON formatted string of misc battery info
     *
     * Constants can be found at
     * https://developer.android.com/reference/android/os/BatteryManager.html
     *
     * e.g.: {'status': 3, 'temperature': 257, 'level': 99, 'battery_present': True,
     * 'plugged': 2, 'health': 2, 'voltage': 4186, 'technology': 'Li-ion'}
     */
    public String getBatteryInfo() throws JSONException {
        JSONObject battery_info_json = new JSONObject();

        // Register a null receiver - immediately returns Intent
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery_info = app_context.registerReceiver(null, ifilter);

        // Retrieve values
        int status = battery_info.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int temperature = battery_info.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int level = battery_info.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        boolean present = battery_info.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT, false);
        int plugged = battery_info.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        int health = battery_info.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int voltage = battery_info.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        String technology = battery_info.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY, "N/A");

        // Add values to JSON dict
        battery_info_json.put("status",status);
        battery_info_json.put("temperature", temperature);
        battery_info_json.put("level", level);
        battery_info_json.put("battery_present", present);
        battery_info_json.put("plugged", plugged);
        battery_info_json.put("health", health);
        battery_info_json.put("voltage", voltage);
        battery_info_json.put("technology", technology);

        // Dump JSON to string and return
        return battery_info_json.toString();
    }
    /*
     * ###################################################
     * Settings
     * ###################################################
     */

    /*
     * {"airplane_mode": False, "ringer_mode": True}
     */
    public String getModeSettings() throws Settings.SettingNotFoundException, JSONException {
        JSONObject mode_settings_json = new JSONObject();
        boolean airplane_mode = android.provider.Settings.System.getString(content_resolver,
                Settings.Global.AIRPLANE_MODE_ON)  == Settings.Global.AIRPLANE_MODE_ON;
        int ringer_mode = audio_manager.getRingerMode();

        mode_settings_json.put("airplane_mode", airplane_mode);
        mode_settings_json.put("ringer_mode", ringer_mode);

        return mode_settings_json.toString();
    }

    /*
     *  {"screen_on": True, "screen_brightness": 200, "screen_timeout": 60}.
     */
    public String getScreenSettings() throws JSONException {
        JSONObject screen_settings = new JSONObject();

        screen_settings.put("screen_on",);
        screen_settings.put("screen_brightness",);
        screen_settings.put("screen_timeout",);


        return screen_settings.toString();
    }

    /*
     * {"media_volume": xx, "max_media_volume": xxx}
     */
    public String getMediaVolume() throws JSONException {
        JSONObject media_volume_json = new JSONObject();

        media_volume_json.put("media_volume",
                audio_manager.getStreamVolume(AudioManager.STREAM_MUSIC));
        media_volume_json.put("max_media_volume",
                audio_manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        return media_volume_json.toString();
    }

    /*
     * {"ringer_volume": xx, "max_ringer_volume": xxx}
     */
    public String getRingerVolume() throws JSONException {
        JSONObject ringer_volume_json = new JSONObject();

        ringer_volume_json.put("ringer_volume",
                audio_manager.getStreamVolume(AudioManager.STREAM_RING));
        ringer_volume_json.put("max_ringer_volume",
                audio_manager.getStreamMaxVolume(AudioManager.STREAM_RING));

        return ringer_volume_json.toString();
    }

}
