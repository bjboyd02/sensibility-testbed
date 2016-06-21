package com.snakei;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import android.content.pm.PackageManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.sensibility_testbed.SensibilityApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Created by lukas on 5/4/16.
 *
 * Pseudo Service that facades Android Location Services
 *
 * This class is a Singleton using the thread safe
 * Java Initialization on Demand Holder pattern
 * (cf. SensorService.java for explanation)
 * XXX: Maybe we can generalize some common aspects of
 * all the "Sensor" code (Real android sensors, location, cell, wifi,...)
 * Try now, DRY later!
 *
 * Python user wants:
 *      get_location() // This should always be the last known location
 *      get_geolocation() // Reverse geocoding
 *
 * We have to:
 *      Decide whether we use Android Location API (android.location) or
 *      Google Location Services API (Google Play Services)
 *          for reverse geocoding we need Google Play Services
 *      Decide which provider we use (GPS, Network, Passive)
 *      How we handle varying accuracy
 *      Starting/Stopping Location Services
 *      Setting the Interval
 *          Keep battery drain in mind
 *
 * For now:
 *      Use Android Location API for locations
 *      Use Google location Services for reverse geocoding
 *      Use both GPS and Network provider
 *      Provide polling methods for each provider, combine them in c
 *      (Return last known location if
 *          it exists AND a user calls get_location AND
 *          no location update has been received by our listener yet)
 *      (Provide listener starting and stopping to C code)
 *
 */

public class LocationService implements ConnectionCallbacks, OnConnectionFailedListener,
        android.location.LocationListener, com.google.android.gms.location.LocationListener {
    static final String TAG = "LocationService";

    // Used to start/stop listener on network and gps location provider
    private LocationManager location_manager;

    // Used to connect to Google Play Service
    private GoogleApiClient google_api_client;
    // Used to define accuracy and frequency of Google Play Services location updates
    private LocationRequest google_location_request;
    // Used to transform lat/long to addresses or vice-versa
    // needs Google Play Service
    private Geocoder geocoder;

    // Serialized location JSON object for each provider
    private String location_gps_jsons;
    private String location_network_jsons;
    private String location_fused_jsons;

    /* See Initialization on Demand Holder pattern */
    private static class LocationServiceHolder {
        private static final LocationService instance = new LocationService();
    }

    /* Classic Singleton Instance Getter */
    public static LocationService getInstance(){
        return LocationServiceHolder.instance;
    }

    private LocationService() {
        Context app_context = SensibilityApplication.getAppContext();
        location_manager = (LocationManager)app_context.getSystemService(app_context.LOCATION_SERVICE);

        // Set Google Play Service Qos Paramters to "real-time"
        google_location_request = new LocationRequest();
        google_location_request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        google_location_request.setInterval(5);

        geocoder = new Geocoder(app_context, Locale.getDefault());
    }
    /*
     * Register location update listeners
     *
     * Todo: return meaningful code
     */
    public void start_location() {
        // We could use one, both or PASSIVE_PROVIDER instead
        // There is no use in listening for PASSIVE_PROVIDER if one of the other two is registered
        // It only retrieves values if any other app is listening to a gps or network provider
        // If they can we can too, furthermore we need the same permissions for passive as for gps
        // and network.
        // Sensibility API currently returns values from all three providers
        Log.i(TAG, "Register GPS Location Update Listener...");

        Log.i(TAG, "Register Network Location Update Listener...");
        location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this, Looper.getMainLooper());

        // Create Google Play Service client and connect
        Log.i(TAG, "Connecting to Google Play Service");
        google_api_client = new GoogleApiClient.Builder(SensibilityApplication.getAppContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        google_api_client.connect();
    }

    /*
     * Unregister Android location update listeners
     * Unregister Google Play Service location update listeners
     * Disconnect from Google Play Service
     *
     * Todo: return meaningful code
     */
    public void stop_location() {

        location_manager.removeUpdates(this);

        // If in the process of connecting wait with disconnect until being connected
        if (google_api_client.isConnecting()) {
            google_api_client.blockingConnect();
            google_api_client.disconnect();
        }
        // Disconnect from Google Api Only if we are conncted
        if(google_api_client.isConnected()) {
            // If listener is not registered this has no effects
            LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client, this);
            google_api_client.disconnect();
        }
    }

    public String getLocationValuesGPS() {
        Log.i(TAG, "Polling gps locations");
        return location_gps_jsons;
    }
    public String getLocationValuesNetwork() {
        Log.i(TAG, "Polling network locations");
        return location_network_jsons;
    }
    public String getLocationValuesFused() {
        Log.i(TAG, "Polling google locations");
        return location_fused_jsons;
    }

    public String getLastKnownLocationValuesGPS() throws JSONException {
        Log.i(TAG, "Polling gps last known locations");
        Location location = location_manager.getLastKnownLocation("gps");
        return jsonifys_location(location);
    }
    public String getLastKnownLocationValuesNetwork() throws JSONException {
        Log.i(TAG, "Polling network last known locations");
        Location location = location_manager.getLastKnownLocation("network");
        return jsonifys_location(location);
    }
    public String getLastKnownLocationValuesFused() throws JSONException {
        Log.i(TAG, "Polling fused last known locations");
        if (google_api_client.isConnected()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(google_api_client);
            return jsonifys_location(location);
        }
        return null;
    }

    /*

     * Todo:
     *   Better failure handling
     */
    public String getGeoLocation(double latitude, double longitude, int max_results) throws
            IOException, IllegalArgumentException, JSONException {
        Log.i(TAG, String.format("Get address(es) for location -- lat: %f, lon: %f, max: %d",
                latitude, longitude, max_results));

        List<Address> addresses = null;
        if (google_api_client.isConnected() &&
                geocoder.isPresent()) {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    max_results);
        } else {
            Log.i(TAG, "Did not perform reverse geocoding");
        }

        if (addresses != null) {
            JSONArray addresses_json = new JSONArray();
            for (Address address: addresses  ) {
                JSONObject address_json = new JSONObject();
                address_json.put("admin_area", address.getAdminArea());
                address_json.put("country_code", address.getCountryCode());
                address_json.put("country_name", address.getCountryName());
                address_json.put("feature_name", address.getFeatureName());
                address_json.put("locality", address.getLocality());
                address_json.put("phone", address.getPhone());
                address_json.put("postal_code", address.getPostalCode());
                address_json.put("premises", address.getPremises());
                address_json.put("sub_admin_area", address.getSubAdminArea());
                address_json.put("sub_locality", address.getSubLocality());
                address_json.put("sub_thoroughfare", address.getSubThoroughfare());
                address_json.put("thoroughfare", address.getThoroughfare());
                address_json.put("url", address.getUrl());
                int address_line_cnt = address.getMaxAddressLineIndex();
                if (address_line_cnt > 0) {
                    JSONArray address_lines_json = new JSONArray();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
                        address_lines_json.put(address.getAddressLine(i));
                    }
                    address_json.put("lines", address_lines_json);
                }
                addresses_json.put(address_json);
            }
            return addresses_json.toString();
        }

        Log.i(TAG, "Did not get any addresses");
        return null;
    }

//    /*
//     * Helper method that converts a Locatoin object to a double array
//     *
//     * XXX
//     * I don't like all the (double) casting, but maybe it does not matter
//     * - In case of floats it needs additional memory
//     * - In case of longs it loses precision
//     *
//     * Would storing all the actual values to some object and
//     * calling them from C in a complicated way
//     * (for each value at least three method calls) be doing
//     * it the right way?
//     */
//    private double[] _convert_location(Location location) {
//        if (location == null)
//            return null;
//
//        double[] result = new double[8];
//        result[0] = (double) System.currentTimeMillis();
//        result[1] = (double) location.getTime(); // long
//        result[2] = (double) location.getAccuracy(); // float
//        result[3] = location.getAltitude();
//        result[4] = (double) location.getBearing(); //float
//        // XXX Do we want this?
//        // location.getElapsedRealtimeNanos();
//        result[5] = location.getLatitude();
//        result[6] = location.getLongitude();
//        result[7] = (double) location.getSpeed(); // float
//
//        // XXX Could contain # of gps satellite. Interested?
//        // location.getExtras()
//        return result;
//    }


    private JSONObject jsonify_location(Location location) throws JSONException {
        JSONObject location_json = new JSONObject();

        location_json.put("time_polled", System.currentTimeMillis());
        location_json.put("time_sample", location.getTime());
        location_json.put("accuracy", location.getAccuracy());
        location_json.put("altitude", location.getAltitude());
        location_json.put("bearing", location.getBearing());
        location_json.put("latitude", location.getLatitude());
        location_json.put("longitude", location.getLongitude());
        location_json.put("speed", location.getSpeed());
        Bundle extras = location.getExtras();

        // Provider specific extra information
        if (extras != null) {
            JSONObject extras_json = new JSONObject();
            for (String key : extras.keySet()) {
                // Use wrap to also stringify in case the value is an unexpected Object
                extras_json.put(key, JSONObject.wrap(extras.get(key)));
            }
            if (extras_json.length() > 0) {
                location_json.put("extras", extras_json);
            }
        }
        return location_json;
    }

    private String jsonifys_location(Location location) throws JSONException {
        return jsonify_location(location).toString();
    }

    /*
     * ###################################################
     * Required LocationListener implementations
     * (Android AND Google Play Service)
     * ###################################################
     */

    /*
     * Callback: Receives gps, network and fused locations
     *
     * Stores location object and location double values to the according
     * class members
     *
     * CAUTION !!!!!!!!!
     * This method implements `onLocationChanged` of two different
     * interfaces:
     *     - android.location.LocationListener
     *     - com.google.android.gms.location.LocationListener
     * Both callbacks receive an Android Location Object but
     * from different Location Providers
     *
     * Android's Interface expects Locations of provider type:
     *      - LocationManager.GPS_PROVIDER
     *      - LocationManager.NETWORK_PROVIDER
     * Google Play Services's Interface expects Locations of provider type:
     *      "fused"
     *
     * XXX
     * It is not safe to expect that the string "fused" won't change
     *
     */
    @Override
    public void onLocationChanged(Location location) {
        String location_jsons = null;
        try {
            location_jsons = jsonify_location(location);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "Received location GPS");
            location_gps_jsons = location_jsons;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            Log.i(TAG, "Received location Network");
            location_network_jsons = location_jsons;
        } else if (location.getProvider().equals("fused")) {
            Log.i(TAG, "Received location Fused");
            location_fused_jsons = location_jsons;
        } else {
            Log.i(TAG, String.format("Received location from unknown Provider: %s", location.getProvider()));
        }
    }

    /*
     * ###################################################
     * Required LocationListener implementations (android)
     * ###################################################
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }

    /*
     * ###################################################
     * Required ConnectionCallback implementations (Google Play Services)
     * ###################################################
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to google");
        LocationServices.FusedLocationApi
                .requestLocationUpdates(google_api_client, google_location_request, this, Looper.getMainLooper());
    }
    @Override
    public void onConnectionSuspended(int cause) {
    }

    /*
     * ###################################################
     * Required OnConnectionFailedListener implementation (Google Play Services)
     * ###################################################
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
            Log.i(TAG, String.format("Connection failed with code: %d. " +
              "Check com.google.android.gms.common.ConnectionResult Constants for details",
              result.getErrorCode()));
    }
}
