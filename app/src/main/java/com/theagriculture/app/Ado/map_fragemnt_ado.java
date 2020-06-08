package com.theagriculture.app.Ado;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.theagriculture.app.Admin.MyItem;
import com.theagriculture.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class map_fragemnt_ado extends Fragment {
    private final String TAG = "map fragment";

    public GoogleMap map = null;
    private String url_pending = "http://18.224.202.135/api/locations/ado/pending";
    private String token;
    private String next;
    private SupportMapFragment mapFragment;
    private int count = 0;
    //    private ProgressBar pbar;
    private AlertDialog dialog;
    private ArrayList<Double> latitude;
    private ArrayList<Double> longitude;
    private ArrayList<String> villname;
    private ClusterManager<MyItem> mClusterManager;
    private CardView statsCardview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragemnt, container, false);
        mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        statsCardview = view.findViewById(R.id.stats_cardview);
        statsCardview.setVisibility(View.GONE);
        final SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        latitude = new ArrayList<>();
        longitude = new ArrayList<>();
        villname = new ArrayList<>();
//        pbar = view.findViewById(R.id.pbar);
        dialog = new SpotsDialog.Builder().setContext(getActivity()).setMessage("Loading locations...")
                .setTheme(R.style.CustomDialog)
                .setCancelable(false).build();
        dialog.show();

        next = url_pending;
        getMarkers(next);


        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                Log.d(TAG, "onMapReady:value if map is : " + map);

                LatLng one = new LatLng(7.798000, 68.14712);
                LatLng two = new LatLng(37.090000, 97.34466);

                LatLng shimala = new LatLng(31.104815, 77.173401);
                LatLng jaipur = new LatLng(26.912434, 75.787270);

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

                //set zoom to level to current so that you won't be able to zoom out viz. move outside bounds
                map.setMinZoomPreference(map.getCameraPosition().zoom);


            }
        });


        Log.d(TAG, "onCreateView: look me here " + mapFragment);
        return view;
    }

    void getMarkers(String url) {
        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        Double lat = Double.valueOf(c.getString("latitude"));
                        Double lon = Double.valueOf(c.getString("longitude"));
                        String vill = c.getString("village_name");
                        latitude.add(lat);
                        longitude.add(lon);
                        villname.add(vill);
                    }
                    dialog.dismiss();

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
                    dialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error);
//                pbar.setVisibility(View.GONE);
                dialog.dismiss();
                if (error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
        jsonObjectRequest2.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        requestQueue.add(jsonObjectRequest2);
        requestFinished(requestQueue);
    }

    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                Log.d(TAG, "onRequestFinished: here too");
               /* if(count == 0)
                    nextRequest();
                else if(count == 1) */
                marklocation();

            }
        });

    }

    private void marklocation() {
//        pbar.setVisibility(View.GONE);
        /*dialog.dismiss();
        for (int i = 0; i < latitude.size(); i++) {
            MarkerOptions Dlocation = new MarkerOptions().position(new LatLng(latitude.get(i), longitude.get(i))).title(villname.get(i)).icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_action_name));
            map.addMarker(Dlocation);
            if (i == 0) {
//                pbar.setVisibility(View.GONE);
                dialog.dismiss();

            }
        }*/
        mClusterManager = new ClusterManager<MyItem>(getActivity(), map);

        map.setOnCameraIdleListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);

        addmarkers();

        dialog.dismiss();
    }

    private void addmarkers() {
        Log.d(TAG, "addmarkers: inside addmaekers");

        for (int i = 0; i < latitude.size(); i++) {
            double lat = latitude.get(i);
            double lon = longitude.get(i);
            String title = villname.get(i);

            MyItem item = new MyItem(lat, lon, title);

            mClusterManager.addItem(item);


        }
        mClusterManager.cluster();

    }

  /*  void nextRequest(){
        count = 1;
      next= url_unassigned;
      getMarkers(next);


    }*/

    BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
