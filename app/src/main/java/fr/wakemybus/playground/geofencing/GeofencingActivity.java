package fr.wakemybus.playground.geofencing;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.wakemybus.wakemybus.R;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;

public class GeofencingActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofencing);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());
        locationProvider.getLastKnownLocation()
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        if (map!=null){
                            if(location != null) {
                                double curLat = location.getLatitude();
                                double curLon = location.getLongitude();
                                LatLng currentPos = new LatLng(curLat, curLon);
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curLat, curLon), 12));

                                final Marker marker= map.addMarker(new MarkerOptions().position(currentPos)
                                        .title("Draggable Marker")
                                        .snippet("Long press and move the marker if needed.")
                                        .draggable(true));
                                map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                                    @Override
                                    public void onMarkerDrag(Marker arg0) {
                                        // TODO Auto-generated method stub
                                        Log.d("Marker", "Dragging");
                                    }

                                    @Override
                                    public void onMarkerDragEnd(Marker arg0) {
                                        // TODO Auto-generated method stub
                                        LatLng markerLocation = marker.getPosition();
                                        Toast.makeText(GeofencingActivity.this, markerLocation.toString(), Toast.LENGTH_LONG).show();
                                        Log.d("Marker", "finished");
                                    }

                                    @Override
                                    public void onMarkerDragStart(Marker arg0) {
                                        // TODO Auto-generated method stub
                                        Log.d("Marker", "Started");

                                    }
                                });
                            }
                        }
                    }
                });

    }

    private void handleIntent(Intent intent){
        if(intent.getAction().equals(Intent.ACTION_SEARCH)){
            doSearch(intent.getStringExtra(SearchManager.QUERY));
        }else if(intent.getAction().equals(Intent.ACTION_VIEW)){
            getPlace(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
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
        }

        return super.onOptionsItemSelected(item);
    }

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

    private void showLocations(Cursor c){
        MarkerOptions markerOptions = null;
        LatLng position = null;
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
            map.animateCamera(cameraPosition);
            Toast.makeText(GeofencingActivity.this, markerOptions.getPosition().toString(), Toast.LENGTH_LONG).show();
        }
    }
}
