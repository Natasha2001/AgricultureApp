package com.theagriculture.app.Admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.theagriculture.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;


public class adoListofDistrict extends AppCompatActivity {
    private ArrayList<String> username;
    private ArrayList<String> userinfo;
    private ArrayList<String> mUserId;
    private ArrayList<String> mPkList;
    private ArrayList<String> mDdoNames;
    private ArrayList<String> mDistrictNames;
    private String ado_list;
    private String curr_dist;
    private String district_list_url;

    private RecyclerViewAdater recyclerViewAdater;
    private String token;
    private String nextUrl;
    private ProgressBar progressBar;
    private LinearLayoutManager layoutManager;
    private GridLayoutManager gridlayout;
    private boolean isNextBusy = false;
    private RelativeLayout relativeLayout;
    private final String TAG = "ado_info_fragment";
    private RecyclerView Rview;
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ado_listof_district);
        ado_list="";
        district_list_url ="http://18.224.202.135/api/district/";
        username = new ArrayList<>();
        userinfo = new ArrayList<>();
        mUserId = new ArrayList<>();
        mPkList = new ArrayList<>();
        mDdoNames = new ArrayList<>();
        mDistrictNames = new ArrayList<>();
        try {
            Intent intent = getIntent();
            curr_dist = intent.getStringExtra("district");
        }
        catch (Exception e)
        {
            Log.d(TAG, "onCreate: INTENT EXTRA " + e);
        }
        getSupportActionBar().setTitle(curr_dist + " ADOs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = findViewById(R.id.ado_list_progressbar);
        relativeLayout = findViewById(R.id.relativeLayout);
        recyclerViewAdater = new RecyclerViewAdater(this, username, userinfo, mUserId, false,
                mPkList, mDdoNames, mDistrictNames);
        Rview = findViewById(R.id.recyclerViewado);
        Rview.setAdapter(recyclerViewAdater);
        SharedPreferences preferences = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        layoutManager = new LinearLayoutManager(this);
        Rview.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
        Rview.addItemDecoration(divider);
        recyclerViewAdater.mShowShimmer = false;

        dialog = new SpotsDialog.Builder().setContext(this).setMessage("Loading...")
                .setTheme(R.style.CustomDialog)
                .setCancelable(false).build();

        Rview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int totalCount, pastItemCount, visibleItemCount;
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastItemCount = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        Log.d(TAG, "onScrolled: " + totalCount + " " + pastItemCount + " " + visibleItemCount);
                        if (!nextUrl.equals("null") && !isNextBusy)
                            getNextAdos();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        getadolist(curr_dist);
    }

    void getadolist(String district){

        ado_list="http://18.224.202.135/api/users-list/ado/?search="+district;

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ado_list, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //relativeLayout.setBackground(getResources().getDrawable(R.drawable.data_background));


                Log.d(TAG, "onResponse: sizes"+username.size()+userinfo.size());

                try {
                    JSONObject rootObject = new JSONObject(String.valueOf(response));
                    nextUrl = rootObject.getString("next");
                    Log.d(TAG, "onResponse: " + nextUrl);
                    JSONArray resultsArray = rootObject.getJSONArray("results");
                    if(resultsArray.length()== 0){
                        recyclerViewAdater.mShowShimmer = false;
                        recyclerViewAdater.notifyDataSetChanged();

                        relativeLayout.setBackground(getResources().getDrawable(R.mipmap.no_entry_background));
                        //relativeLayout.getView().setBackground(getActivity().getResources().getDrawable(R.drawable.no_entry_background));
                    }
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject singleObject = resultsArray.getJSONObject(i);
                        username.add(singleObject.getString("name").toUpperCase());
                        JSONArray villageArray = singleObject.getJSONArray("village");
                        Log.d(TAG, "onResponse: LENGTH " + villageArray.length());
                        if (villageArray.length() == 0)
                            userinfo.add("NOT ASSIGNED");
                        for (int j = 0; j < 1; j++) {
                            try {
                                JSONObject villageObject = villageArray.getJSONObject(i);
                                userinfo.add(villageObject.getString("village").toUpperCase());
                            } catch (JSONException e) {
                                userinfo.add("NOT ASSIGNED");
                            }
                        }
                        JSONObject authObject = singleObject.getJSONObject("auth_user");
                        String pk = authObject.getString("pk");
                        mPkList.add(pk);
                        String id = singleObject.getString("id");
                        mUserId.add(id);
                        try {
                            JSONObject ddaObject = singleObject.getJSONObject("dda");
                            String ddaName = ddaObject.getString("name");
                            mDdoNames.add(ddaName);
                            try {
                                JSONObject districtObject = ddaObject.getJSONObject("district");
                                String districtName = districtObject.getString("district");
                                mDistrictNames.add(districtName.toUpperCase());
                            } catch (JSONException e) {
                                mDistrictNames.add("NOT ASSIGNED");
                            }
                        } catch (JSONException e) {
                            mDdoNames.add("Not Assigned");
                        }
                    }

                    recyclerViewAdater.mShowShimmer = false;
                    recyclerViewAdater.notifyDataSetChanged();
                    dialog.dismiss();

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: JSON" + e);
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getApplicationContext(), "Check Your Internt Connection Please!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorResponse: " + error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
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
    }

    private void getNextAdos() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        isNextBusy = true;
        Log.d(TAG, "getNextAdos: count ");
        progressBar.setVisibility(View.VISIBLE);
        final JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, nextUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rootObject = new JSONObject(String.valueOf(response));
                    nextUrl = rootObject.getString("next");
                    JSONArray resultsArray = rootObject.getJSONArray("results");
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject singleObject = resultsArray.getJSONObject(i);
                        username.add(singleObject.getString("name").toUpperCase());
                        JSONArray villageArray = singleObject.getJSONArray("village");
                        Log.d(TAG, "onResponse: LENGTH " + villageArray.length());
                        if (villageArray.length() == 0)
                            userinfo.add("NOT ASSIGNED");
                        for (int j = 0; j < 1; j++) {
                            try {
                                JSONObject villageObject = villageArray.getJSONObject(i);
                                userinfo.add(villageObject.getString("village").toUpperCase());
                            } catch (JSONException e) {
                                userinfo.add("NOT ASSIGNED");
                            }
                        }
                        JSONObject authObject = singleObject.getJSONObject("auth_user");
                        String pk = authObject.getString("pk");
                        mPkList.add(pk);
                        String id = singleObject.getString("id");
                        mUserId.add(id);
                        try {
                            JSONObject ddaObject = singleObject.getJSONObject("dda");
                            String ddaName = ddaObject.getString("name");
                            mDdoNames.add(ddaName);
                            try {
                                JSONObject districtObject = ddaObject.getJSONObject("district");
                                String districtName = districtObject.getString("district");
                                mDistrictNames.add(districtName.toUpperCase());
                            } catch (JSONException e) {
                                mDistrictNames.add("NOT ASSIGNED");
                            }
                        } catch (JSONException e) {
                            mDdoNames.add("Not Assigned");
                        }
                    }
                    Log.d(TAG, "onResponse: " + username);
                    recyclerViewAdater.notifyDataSetChanged();
                    isNextBusy = false;

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getApplicationContext(), "Check Your Internt Connection Please!",
                            Toast.LENGTH_SHORT).show();
                isNextBusy = false;
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        requestQueue.add(jsonArrayRequest);
        requestFinished(requestQueue);
        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
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

    }



    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
