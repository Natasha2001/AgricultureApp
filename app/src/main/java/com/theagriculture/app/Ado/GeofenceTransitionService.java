package com.theagriculture.app.Ado;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


public class GeofenceTransitionService extends IntentService {


    public GeofenceTransitionService(String name) {
        super(name);
    }

    public GeofenceTransitionService(){
        super("GeofenceTransitionService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()){
            String error= String.valueOf(geofencingEvent.getErrorCode());
            Toast.makeText(getApplicationContext(),"error code:"+error,Toast.LENGTH_LONG);
            return;
        }

        int geofencingTransition = geofencingEvent.getGeofenceTransition();

        if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            Log.d("intentservice", "onHandleIntent: heretooo");






                ado_map_activity.getStatus(true);

        }

    }
}
