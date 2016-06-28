package com.snakei;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

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
import java.util.List;
import java.util.Locale;


/**
 * Created by lukas.puehringer@nyu.edu
 * on 5/4/16.
 *
 * A pseudo Service class that facades Android location services
 *
 * Provides methods to start and stop location services, poll updated
 * location information for different providers, poll last known location
 * information for different providers and perform reverse geocoding
 *
 * Currently provides three location providers
 * GPS and Network (Android Location API) and Fused (Google Play Services)
 *
 * Revers geocoding uses Google Play Services and requires an internet connection
 *
 * Location and address objects are returned as serialized JSON
 *
 * This class is a Singleton using the thread safe
 * Java Initialization on Demand Holder pattern
 * (cf. SensorService.java for more info )
 *
 * Todo:
 *   Maybe we can generalize some common aspects of all the facades
 *     (Real sensors, location, media, miscinfo,...)
 *   Decide how to handle resource allocation and release in multi-threading environments
 *     Keep battery drain in mind
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
    // Used to transform lat/long to addresses or vice-versa (requires Google Play Service)
    private Geocoder geocoder;

    // Location JSON object for each provider
    private JSONObject location_gps_json;
    private JSONObject location_network_json;
    private JSONObject location_fused_json;


    /* See Initialization on Demand Holder pattern */
    private static class LocationServiceHolder {
        private static final LocationService instance = new LocationService();
    }


    /* Singleton Instance Getter */
    public static LocationService getInstance(){
        return LocationServiceHolder.instance;
    }


    /*
     * Singleton Constructor
     *
     * Fetches context from static application function
     * Initializes Android location manager and Google Play Service objects
     *
     */
    private LocationService() {
        Context app_context = SensibilityApplication.getAppContext();
        location_manager = (LocationManager)app_context.getSystemService(app_context.LOCATION_SERVICE);

        // Set Google Play Service QoS parameters to "real-time"
        google_location_request = new LocationRequest();
        google_location_request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        google_location_request.setInterval(5);

        geocoder = new Geocoder(app_context, Locale.getDefault());
    }
    /*
     * Registers location update listeners for GPS and network provider and connects to
     * Google API client
     *
     * The connection callback function is implemented
     * by the LocationService class (see below). Upon connection it will also register a location
     * update listener.
     *
     */

    public void start_location() {
        // There is no use in listening for PASSIVE_PROVIDER if one of the other two is registered
        // It only retrieves values if any other app is listening to a gps or network provider
        // If they can we can too, furthermore we need the same permissions for passive as for gps
        // and network.
        // Current Sensibility API returns values from all three providers
        location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
            this, Looper.getMainLooper());
        location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
            this, Looper.getMainLooper());

        // Create Google Play Service client and connect
        google_api_client = new GoogleApiClient.Builder(SensibilityApplication.getAppContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        google_api_client.connect();
    }

    /*
     * Unregisters Android location update listeners
     * Unregisters Google Play Service location update listeners
     * Disconnects from Google Play Service
     *
     */
    public void stop_location() {

        location_manager.removeUpdates(this);

        // If in the process of connecting wait with disconnect until being connected
        if (google_api_client.isConnecting()) {
            google_api_client.blockingConnect();
            google_api_client.disconnect();
        }

        // Disconnect from Google Api Only if we are connected
        if(google_api_client.isConnected()) {
            // If listener is not registered this has no effects
            LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client, this);
            google_api_client.disconnect();
        }
    }

  /*
   * Returns location information for each available provider
   *
   * @return  String serialized JSON Object or null
   * e.g.:
   *
   */
    public String getLocation() throws JSONException {
        JSONObject locations_json = new JSONObject();
        if (location_gps_json != null) {
            locations_json.put("gps", location_gps_json);
        }
        if (location_network_json != null) {
            locations_json.put("network", location_network_json);
        }
        if (location_fused_json != null) {
            locations_json.put("fused", location_fused_json);
        }

        if (locations_json.length() > 0) {
            return locations_json.toString();
        }
        return null;
    }

    public String getLastKnownLocation() throws JSONException {

        JSONObject locations_json = new JSONObject();
        Location location_gps = null;
        Location location_network = null;
        Location location_fused = null;

        location_gps = location_manager.getLastKnownLocation("gps");
        if (location_gps != null) {
            locations_json.put("gps", jsonify_location(location_gps));
        }

        location_network = location_manager.getLastKnownLocation("network");
        if (location_network != null) {
            locations_json.put("network", jsonify_location(location_network));
        }

        if (google_api_client.isConnected()) {
            location_fused = LocationServices.FusedLocationApi.getLastLocation(google_api_client);
            if (location_fused != null) {
                locations_json.put("fused", jsonify_location(location_fused));
            }
        }

        if (locations_json.length() > 0) {
            return locations_json.toString();
        }
        return null;
    }

    /*

     * Todo:
     *   Better failure handling
     */
    public String getGeoLocation(double latitude, double longitude, int max_results) throws
            IOException, IllegalArgumentException, JSONException {
//        Log.i(TAG, String.format("Get address(es) for location -- lat: %f, lon: %f, max: %d",
//                latitude, longitude, max_results));

        List<Address> addresses = null;
        if (google_api_client.isConnected() &&
                geocoder.isPresent()) {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    max_results);
        }
//        else {
//            Log.i(TAG, "Did not perform reverse geocoding");
//        }

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

//        Log.i(TAG, "Did not get any addresses");
        return null;
    }

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
        JSONObject location_json = null;
        try {
            location_json = jsonify_location(location);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
//            Log.i(TAG, "Received location GPS");
            location_gps_json = location_json;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
//            Log.i(TAG, "Received location Network");
            location_network_json = location_json;
        } else if (location.getProvider().equals("fused")) {
//            Log.i(TAG, "Received location Fused");
            location_fused_json = location_json;
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
//        Log.i(TAG, "Connected to google");
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
