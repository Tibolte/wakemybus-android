package fr.wakemybus.playground.geofencing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;

import fr.wakemybus.playground.geofencing.places.PlaceProvider;
import fr.wakemybus.playground.geofencing.services.LocationClientService;
import fr.wakemybus.playground.geofencing.services.ServiceManager;
import fr.wakemybus.playground.util.BusProvider;
import fr.wakemybus.playground.util.GPSTracker;
import fr.wakemybus.playground.util.GeofencesReceivedEvent;
import fr.wakemybus.wakemybus.R;

public class GeofencingActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ActionBar.TabListener, fr.wakemybus.playground.geofencing.MapFragment.MapCallback {

    private static final String TAG = GeofencingActivity.class.getSimpleName();

    public static final int START_SERVICE = 201;

    private GoogleMap mMap;
    private ServiceManager mServiceManager;
    private static GeofencingActivity mInstance;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * MARK: Activity overrides
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInstance = this;

        setContentView(R.layout.activity_geofencing);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        mServiceHandler.sendEmptyMessage(START_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_geofencing, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_show_geofences) {
            if (mServiceManager != null) {
                mServiceManager.sendServiceMessage(LocationClientService.SHOW_GEOFENCES);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        try {
            mServiceManager.unbind();
        } catch (Throwable t) {

        }
        super.onDestroy();
    }

    /**
     * MARK: Tabbar Overrides
     */

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    /**
     * MARK: MapFragment listener
     */

    @Override
    public void onMapCreateGeoFence(SimpleGeofence geofence) {
        Bundle b = new Bundle();
        b.putParcelable(LocationClientService.BUNDLE_GEOFENCE, geofence);
        if (mServiceManager != null) {
            mServiceManager.sendServiceMessage(LocationClientService.ADD_GEOFENCE, b);
        }
    }

    /**
     * MARK: Tabbar adapter
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fr.wakemybus.playground.geofencing.MapFragment.newInstance();
                case 1:
                    return GeofencesFragment.newInstance();
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.map_fragment).toUpperCase(l);
                case 1:
                    return getString(R.string.geofences_fragment).toUpperCase(l);
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    /**
     * MARK: Service handler
     */

    @SuppressLint("HandlerLeak")
    private Handler mServiceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            switch (msg.what) {
                case START_SERVICE: {
                    mServiceManager = new ServiceManager(GeofencingActivity.this, LocationClientService.class, this);
                    mServiceManager.start();
                    break;
                }
                case LocationClientService.GEOFENCE_CREATED: {
                    if(b != null) {
                        b.setClassLoader(SimpleGeofence.class.getClassLoader());
                        if (b.containsKey(LocationClientService.BUNDLE_GEOFENCE)) {
                            SimpleGeofence simpleGeofence = b.getParcelable(LocationClientService.BUNDLE_GEOFENCE);
                            showSnackBarGeofenceAdded();
                        }
                    }
                    break;
                }
                case LocationClientService.SHOW_GEOFENCES: {
                    if(b != null) {
                        b.setClassLoader(SimpleGeofence.class.getClassLoader());
                        if (b.containsKey(LocationClientService.BUNDLE_GEOFENCES)) {
                            ArrayList<SimpleGeofence> geofences = b.getParcelableArrayList(LocationClientService.BUNDLE_GEOFENCES);
                            Log.d(TAG, String.format("received array size: %d", geofences.size()));
                            BusProvider.getUIBusInstance().post(new GeofencesReceivedEvent(geofences));
                        }
                    }
                }
                break;
            }
        }
    };

    /**
     * MARK: Loader Overrides
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cLoader = null;
        if(id==0)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.SEARCH_URI, null, null, new String[]{ args.getString("query") }, null);
        else if(id==1)
            cLoader = new CursorLoader(getBaseContext(), PlaceProvider.DETAILS_URI, null, null, new String[]{ args.getString("query") }, null);
        return cLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        showLocations(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * MARK: Search methods
     */

    private void handleIntent(Intent intent){
        if(intent.getAction() != null) {
            if(intent.getAction().equals(Intent.ACTION_SEARCH)){
                doSearch(intent.getStringExtra(SearchManager.QUERY));
            }else if(intent.getAction().equals(Intent.ACTION_VIEW)){
                getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
            }
        }
    }

    private void doSearch(String query){
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(0, data, this);
    }

    private void getPlace(String query){
        Bundle data = new Bundle();
        data.putString("query", query);
        getSupportLoaderManager().restartLoader(1, data, this);
    }

    /**
     * MARK: Public Methods
     */

    public static GeofencingActivity getInstance() {
        return mInstance;
    }

    /**
     * MARK: Private Methods
     */

    private void showLocations(Cursor c){
        MarkerOptions markerOptions = null;
        LatLng position = null;

        fr.wakemybus.playground.geofencing.MapFragment mapFragment = (fr.wakemybus.playground.geofencing.MapFragment) mSectionsPagerAdapter.getRegisteredFragment(0);
        GoogleMap map = mapFragment.getMap();

        map.clear();
        while(c.moveToNext()){
            markerOptions = new MarkerOptions();
            position = new LatLng(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)));
            markerOptions.position(position);
            markerOptions.title(c.getString(0));
            map.addMarker(markerOptions);
        }
        if(position!=null){
            CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(position);
            final LatLng finalPosition = position;
            map.animateCamera(cameraPosition, new GoogleMap.CancelableCallback()
            {

                @Override
                public void onFinish()
                {
                    new AlertDialog.Builder(GeofencingActivity.this)
                            .setTitle(getString(R.string.alert_geofence_title))
                            .setMessage(getString(R.string.alert_geofence_description))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SimpleGeofence geofence = new SimpleGeofence(
                                            String.valueOf(finalPosition.latitude),
                                            finalPosition.latitude,
                                            finalPosition.longitude,
                                            100.0f,
                                            Geofence.NEVER_EXPIRE,
                                            Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
                                    );
                                    Bundle b = new Bundle();
                                    b.putParcelable(LocationClientService.BUNDLE_GEOFENCE, geofence);
                                    if (mServiceManager != null) {
                                        mServiceManager.sendServiceMessage(LocationClientService.ADD_GEOFENCE, b);
                                    }
                                }

                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();
                }

                @Override
                public void onCancel()
                {


                }

            });

        }
    }

    private void showSnackBarGeofenceAdded() {
        new SnackBar.Builder(GeofencingActivity.this)
                .withMessage(getString(R.string.geofence_started))
                .withActionMessage(getString(R.string.map_instructions_ok))
                .withStyle(SnackBar.Style.INFO)
                .withDuration(SnackBar.LONG_SNACK)
                .show();
    }
}
