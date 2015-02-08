package fr.wakemybus.playground.geofencing.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import fr.wakemybus.playground.geofencing.SimpleGeofence;
import fr.wakemybus.playground.geofencing.SimpleGeofenceStore;

/**
 * Created by thibaultguegan on 08/02/15.
 */
public class LocationClientService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final String TAG = LocationClientService.class.getSimpleName();

    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    List<Geofence> mGeofenceList;

    // Persistent storage for geofences.
    private SimpleGeofenceStore mGeofenceStorage;

    private LocationServices mLocationService;
    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            //TODO: prevent the service from running geofences then
        }

        // Instantiate a new geofence storage area.
        mGeofenceStorage = new SimpleGeofenceStore(this);
        // Instantiate the current List of geofences.
        mGeofenceList = new ArrayList<Geofence>();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Get the PendingIntent for the geofence monitoring request.
        // Send a request to add the current geofences.
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        LocationServices.GeofencingApi.addGeofences(mApiClient, mGeofenceList,
                mGeofenceRequestIntent);
        Log.d(TAG, "Starting geofence service");
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If the error has a resolution, start a Google Play services activity to resolve it.
        //TODO: reference activity
        /*if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }*/
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createGeoFence(LatLng position) {

        SimpleGeofence geofence = new SimpleGeofence(
                "1",
                position.latitude,
                position.longitude,
                100.0f,
                -1,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        mGeofenceStorage.setGeofence("1", geofence);
        mGeofenceList.add(geofence.toGeofence());

        mApiClient = new GoogleApiClient.Builder(LocationClientService.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(LocationClientService.this)
                .addOnConnectionFailedListener(LocationClientService.this)
                .build();

        mApiClient.connect();

        /*new SnackBar.Builder(this)
                .withMessage(getString(R.string.geofence_started))
                .withActionMessage(getString(R.string.map_instructions_ok))
                .withStyle(SnackBar.Style.INFO)
                .withDuration(SnackBar.LONG_SNACK)
                .show();*/
    }

    /**
     * Checks if Google Play services is available.
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
