package fr.wakemybus.playground.geofencing;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import fr.wakemybus.playground.util.BusProvider;
import fr.wakemybus.playground.util.GeofencesReceivedEvent;
import fr.wakemybus.wakemybus.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeofencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeofencesFragment extends Fragment {

    private static final String TAG = GeofencesFragment.class.getSimpleName();

    private ListView mListView;

    /**
     * MARK: Instance
     */

    public static GeofencesFragment newInstance(String param1, String param2) {
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

        Log.d(TAG, String.format("received array size: %d", geofences.size()));

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

            SimpleGeofence simpleGeofence = getItem(position);
            ViewHolder holder;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                holder = new ViewHolder();

                holder.txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
                holder.txtLatLon = (TextView) convertView.findViewById(R.id.txtLatLong);
                holder.toggleButton = (ToggleButton) convertView.findViewById(R.id.toggle);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.txtLatLon.setText(String.format("lat: %f, long: %f", simpleGeofence.getLatitude(), simpleGeofence.getLongitude()));

            return convertView;
        }

        private class ViewHolder {
            TextView txtAddress;
            TextView txtLatLon;
            ToggleButton toggleButton;
        }
    }
}
