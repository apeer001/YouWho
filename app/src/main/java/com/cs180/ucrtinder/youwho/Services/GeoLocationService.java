package com.cs180.ucrtinder.youwho.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.cs180.ucrtinder.youwho.Parse.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class GeoLocationService extends Service implements LocationListener {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    Location mLastLocation = null;

    Context mContext;
    Boolean mRequestingLocationUpdates = false;
    //LocationRequest mLocationRequest;
    LocationManager locationManager;
    LocationProvider locationProvider;

    public GeoLocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Log.d(getClass().getSimpleName(), "Started geolocation in OnStartCommand");

        // Get context
        mContext = getApplicationContext();

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationProvider = locationManager.getProvider(locationManager.GPS_PROVIDER);
        }

        if (locationManager != null) {
            startLocationUpdates();
        }

        return START_STICKY;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

        //Log.d(getClass().getSimpleName(), "onProviderEnabled called");
        mRequestingLocationUpdates = true;
        try {
            mLastLocation = locationManager.getLastKnownLocation(locationProvider.getName());
        } catch (SecurityException s) {
            s.printStackTrace();
        }
        if (mLastLocation != null) {

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }

            updateGeoLocationOnParse(mLastLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
       stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d(getClass().getSimpleName(), "location changed");
        mLastLocation = location;
        updateGeoLocationOnParse(mLastLocation);
    }

    protected void startLocationUpdates() {
        //Log.d(getClass().getSimpleName(), "Started location updates");
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1*20*1000, 0, this);
        } catch (SecurityException s) {
            //Log.d(getClass().getSimpleName(), "Must enable the location services permission by user");
            s.printStackTrace();
        }

    }

    protected void stopLocationUpdates() {
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        try {
            locationManager.removeUpdates(this);
        } catch(SecurityException s) {
            s.printStackTrace();
        }

    }

    void updateGeoLocationOnParse(final Location mLastLocation) {

        // Start parse put request
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            currentUser.put(ParseConstants.KEY_LOCATION, parseGeoPoint);

            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    //Log.d(getClass().getSimpleName(), "Done saving geopoint");
                }
            });

            //Log.d(getClass().getSimpleName(), mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            if (locationProvider != null) {
                stopLocationUpdates();
            }
            locationManager = null;
        }
        locationManager = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
