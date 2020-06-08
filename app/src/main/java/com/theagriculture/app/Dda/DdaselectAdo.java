package com.theagriculture.app.Dda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
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


public class DdaselectAdo extends AppCompatActivity {
    private static final String TAG = "DdaselectAdo";
    private ArrayList<String> nameofado;
    private Map<Integer, ArrayList<String>> villagename;
    private String mCurrentAdoId = "";
    private String urlget = "http://18.224.202.135/api/ado/";
    private String token;
    private DdaAdoListAdapter ddaAdoListAdapter;
    private String idtopass;
    private String adoid;
    public static boolean isAssigned = false;
    private LinearLayoutManager layoutManager;
    private String nextUrl;
    private boolean isNextBusy = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ddaselect_ado);


        nameofado = new ArrayList<String>();
        villagename = new HashMap<>();
        progressBar = findViewById(R.id.ado_list_loading);
        ddaAdoListAdapter = new DdaAdoListAdapter(DdaselectAdo.this,nameofado,villagename);
        RecyclerView review = findViewById(R.id.RecyclerViewadolist);
        review.setAdapter(ddaAdoListAdapter);
        layoutManager = new LinearLayoutManager(this);
        review.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(review.getContext(), layoutManager.getOrientation());
        review.addItemDecoration(divider);
        //getting location id coming from unassigned fragment to this activity
        Intent intent = getIntent();
        idtopass = intent.getStringExtra("Id_I_Need");
        mCurrentAdoId = intent.getStringExtra("adoId");
        Log.d(TAG, "onCreate: Id_I_Need=" + idtopass);
        ddaAdoListAdapter.getlocationid(idtopass);
        try {

        } catch (Exception e) {

        }
        //Toast.makeText(this, "List of Ado's", Toast.LENGTH_SHORT).show();
        loadData(urlget);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("List of Ado's");
        SharedPreferences preferences = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        Log.d(TAG, "onCreateView: " + token);

        review.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int totalCount, pastItemCount, visibleItemCount;
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastItemCount = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        Log.d(TAG, "onScrolled: " + nextUrl);
                        if (!nextUrl.equals("null") && !isNextBusy) {
                            progressBar.setVisibility(View.VISIBLE);
                            loadData(nextUrl);
                        }
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }

    private void loadData(String url){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        isNextBusy = true;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    nextUrl = jsonObject.getString("next");
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    int currentListSize = nameofado.size();
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject c =jsonArray.getJSONObject(i);
                        adoid = c.getString("id");
                        if (adoid.equals(mCurrentAdoId)) {
                            ddaAdoListAdapter.getCurrentAdo(currentListSize + i);
                            Log.d(TAG, "onResponse: CURRENT ADO ID" + currentListSize + i);
                        }
                        ddaAdoListAdapter.getadoid(adoid);
                        Log.d(TAG, "onResponse: ID " + adoid);
                        nameofado.add(c.getString("name").toUpperCase());
                        JSONArray villageArray = c.getJSONArray("village");
                        ArrayList<String> villagesList = new ArrayList<>();
                        for (int j = 0; j < villageArray.length(); j++)
                        {
                            JSONObject singleObject = villageArray.getJSONObject(j);
                            String village = singleObject.getString("village");
                            villagesList.add(village);
                        }
                        villagename.put(i, villagesList);
                    }
                    isNextBusy = false;
                    ddaAdoListAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }catch (JSONException e){
                    Log.d(TAG, "onResponse: "+e);
                    isNextBusy = false;
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onResponse: JSON EXCEPTION " + e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.getLocalizedMessage());
                if (error instanceof NoConnectionError)
                    Toast.makeText(getApplicationContext(), "Please Check your internet connection",
                            Toast.LENGTH_LONG).show();
                isNextBusy = false;
                progressBar.setVisibility(View.GONE);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
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
        requestQueue.add(jsonObjectRequest);

    }

    //for back button on action bar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.searchmenu,menu);
        return true;
    }


}
