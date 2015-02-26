package fr.wakemybus.playground.geofencing;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.Geofence;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.wakemybus.playground.util.BusProvider;
import fr.wakemybus.playground.util.GeofencesReceivedEvent;
import fr.wakemybus.wakemybus.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeofencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeofencesFragment extends Fragment {

    private static final String LOG_TAG = GeofencesFragment.class.getSimpleName();

    private ListView mListView;

    /**
     * MARK: Instance
     */

    public static GeofencesFragment newInstance() {
        GeofencesFragment fragment = new GeofencesFragment();
        return fragment;
    }

    public GeofencesFragment() {
        // Required empty public constructor
    }

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_geofences, container, false);
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mListView = (ListView) v.findViewById(android.R.id.list);
    }

    /**
     * MARK: Bus event
     */

    @Subscribe
    public void onGeofences(GeofencesReceivedEvent event) {
        ArrayList<SimpleGeofence> geofences = event.getGeofences();

        Log.d(LOG_TAG, String.format("received array size: %d", geofences.size()));

        GeofencesAdapter adapter = new GeofencesAdapter(geofences);
        mListView.setAdapter(adapter);
    }

    /**
     * MARK: List adapter
     */

    public class GeofencesAdapter extends BaseAdapter {

        private ArrayList<SimpleGeofence> geofences;

        public GeofencesAdapter(ArrayList<SimpleGeofence> geofences) {
            this.geofences = geofences;
        }

        @Override
        public int getCount() {
            return geofences.size();
        }

        @Override
        public SimpleGeofence getItem(int position) {
            return geofences.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //TODO: update toggleButton if the geofence is activated or not

            final SimpleGeofence simpleGeofence = getItem(position);
            ViewHolder holder;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder = new ViewHolder();

                holder.txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
                holder.txtLatLon = (TextView) convertView.findViewById(R.id.txtLatLong);
                holder.txtExpiration = (TextView) convertView.findViewById(R.id.txtExpiration);
                holder.txtRadius = (TextView) convertView.findViewById(R.id.txtRadius);
                holder.toggleButton = (ToggleButton) convertView.findViewById(R.id.toggle);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.txtLatLon.setText(String.format("lat: %f, long: %f", simpleGeofence.getLatitude(), simpleGeofence.getLongitude()));
            holder.txtRadius.setText(String.format("radius: %sm", String.valueOf(simpleGeofence.getRadius())));

            String expiration = "Expiration mode: %s";
            if(simpleGeofence.getExpirationDuration() == Geofence.NEVER_EXPIRE) {
                holder.txtExpiration.setText(String.format(expiration, "NEVER_EXPIRE"));
            } else if(simpleGeofence.getExpirationDuration() == Geofence.GEOFENCE_TRANSITION_DWELL) {
                holder.txtExpiration.setText(String.format(expiration, "GEOFENCE_TRANSITION_DWELL"));
            } else if(simpleGeofence.getExpirationDuration() == Geofence.GEOFENCE_TRANSITION_ENTER) {
                holder.txtExpiration.setText(String.format(expiration, "GEOFENCE_TRANSITION_ENTER"));
            } else if(simpleGeofence.getExpirationDuration() == Geofence.GEOFENCE_TRANSITION_EXIT) {
                holder.txtExpiration.setText(String.format(expiration, "GEOFENCE_TRANSITION_EXIT"));
            }

            Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(simpleGeofence.getLatitude(), simpleGeofence.getLongitude(), 1);

                if(addresses.size() > 0) {
                    holder.txtAddress.setText(String.format("Address: %s", addresses.get(0).getAddressLine(0)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            holder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        GeofencingActivity.getInstance().activateGeoFence(simpleGeofence);
                    } else {
                        GeofencingActivity.getInstance().deactivateGeoFence(simpleGeofence);
                    }
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView txtAddress;
            TextView txtLatLon;
            TextView txtExpiration;
            TextView txtRadius;
            ToggleButton toggleButton;
        }
    }
}
