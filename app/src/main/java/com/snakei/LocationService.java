package com.snakei;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.sensibility_testbed.SensibilityApplication;

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

public class LocationService implements LocationListener {
    static final String TAG = "LocationService";
    private LocationManager location_manager;
    private double[] location_values_gps;
    private double[] location_values_network;

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
    }

    /*
     * Unregister location update listeners
     *
     * Todo: return meaningful code
     */
    public void stop_location() {
        location_manager.removeUpdates(this);
    }

    public double[] getLocationValuesGPS() {
        Log.i(TAG, "Polling gps");
        return location_values_gps;
    }
    public double[] getLocationValuesNetwork() {
        Log.i(TAG, "Polling network");
        return location_values_network;
    }

    /*
     * Store location values on location change to values member
     * variable of according provider
     *
     * XXX I don't like all the (double) casting, but maybe it does not matter
     *      In case of foats it needs additional memory
     *      In case of longs it loses precision
     *
     * Would storing all the actual values to some object and
     * calling them from C in a complicated way
     * (for each value at least three method calls) be doing
     * it the right way?
     */

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "get location");

        Log.i(TAG, String.format("time %i, accuarcy %f, alt %f, bearing %f, latitude %f, longitude %f, speed %f", location.getTime(), location.getAccuracy(), location.getAltitude(), location.getBearing(), location.getLatitude(), location.getLongitude(), location.getSpeed()));

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

        // XXX Could contain # of gps satellite. Interested?
        //location.getExtras()

//        if (location.getProvider() == LocationManager.GPS_PROVIDER) {
//            location_values_gps = result;
//        } else if (location.getProvider() == LocationManager.NETWORK_PROVIDER) {
//            location_values_network = result;
//        }
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
}
