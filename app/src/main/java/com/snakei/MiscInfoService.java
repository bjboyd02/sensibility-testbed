package com.snakei;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
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
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;

import com.sensibility_testbed.SensibilityApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ObjectInput;
import java.io.StringWriter;
import java.util.ArrayList;
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
 */
public class MiscInfoService {
    static final String TAG = "MiscInfoService";
    Context app_context;
    ContentResolver content_resolver;
    ConnectivityManager connectivity_manager;
    TelephonyManager telephony_manager;
    WifiManager wifi_manager;
    BroadcastReceiver wifi_broadcast_receiver;
    Object wifi_sync;
    AudioManager audio_manager;
    DisplayManager display_manager;
    BluetoothManager bluetooth_manager;
    BluetoothAdapter bluetooth_adapter;
    Object bluetooth_sync;
    BroadcastReceiver bluetooth_broadcast_receiver;
    ArrayList<BluetoothDevice> scanned_bluetooth_devices;


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
        wifi_broadcast_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Wifi scan returned");
                synchronized(wifi_sync) {
                    wifi_sync.notify();
                }
            }
        };

        audio_manager = (AudioManager)app_context.getSystemService(app_context.AUDIO_SERVICE);
        display_manager = (DisplayManager)app_context.getSystemService(app_context.DISPLAY_SERVICE);

        bluetooth_manager = (BluetoothManager)app_context.getSystemService(app_context.BLUETOOTH_SERVICE);
        bluetooth_adapter = bluetooth_manager.getAdapter();
        bluetooth_sync = new Object();
        bluetooth_broadcast_receiver = new BroadcastReceiver() {

            // This BroadcastReceiver will receive bluetooth discovery actions
            // for page requests (ACTION_FOUND) and when discovery and all page
            // requests have finished (ACTION_DISCOVERY_FINISHED)

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Bluetooth scan returned");
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Bluetooth discovery has received info from a paged remote device
                    BluetoothDevice remote_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    scanned_bluetooth_devices.add(remote_device);

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    // Bluetooth discovery has finished, there won't be any more page requests
                    // Notify scan function to stop waiting and return values
                    synchronized (bluetooth_sync){
                        bluetooth_sync.notify();
                    }
                }


            }
        };
    }


    /*
     * ###################################################
     * Cellular
     * ###################################################
     */

    /*
     * We can have several networks
     */
    public String getNetworkInfo() throws JSONException {
        Network[] networks = connectivity_manager.getAllNetworks();

        if (networks.length > 0) {
            JSONArray network_info_json_array = new JSONArray();
            for (Network network : networks) {
                JSONObject network_info_json = new JSONObject();
                NetworkInfo network_info = connectivity_manager.getNetworkInfo(network);
                network_info_json.put("detailed_state", network_info.getDetailedState().name()); // Enum
                network_info_json.put("extra_info", network_info.getExtraInfo());
                network_info_json.put("reason", network_info.getReason());
                network_info_json.put("state", network_info.getState().name()); //Enum
                network_info_json.put("subtype", network_info.getSubtype());
                network_info_json.put("subtype_name", network_info.getSubtypeName());
                network_info_json.put("type", network_info.getType());
                network_info_json.put("type_name", network_info.getTypeName());
                network_info_json.put("is_connected", network_info.isConnected());
                network_info_json.put("is_available", network_info.isAvailable());
                network_info_json.put("is_connected_or_connecting", network_info.isConnectedOrConnecting());
                network_info_json.put("is_failover", network_info.isFailover());
                network_info_json.put("is_roaming", network_info.isRoaming());

                network_info_json_array.put(network_info_json);
            }
            return network_info_json_array.toString();
        }
        return null;
    }

    /*
     * e.g. {‘network_operator’: 310260, ‘network_operator_name’: ‘T-Mobile’}.
     */
    public String getCellularProviderInfo() throws JSONException {
        JSONObject provider_info_json = new JSONObject();

        provider_info_json.put("network_operator", telephony_manager.getNetworkOperator());
        provider_info_json.put("network_operator_name",telephony_manager.getNetworkOperatorName());

        return provider_info_json.toString();
    }

    /*
     * Used to be:
     * e.g. {‘cellID’: {‘lac’: 32115, ‘cid’: 26742}, ‘neighboring_cell’: [{‘rssi’: 11, ‘cid’: 26741},
     * {‘rssi’: 9, ‘cid’: 40151}, {‘rssi’: 5, ‘cid’: 40153}]}.
     */
    public String getCellInfo() throws JSONException {

        List<CellInfo> cell_infos = telephony_manager.getAllCellInfo();

        if (cell_infos.size() > 0) {
            JSONArray cell_info_json_array = new JSONArray();
            for (CellInfo cell_info : cell_infos) {
                JSONObject cell_info_json = new JSONObject();
                cell_info_json.put("is_registered", cell_info.isRegistered());
                if (cell_info instanceof CellInfoCdma) {

                    // CDMA Signal Strength
                    CellSignalStrengthCdma signal_strength = ((CellInfoCdma) cell_info).getCellSignalStrength();
                    cell_info_json.put("asu_level", signal_strength.getAsuLevel());
                    cell_info_json.put("cdma_dbm", signal_strength.getCdmaDbm());
                    cell_info_json.put("cdma_level", signal_strength.getCdmaLevel());
                    cell_info_json.put("dbm", signal_strength.getDbm());
                    cell_info_json.put("evdo_dbm", signal_strength.getEvdoDbm());
                    cell_info_json.put("evdo_ecio", signal_strength.getEvdoEcio());
                    cell_info_json.put("evdo_level", signal_strength.getEvdoLevel());
                    cell_info_json.put("evdo_snr", signal_strength.getEvdoSnr());
                    cell_info_json.put("level", signal_strength.getLevel());

                    //CDMA Cell Identity
                    CellIdentityCdma cell_id = ((CellInfoCdma) cell_info).getCellIdentity();

                    cell_info_json.put("base_station_id", cell_id.getBasestationId());
                    cell_info_json.put("base_station_latitude", cell_id.getLatitude());
                    cell_info_json.put("base_station_longitude", cell_id.getLongitude());
                    cell_info_json.put("network_id", cell_id.getNetworkId());
                    cell_info_json.put("system_id", cell_id.getSystemId());

                } else if (cell_info instanceof CellInfoLte) {
                    // LTE Signal Strength
                    CellSignalStrengthLte signal_strength = ((CellInfoLte) cell_info).getCellSignalStrength();
                    cell_info_json.put("dbm", signal_strength.getDbm());
                    cell_info_json.put("asu_level", signal_strength.getAsuLevel());
                    cell_info_json.put("level", signal_strength.getLevel());
                    cell_info_json.put("timing_advance", signal_strength.getTimingAdvance());

                    //LTE Cell Identity
                    CellIdentityLte cell_id = ((CellInfoLte) cell_info).getCellIdentity();
                    cell_info_json.put("ci", cell_id.getCi());
                    cell_info_json.put("mcc", cell_id.getMcc());
                    cell_info_json.put("mnc", cell_id.getMnc());
                    cell_info_json.put("pci", cell_id.getPci());
                    cell_info_json.put("tac", cell_id.getTac());

                } else if (cell_info instanceof CellInfoGsm) {
                    // GSM Signal Strength
                    CellSignalStrengthGsm signal_strength = ((CellInfoGsm) cell_info).getCellSignalStrength();
                    cell_info_json.put("asu_level", signal_strength.getAsuLevel());
                    cell_info_json.put("dbm", signal_strength.getDbm());
                    cell_info_json.put("level", signal_strength.getLevel());

                    //GSM Cell Identity
                    CellIdentityGsm cell_id = ((CellInfoGsm) cell_info).getCellIdentity();
                    cell_info_json.put("mnc", cell_id.getMnc());
                    cell_info_json.put("mcc", cell_id.getMcc());
                    cell_info_json.put("cid", cell_id.getCid());
                    cell_info_json.put("lac", cell_id.getLac());

                } else if (cell_info instanceof CellInfoWcdma) {
                    // WCDMA Signal Strength
                    CellSignalStrengthWcdma signal_strength = ((CellInfoWcdma) cell_info).getCellSignalStrength();
                    cell_info_json.put("asu_level", signal_strength.getAsuLevel());
                    cell_info_json.put("dbm", signal_strength.getDbm());
                    cell_info_json.put("level", signal_strength.getLevel());

                    //WCDMA Cell Identity
                    CellIdentityWcdma cell_id = ((CellInfoWcdma) cell_info).getCellIdentity();
                    cell_info_json.put("psc", cell_id.getPsc());
                    cell_info_json.put("cid", cell_id.getCid());
                    cell_info_json.put("lac", cell_id.getLac());
                    cell_info_json.put("mcc", cell_id.getMcc());
                    cell_info_json.put("mnc", cell_id.getMnc());

                } else {
                    // XXX Throw an exception?
                    Log.i(TAG, "Cell info of unknown Type");
                    continue;
                }
                cell_info_json_array.put(cell_info_json);
            }
            return cell_info_json_array.toString();
        }
        return null;
    }

    /*
     * e.g. {‘SIM_operator’: 310260, ‘SIM_operator_name’: ‘’, ‘SIM_country_code’: ‘us’, ‘SIM_state’: ‘ready’}
     */
    public String getSimInfo() throws JSONException {
        JSONObject sim_info_json = new JSONObject();
        sim_info_json.put("SIM_operator", telephony_manager.getSimOperator());
        sim_info_json.put("SIM_state", telephony_manager.getSimState());
        sim_info_json.put("SIM_country_code", telephony_manager.getSimCountryIso());
        sim_info_json.put("SIM_operator_name", telephony_manager.getSimOperatorName());
        sim_info_json.put("SIM_serial_number", telephony_manager.getSimSerialNumber());

        return sim_info_json.toString();
    }

    /*
     * Used to be:
     * e.g. {‘phone_state’: {‘incomingNumber’: ‘’, ‘state’: ‘idle’},
     * ‘phone_type’: ‘gsm’, ‘network_type’: 'edge'}. When no SIM card is available,
     * the phone info dict would be, e.g., {‘phone_state’: {}, ‘phone_type’: ‘gsm’, ‘network_type’: 'unknown'}
     *
     */
    public String getPhoneInfo() throws JSONException {
        JSONObject phone_info_json = new JSONObject();

        phone_info_json.put("subscriber_id", telephony_manager.getSubscriberId());
        phone_info_json.put("call_state", telephony_manager.getCallState());
        phone_info_json.put("data_activity", telephony_manager.getDataActivity());
        phone_info_json.put("data_state", telephony_manager.getDataState());
        phone_info_json.put("device_id", telephony_manager.getDeviceId());
        phone_info_json.put("device_software_version", telephony_manager.getDeviceSoftwareVersion());
        phone_info_json.put("network_type", telephony_manager.getNetworkType());
        phone_info_json.put("phone_type", telephony_manager.getPhoneType());

        return phone_info_json.toString();
    }


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
        app_context.registerReceiver(wifi_broadcast_receiver,
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
        app_context.unregisterReceiver(wifi_broadcast_receiver);

        return null;
    }

    /*
     * ###################################################
     * Bluetooth
     * ###################################################
     */
    /*
     * {'state': True, 'scan_mode': 3, 'local_name': 'GT-P1000, 'local_address' : "XX:XX:XX:XX:XX:XX"}
     */
    public String getBluetoothInfo() throws JSONException {
        JSONObject bluetooth_info_json = new JSONObject();
        BluetoothAdapter bluetooth_adapter = bluetooth_manager.getAdapter();

        bluetooth_info_json.put("state", bluetooth_adapter.getState());
        bluetooth_info_json.put("scan_mode", bluetooth_adapter.getScanMode());
        bluetooth_info_json.put("local_name", bluetooth_adapter.getName());
        bluetooth_info_json.put("local_address", bluetooth_adapter.getAddress());

        bluetooth_adapter.getClass();

        return bluetooth_info_json.toString();
    }

    /*
     * Start bluetooth discovery and wait until it has returned.
     *
     * Discovery usually involves an inquiry scan of about 12 seconds, followed by a page scan
     * for each found device.
     *
     * Returns a list of remote bluetooth devices:
     * [{
     *      "address": MAC address
     *      "name":
     *      "bond_state": BOND_NONE=10 | BOND_BONDING=11 | BOND_BONDED=12
     *      "type": DEVICE_TYPE_CLASSIC=1 | DEVICE_TYPE_LE=2 | DEVICE_TYPE_DUAL=3 | DEVICE_TYPE_UNKNOWN=0
     * }, ...]
     *
     */

    public String getBluetoothScanInfo() throws JSONException, InterruptedException {

        // Register receiver for page scan result
        // and inquire scan finished
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(BluetoothDevice.ACTION_FOUND);
        ifilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        app_context.registerReceiver(bluetooth_broadcast_receiver, ifilter);

        // Initialize list to which the broadcast receiver will append
        // paged bluetooth devices
        // XXX: Think about caching scan results for a specified amount of time
        scanned_bluetooth_devices = new ArrayList<BluetoothDevice>();

        // Start discovery and wait until it is finished
        if (bluetooth_adapter.startDiscovery()) {
            synchronized(bluetooth_sync) {
                bluetooth_sync.wait();
                // If we have discovered some devices
                // transform infos to JSON and return as String
                if (scanned_bluetooth_devices.size() > 0) {
                    JSONArray bluetooth_json_array = new JSONArray();
                    for (BluetoothDevice remote_device : scanned_bluetooth_devices) {
                        JSONObject bluetooth_json = new JSONObject();
                        bluetooth_json.put("address", remote_device.getAddress());
                        bluetooth_json.put("name", remote_device.getName());
                        bluetooth_json.put("bond_state", remote_device.getBondState());
                        bluetooth_json.put("type", remote_device.getType());

                        bluetooth_json_array.put(bluetooth_json);
                    }
                    return bluetooth_json_array.toString();
                }
            }
        }
        app_context.unregisterReceiver(bluetooth_broadcast_receiver);

        return null;
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
    public String getDisplayInfo() throws JSONException {
        JSONObject screen_settings_json = new JSONObject();
        Display display = display_manager.getDisplay(Display.DEFAULT_DISPLAY);
        Point size = new Point();

        String name = display.getName();
        int state = display.getState();
        int rotation = display.getRotation();
        display.getSize(size);

        String brightness = android.provider.Settings.System.getString(content_resolver,
                Settings.System.SCREEN_BRIGHTNESS);
        String brightness_mode = android.provider.Settings.System.getString(content_resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE);
        String timeout = android.provider.Settings.System.getString(content_resolver,
                Settings.System.SCREEN_OFF_TIMEOUT);

        screen_settings_json.put("name", name);
        screen_settings_json.put("state", state);
        screen_settings_json.put("rotation", rotation);
        screen_settings_json.put("size_x", size.x);
        screen_settings_json.put("size_y",  size.y);

        screen_settings_json.put("brightness", brightness);
        screen_settings_json.put("brightness_mode", brightness_mode);
        screen_settings_json.put("timeout", timeout);

        return screen_settings_json.toString();
    }

    /*
     * {"media_volume": xx, "max_media_volume": xxx, "ringer_volume": xx, "max_ringer_volume": xxx}
     */
    public String getVolumeInfo() throws JSONException {
        JSONObject volume_json = new JSONObject();

        volume_json.put("media_volume",
                audio_manager.getStreamVolume(AudioManager.STREAM_MUSIC));
        volume_json.put("max_media_volume",
                audio_manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        volume_json.put("ringer_volume",
                audio_manager.getStreamVolume(AudioManager.STREAM_RING));
        volume_json.put("max_ringer_volume",
                audio_manager.getStreamMaxVolume(AudioManager.STREAM_RING));

        return volume_json.toString();
    }
}
