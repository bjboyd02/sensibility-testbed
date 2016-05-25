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

public class LocationService implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
    static final String TAG = "LocationService";

    // Used to start/stop listener on network and gps location provider
    private LocationManager location_manager;
    // Used to connect to Google Play Service
    private GoogleApiClient google_api_client;
    // Used to transform lat/long to addresses or vice-verca
    // needs Google Play Service
    private Geocoder geocoder;

    private Location location_gps;
    private Location location_network;
    private Location location_google;

    private double[] location_values_gps;
    private double[] location_values_network;
    private double[] location_values_google;

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

        //Connect to Google Play Service
        Log.i(TAG, "Connecting to Google Play Service");
        google_api_client = new GoogleApiClient.Builder(SensibilityApplication.getAppContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        google_api_client.connect();
    }

    /*
     * Unregister location update listeners
     * Disconnect from Google Play Service
     *
     * Todo: return meaningful code
     */
    public void stop_location() {
        location_manager.removeUpdates(this);
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
    public double[] getLocationValuesGoogle() {
        Log.i(TAG, "Polling google locations");
        return location_values_google;
    }
    /*
     * Which location should we use to get the address?
     * Probably the one with the best accuracy?
     * For now let's try it with google
     *
     * Todo:
     *   Better failure handling
     *   Pass parameter for max(addresses)
     */
    public void getLocationAddress() {
        Log.i(TAG, "Try to get address from last known location location");
        List<Address> addresses = null;
        if (google_api_client.isConnected() &&
                location_google != null && geocoder.isPresent()) {
            try {
                addresses = geocoder.getFromLocation(
                        location_google.getLatitude(),
                        location_google.getLongitude(),
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
     * Store location values on location change to values member
     * variable of according provider
     *
     * XXX I don't like all the (double) casting, but maybe it does not matter
     *      In case of floats it needs additional memory
     *      In case of longs it loses precision
     *
     * Would storing all the actual values to some object and
     * calling them from C in a complicated way
     * (for each value at least three method calls) be doing
     * it the right way?
     */

    private double[] _convert_location(Location location) {
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

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "get location");

        // Store the location the a float array
        // Addiationally store location as object
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            location_values_gps = _convert_location(location);
            location_gps = location;
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            location_values_network = _convert_location(location);
            location_network = location;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "connected to google");

        location_google = LocationServices
                .FusedLocationApi
                .getLastLocation(google_api_client);

        location_values_google = _convert_location(location_google);
        getLocationAddress();
//        double[] loc = location_values_google;
//        Log.i(TAG, String.format("time %f, time2 %f, accuarcy %f, alt %f, bearing %f, latitude %f, longitude %f, speed %f",
//                loc[0], loc[1], loc[2], loc[3], loc[4], loc[5], loc[6], loc[7]));
    }
    @Override
    public void onConnectionSuspended(int cause) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
            Log.i(TAG, String.format("Connection failed with code: %d. " +
              "Check com.google.android.gms.common.ConnectionResult Constants for details",
              result.getErrorCode()));
    }
}
