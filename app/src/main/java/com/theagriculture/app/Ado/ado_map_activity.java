package com.theagriculture.app.Ado;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.theagriculture.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ado_map_activity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status> {

    private final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private double longitude;
    private double latitude;
    private String id;
    private String villageName;
    private Boolean showmap;

    private final int RESULT_CODE = 786;
    private GoogleMap map = null;
    private final String TAG = "ado_map_activity";
    public static boolean isEntered = false;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    MarkerOptions Dlocation;
    private android.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ado_map_activity);
        Intent intent = getIntent();
        longitude = Double.parseDouble(intent.getStringExtra("longitude"));
        latitude = Double.parseDouble(intent.getStringExtra("latitude"));
        id = intent.getStringExtra("id");
        villageName = intent.getStringExtra("village_name");
        String title = intent.getStringExtra("title");
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "onCreate: " + latitude + " " + longitude);


        if (getPermission()) {
            SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ado_map));

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    Log.d("inside onMapReady", "onMapReady: ");
                    map = googleMap;

                    //get latlong for corners for specified city

                    LatLng one = new LatLng(latitude, longitude);
                    LatLng two = new LatLng(37.090000, 97.34466);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    //add them to builder
                    builder.include(one);
                    builder.include(two);

                    LatLngBounds bounds = builder.build();

                    //get width and height to current display screen
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;

                    // 20% padding
                    int padding = (int) (width * 0.20);

                    //set latlong bounds
                    map.setLatLngBoundsForCameraTarget(bounds);

                    //move camera to fill the bound to screen
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

                    //set zoom to level to current so that you won't be able to zoom out viz. move outside bounds
                    map.setMinZoomPreference(map.getCameraPosition().zoom);

                    Dlocation = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                    //marking the position
                    map.addMarker(Dlocation);

                    //buildGoogleApiClient();

                    map.setMyLocationEnabled(true);



                }


            });
        }


    }

    GeofencingRequest geofencingRequest;

    private void startgeofence(MarkerOptions dlocation) {
        if (dlocation != null) {
            Geofence geofence = creategeofence(dlocation.getPosition(), 350f);
            geofencingRequest = creategeofencerequest(geofence);
            addgeofence(geofence);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        drawGeofence();

    }

    Circle geofencelimit;

    private void drawGeofence() {

        if (geofencelimit != null) {
            geofencelimit.remove();
        }
        CircleOptions circleOptions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            circleOptions = new CircleOptions()
                    .center(Dlocation.getPosition())
                    .strokeColor(Color.argb(94, 182, 24, 38))
                    .fillColor(Color.argb(100, 239, 83, 79))
                    .radius(350f);
        } else {
            circleOptions = new CircleOptions()
                    .center(Dlocation.getPosition())
                    .strokeColor(Color.argb(94, 182, 24, 38))
                    .radius(350f);


        }

        geofencelimit = map.addCircle(circleOptions);
        dialog.dismiss();


    }

    private void addgeofence(Geofence geofence) {
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, creategeofencePendingIntent())
                .setResultCallback(this);
    }

    PendingIntent geofencePendingIntent;

    private PendingIntent creategeofencePendingIntent() {

        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionService.class);

        geofencePendingIntent =PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private GeofencingRequest creategeofencerequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    private Geofence creategeofence(LatLng position, float v) {
        return new Geofence.Builder().setRequestId("My Request")
                .setCircularRegion(position.latitude, position.longitude, v)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean getPermission() {
        List<String> Permission = new ArrayList();
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.add(ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.add(ACCESS_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), CAMERA_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.add(CAMERA_PERMISSION);
        }

        if (!Permission.isEmpty()) {
            String[] permissions = Permission.toArray(new String[Permission.size()]);
            ActivityCompat.requestPermissions(this, permissions, RESULT_CODE);
            return false;
        } else
            return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == RESULT_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if (deniedCount == 0) {
                SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ado_map));

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        Log.d("inside onMapReady", "onMapReady: ");
                        map = googleMap;

                        //get latlong for corners for specified city

                        LatLng one = new LatLng(7.798000, 68.14712);
                        LatLng two = new LatLng(37.090000, 97.34466);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        //add them to builder
                        builder.include(one);
                        builder.include(two);

                        LatLngBounds bounds = builder.build();

                        //get width and height to current display screen
                        int width = getResources().getDisplayMetrics().widthPixels;
                        int height = getResources().getDisplayMetrics().heightPixels;

                        // 20% padding
                        int padding = (int) (width * 0.20);

                        //set latlong bounds
                        map.setLatLngBoundsForCameraTarget(bounds);

                        //move camera to fill the bound to screen
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

                        //set zoom to level to current so that you won't be able to zoom out viz. move outside bounds
                        map.setMinZoomPreference(map.getCameraPosition().zoom);

                        //marking the position
//                        map.addMarker(Dlocation);

                       /* LatLng one = new LatLng(7.798000, 68.14712);
                        LatLng two = new LatLng(37.090000, 97.34466);

                        LatLng shimala = new LatLng(31.104815,77.173401);
                        LatLng jaipur = new LatLng(26.912434,75.787270);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        LatLngBounds.Builder builder1 = new LatLngBounds.Builder();


                        //add them to builder
                        builder.include(one);
                        builder.include(two);

                        builder1.include(shimala);
                        builder1.include(jaipur);

                        LatLngBounds bounds = builder.build();
                        LatLngBounds bounds1 = builder1.build();

                        //get width and height to current display screen
                        int width = getResources().getDisplayMetrics().widthPixels;
                        int height = getResources().getDisplayMetrics().heightPixels;

                        // 20% padding
                        int padding = (int) (width * 0.20);

                        //set latlong bounds
                        map.setLatLngBoundsForCameraTarget(bounds);

                        //move camera to fill the bound to screen
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds1, width, height, padding));

                        map.setMinZoomPreference(map.getCameraPosition().zoom);*/

                        Dlocation = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                        //marking the position
                        map.addMarker(Dlocation);

                        buildGoogleApiClient();
                        map.setMyLocationEnabled(true);



                    }


                });
            } else {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        showDialog("", "This app needs location and files permissions to work without any problems.",
                                "Yes, Grant permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        getPermission();
                                    }
                                },
                                "No, Exit app",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                    } else {
                        showDialog("",
                                "You have denied some permissions. Allow all the permissions at [Setting] > [Permissions]",
                                "Go to Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, "No, Exit App",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                        break;
                    }
                }
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


    }

    private AlertDialog showDialog(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnclick,
                                   String negativeLabel, DialogInterface.OnClickListener negativeOnclick,
                                   boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnclick);
        builder.setNegativeButton(negativeLabel, negativeOnclick);
        builder.setCancelable(isCancelable);
        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }


    public void onClickNavigation(View view) {
        //String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", latitude, longitude, "Destination Location");
        String uri = "https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    public static void  getStatus(Boolean status){
        Log.d("getstatus", "getStatus: herehere"+ status);
        isEntered = status;
    }


    public void onClickCheckIn(View view) {
        Log.d(TAG, "onClickCheckIn: is "+isEntered);
        if (isEntered) {
            Intent intent = new Intent(this, CheckInActivity2.class);
            intent.putExtra("id", id);
            intent.putExtra("lat",latitude);
            intent.putExtra("long",longitude);
            intent.putExtra("village_name", villageName);
            Log.d(TAG, "onClickCheckIn: " + id);
            startActivity(intent);
        }

        else {
            Toast.makeText(getApplicationContext(),"visit the location to enable the button",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = map.addMarker(markerOptions);*/

        LatLng one = new LatLng(latitude,longitude);
        LatLng two =latLng;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //add them to builder
        builder.include(one);
        builder.include(two);

        LatLngBounds bounds = builder.build();

        //get width and height to current display screen
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        // 20% padding
        int padding = (int) (width * 0.20);

        //set latlong bounds
        map.setLatLngBoundsForCameraTarget(bounds);

        //move camera to fill the bound to screen
        // map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

        //set zoom to level to current so that you won't be able to zoom out viz. move outside bounds
        map.setMinZoomPreference(map.getCameraPosition().zoom);

        /*//move map camera
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
        map.moveCamera();*/
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (getPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        startgeofence(Dlocation);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            isEntered = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: superhere");

        if (true) {
            if (true) {
                dialog = new SpotsDialog.Builder().setContext(this).setMessage("Connecting...")
                        .setTheme(R.style.CustomDialog)
                        .setCancelable(false).build();
                dialog.show();
                Log.d(TAG, "onResume: here");
                buildGoogleApiClient();
            }
        }
    }
}

