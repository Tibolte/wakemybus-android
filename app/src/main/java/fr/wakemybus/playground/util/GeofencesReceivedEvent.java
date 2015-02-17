package fr.wakemybus.playground.util;

import java.util.ArrayList;

import fr.wakemybus.playground.geofencing.SimpleGeofence;

/**
 * Created by thibaultguegan on 17/02/15.
 */
public class GeofencesReceivedEvent {

    private ArrayList<SimpleGeofence> geofences;

    public GeofencesReceivedEvent(ArrayList<SimpleGeofence> geofences) {
        this.geofences = geofences;
    }

    public ArrayList<SimpleGeofence> getGeofences() {
        return geofences;
    }
}
