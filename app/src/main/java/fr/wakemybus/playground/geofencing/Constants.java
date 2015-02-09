package fr.wakemybus.playground.geofencing;

import android.net.Uri;

/**
 * Created by thibaultguegan on 02/02/15.
 */
public final class Constants {

    private Constants() {
    }

    public static final String TAG = "Geofencing";

    // Request code to attempt to resolve Google Play services connection failures.
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    public static final long CONNECTION_TIME_OUT_MS = 100;

    // The constants below are less interesting than those above.

    // Path for the DataItem containing the last geofence id entered.
    public static final String GEOFENCE_DATA_ITEM_PATH = "/geofenceid";
    public static final Uri GEOFENCE_DATA_ITEM_URI =
            new Uri.Builder().scheme("wear").path(GEOFENCE_DATA_ITEM_PATH).build();
    public static final String KEY_GEOFENCE_ID = "geofence_id";

    // Keys for flattened geofences stored in SharedPreferences.
    public static final String KEY_LATITUDE = "fr.wakemybus.wakemybus.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "fr.wakemybus.wakemybus.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "fr.wakemybus.wakemybus.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION =
            "fr.wakemybus.wakemybus.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE =
            "fr.wakemybus.wakemybus.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys.
    public static final String KEY_PREFIX = "fr.wakemybus.wakemybus.KEY";

    // Invalid values, used to test geofence storage when retrieving geofences.
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;

}
