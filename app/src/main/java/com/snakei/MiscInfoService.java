package com.snakei;

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
 *      do_wifi_scan should be changed to getWifiInfo
 *      Exceptions should be raised
 *      How do we handle passing large dicts with values of different types
 */
public class MiscInfoService {
    static final String TAG = "NetworkInfoService";

    /* See Initialization on Demand Holder pattern */
    private static class MiscInfoServiceHolder {
        private static final MiscInfoService instance = new MiscInfoService();
    }

    /* Classic Singleton Instance Getter */
    public static MiscInfoService getInstance(){
        return MiscInfoServiceHolder.instance;
    }


    /*
     * ###################################################
     * Cellular
     * ###################################################
     */
    public boolean isRoaming() {
    }

    /*
     * e.g. {‘network_operator’: 310260, ‘network_operator_name’: ‘T-Mobile’}.
     */
    public ?? getCellularProviderInfo() {

    }

    /*
     * e.g. {‘cellID’: {‘lac’: 32115, ‘cid’: 26742}, ‘neighboring_cell’: [{‘rssi’: 11, ‘cid’: 26741},
     * {‘rssi’: 9, ‘cid’: 40151}, {‘rssi’: 5, ‘cid’: 40153}]}.
     */
    public ?? getCellInfo() {


    }

    /*
     * e.g. {‘SIM_operator’: 310260, ‘SIM_operator_name’: ‘’, ‘SIM_country_code’: ‘us’, ‘SIM_state’: ‘ready’}
     */
    public ?? getSimInfo() {

    }

    /*
     * e.g. {‘phone_state’: {‘incomingNumber’: ‘’, ‘state’: ‘idle’},
     * ‘phone_type’: ‘gsm’, ‘network_type’: 'edge'}. When no SIM card is available,
     * the phone info dict would be, e.g., {‘phone_state’: {}, ‘phone_type’: ‘gsm’, ‘network_type’: 'unknown'}
     */
    public ?? getPhoneInfo() {

    }

    /*
     * e.g. {"gsm_signal_strength": 8, "evdo_ecio": -1, "gsm_bit_error_rate": -1, "cdma_ecio": -1, "cdma_dbm": -1, "evdo_dbm": -1}
     */
    public ?? getCellularSignalStrengths() {

    }

    /*
     * ###################################################
     * WiFi
     * ###################################################
     */

    public boolean isWifiEnabled() {

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
    public ?? getWifiConnectionInfo() {

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
    public ?? doWifiScan() {

    }

    /*
     * ###################################################
     * Bluetooth
     * ###################################################
     */


    /*
     * {'state': True, 'scan_mode': 3, 'local_name': 'GT-P1000'}
     */
    public ?? getBluetoothInfo() {

    }

    /*
     * ###################################################
     * Battery
     * ###################################################
     */

    /* 
     * {'status': 3, 'temperature': 257, 'level': 99, 'battery_present': True, 'plugged': 2, 'health': 2, 'voltage': 4186, 'technology': 'Li-ion'}
     */
    public ?? getBatteryInfo() {

    }
    /*
     * ###################################################
     * Settings
     * ###################################################
     */


    /*
     * {"airplane_mode": False, "ringer_silent_mode": True, "vibrate_mode": {'ringer_vibrate': True, 'notification_vibrate': False}}
     */
    public ?? getModeSettings() {

    }

    /*
     *  {"screen_on": True, "screen_brightness": 200, "screen_timeout": 60}. 
     */
    public ?? getScreenSettings() {

    }

    /*
     * {"media_volume": xx, "max_media_volume": xxx}
     */
    public ?? getMediaVolume() {

    }

    /*
     * {"ringer_volume": xx, "max_ringer_volume": xxx}
     */
    public ?? getRingerVolume() {

    }

    public Object getAnyObject() {
        return "434";
    }
}
