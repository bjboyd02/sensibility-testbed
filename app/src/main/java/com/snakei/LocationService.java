package com.snakei;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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

import java.io.IOException;
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

    private Location location_gps;
    private Location location_network;
    private Location location_fused;

    private double[] location_values_gps;
    private double[] location_values_network;
    private double[] location_values_fused;

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
        location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this, Looper.getMainLooper());
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
        LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client, this);
        google_api_client.disconnect();
    }


    public double[] getLocationValuesGPS() {
        Log.i(TAG, "Polling gps locations");
        return location_values_gps;
    }
    public double[] getLocationValuesNetwork() {
        Log.i(TAG, "Polling network locations");
        return location_values_network;
    }
    public double[] getLocationValuesFused() {
        Log.i(TAG, "Polling google locations");
        return location_values_fused;
    }

    public double[] getLastKnownLocationValuesGPS() {
        Log.i(TAG, "Polling gps last known locations");
        Location location = location_manager.getLastKnownLocation("gps");
        return _convert_location(location);
    }
    public double[] getLastKnownLocationValuesNetwork() {
        Log.i(TAG, "Polling network last known locations");
        Location location = location_manager.getLastKnownLocation("network");
        return _convert_location(location);
    }
    public double[] getLastKnownLocationValuesFused() {
        Log.i(TAG, "Polling fused last known locations");
        if (google_api_client.isConnected()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(google_api_client);
            return _convert_location(location);
        } else {
            return null;
        }
    }

    /*
     * Which location should we use to get the address?
     * Probably the one with the best accuracy?
     * For now let's try it with fused
     *
     * Todo:
     *   Better failure handling
     *   Pass parameter for max(addresses)
     */
    public void getLocationAddress() {
        Log.i(TAG, "Try to get address from last known location location");
        List<Address> addresses = null;
        if (google_api_client.isConnected() &&
                location_fused != null && geocoder.isPresent()) {
            try {
                addresses = geocoder.getFromLocation(
                        location_fused.getLatitude(),
                        location_fused.getLongitude(),
                        1);
            } catch (IOException ioException) {
                Log.i(TAG, ioException.getMessage());
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.i(TAG, illegalArgumentException.getMessage());
            }
        } else {
            Log.i(TAG, "Did not perform reverse geocoding");
        }
        if (addresses != null && addresses.size() > 0) {
            for (Address address: addresses  ) {
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
                    Log.i(TAG, address.getAddressLine(i));
                }
            }
        } else {
            Log.i(TAG, "Did not get any addresses");
        }
    }

    /*
     * Helper method that converts a Locatoin object to a double array
     *
     * XXX
     * I don't like all the (double) casting, but maybe it does not matter
     * - In case of floats it needs additional memory
     * - In case of longs it loses precision
     *
     * Would storing all the actual values to some object and
     * calling them from C in a complicated way
     * (for each value at least three method calls) be doing
     * it the right way?
     */
    private double[] _convert_location(Location location) {
        if (location == null)
            return null;

        double[] result = new double[8];
        result[0] = (double) System.currentTimeMillis();
        result[1] = (double) location.getTime(); // long
        result[2] = (double) location.getAccuracy(); // float
        result[3] = location.getAltitude();
        result[4] = (double) location.getBearing(); //float
        // XXX Do we want this?
        // location.getElapsedRealtimeNanos();
        result[5] = location.getLatitude();
        result[6] = location.getLongitude();
        result[7] = (double) location.getSpeed(); // float

        // XXX Could contain # of gps satellite. Interested?
        // location.getExtras()
        return result;
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
        Log.i(TAG, "Received location");

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            location_values_gps = _convert_location(location);
            location_gps = location;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            location_values_network = _convert_location(location);
            location_network = location;
        } else if (location.getProvider().equals("fused")) {
            location_values_fused = _convert_location(location);
            location_fused = location;
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
