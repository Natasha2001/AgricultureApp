package com.theagriculture.app.Ado;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


public class GeofenceTransitionService2 extends IntentService {


    public GeofenceTransitionService2(String name) {
        super(name);
    }

    public GeofenceTransitionService2(){
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
            Log.d("intentservice", "onHandleIntent: heretooo2");

            CheckInActivity2.getStatus(true);

        }

    }
}
