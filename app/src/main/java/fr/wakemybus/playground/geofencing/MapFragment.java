package fr.wakemybus.playground.geofencing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import fr.wakemybus.playground.util.BusProvider;
import fr.wakemybus.playground.util.GPSTracker;
import fr.wakemybus.playground.util.GeofencesReceivedEvent;
import fr.wakemybus.wakemybus.R;

public class MapFragment extends Fragment {

    private static final String TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private MapCallback mCallback;

    /**
     * MARK: Interfaces
     */
    public interface MapCallback {
        public void onMapCreateGeoFence(SimpleGeofence geofence);
    }

    /**
     * MARK: Instance
     */

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    public MapFragment() {}

    /**
     * MARK: Fragment lifecycle
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        BusProvider.getUIBusInstance().register(this);

    }

    @Override
    public void onPause(){
        super.onPause();
        BusProvider.getUIBusInstance().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mapView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapFragment = new SupportMapFragment() {
            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                mMap = mMapFragment.getMap();
                if (mMap != null) {
                    GPSTracker gps = new GPSTracker(getActivity());
                    if(gps.canGetLocation()){
                        if (mMap!=null){
                            double curLat = gps.getLatitude();
                            double curLon = gps.getLongitude();
                            LatLng currentPos = new LatLng(curLat, curLon);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curLat, curLon), 12));

                            final Marker marker= mMap.addMarker(new MarkerOptions().position(currentPos)
                                    .title("Draggable Marker")
                                    .snippet("Long press and move the marker if needed.")
                                    .draggable(true));
                            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                                @Override
                                public void onMarkerDrag(Marker arg0) {
                                    Log.d(TAG, "Marker Dragging");
                                }

                                @Override
                                public void onMarkerDragEnd(Marker arg0) {
                                    LatLng markerLocation = marker.getPosition();

                                    final SimpleGeofence geofence = new SimpleGeofence(
                                            String.valueOf(markerLocation.latitude),
                                            markerLocation.latitude,
                                            markerLocation.longitude,
                                            100.0f,
                                            -1,
                                            Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
                                    );


                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getString(R.string.alert_geofence_title))
                                            .setMessage(getString(R.string.alert_geofence_description))
                                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (mCallback != null) {
                                                        mCallback.onMapCreateGeoFence(geofence);
                                                    }
                                                }

                                            })
                                            .setNegativeButton(getString(R.string.no), null)
                                            .show();

                                    Log.d(TAG, "Marker finished");
                                }

                                @Override
                                public void onMarkerDragStart(Marker arg0) {
                                    Log.d(TAG, "Marker Started");
                                }
                            });

                            new SnackBar.Builder(getActivity())
                                    .withMessage(getString(R.string.map_instructions))
                                    .withActionMessage(getString(R.string.map_instructions_ok))
                                    .withStyle(SnackBar.Style.CONFIRM)
                                    .withDuration(SnackBar.LONG_SNACK)
                                    .show();
                        }
                    }
                }
            }
        };

        getChildFragmentManager().beginTransaction().add(R.id.map, mMapFragment).commit();

        return mapView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (MapCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MapFragment.MapCallback");
        }
    }

    /**
     * MARK: Getters/setters
     */

    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * MARK: Bus event
     */
    @Subscribe
    public void onGeofences(GeofencesReceivedEvent event) {
        ArrayList<SimpleGeofence> geofences = event.getGeofences();
    }
}
