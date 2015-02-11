package fr.wakemybus.playground.geofencing;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import fr.wakemybus.MyApplication;

/**
 * Created by thibaultguegan on 11/02/15.
 */
public class GeofenceStore {

    private static final String SHARED_PREFERENCES = "SharedPreferences";
    private static final String KEY_GEOFENCE_STORE = "geofencestore";

    private static GeofenceStore uniqInstance;

    private ArrayList<SimpleGeofence> geofences;

    /**
     * MARK: Instances methods
     */

    public static synchronized GeofenceStore getInstance() {
        if(uniqInstance == null) {
            uniqInstance = geofenceStore();
        }
        return uniqInstance;
    }

    public GeofenceStore() {
        geofences = new ArrayList<>();
    }

    public static GeofenceStore geofenceStore() {
        GeofenceStore geofenceStore = null;

        SharedPreferences mPrefs = MyApplication.getInstance().getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(KEY_GEOFENCE_STORE, null);

        if (json != null) {
            geofenceStore = gson.fromJson(json, GeofenceStore.class);
        } else {
            geofenceStore = new GeofenceStore();
        }

        return geofenceStore;
    }

    /**
     * MARK: Archive methods
     */
     private void archive() {
         SharedPreferences mPrefs = MyApplication.getInstance().getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
         SharedPreferences.Editor prefsEditor = mPrefs.edit();
         Gson gson = new Gson();
         String json = gson.toJson(this);
         prefsEditor.putString(KEY_GEOFENCE_STORE, json);
         prefsEditor.commit();
     }

    /**
     * MARK: Public methods
     */

    public void addGeofence(SimpleGeofence simpleGeofence) {
        getGeofences().add(simpleGeofence);
        archive();
    }

    /**
     * MARK: getters/setters
     */

    public ArrayList<SimpleGeofence> getGeofences() {
        return geofences;
    }
}
