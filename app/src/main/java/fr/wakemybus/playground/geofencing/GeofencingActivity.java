package fr.wakemybus.playground.geofencing;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.wakemybus.wakemybus.R;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;

public class GeofencingActivity extends ActionBarActivity {

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
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_geofencing, menu);
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
}
