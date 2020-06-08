package com.theagriculture.app.Dda;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class SearchResultsActivity extends AppCompatActivity {

    private static final String TAG = "SearchResultsActivity";
    private RecyclerView searchRecyclerview;
    private ProgressBar progressBar;
    private ProgressBar nextProgressBar;
    private String token;
    private ArrayList<String> villageNames;
    private ArrayList<Integer> villageIds;
    private ArrayList<Integer> currentVillages;
    private String mAdoId;
    private LinearLayoutManager layoutManager;
    private VillagesUnderDistrictAdapter adapter;
    private String nextUrl;
    private boolean isNextBusy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        Intent intent = getIntent();
        String searchUrl = intent.getStringExtra("searchUrl");
        currentVillages = intent.getIntegerArrayListExtra("currentVillages");
        mAdoId = intent.getStringExtra("mAdoId");
        Log.d(TAG, "onCreate: " + currentVillages);
        getSupportActionBar().setTitle("Search Results");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchRecyclerview = findViewById(R.id.search_recyclerview);
        progressBar = findViewById(R.id.search_progressbar);
        nextProgressBar = findViewById(R.id.search_next_progressbar);
        villageNames = new ArrayList<>();
        villageIds = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        searchRecyclerview.setLayoutManager(layoutManager);
        adapter = new VillagesUnderDistrictAdapter(this, villageNames, villageIds);
        searchRecyclerview.setAdapter(adapter);
        SharedPreferences preferences = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        loadResults(searchUrl);
        searchRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int totalCount, pastCount, visibleCount;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastCount = layoutManager.findFirstVisibleItemPosition();
                    visibleCount = layoutManager.getChildCount();
                    if ((pastCount + visibleCount) >= totalCount) {
                        if (!nextUrl.equals("null") && !isNextBusy) {
                            nextProgressBar.setVisibility(View.VISIBLE);
                            loadResults(nextUrl);
                        }
                        Log.d(TAG, "onScrolled:");
                    }
                    Log.d(TAG, "onScrolled: " + totalCount + "total" + pastCount + "past" + visibleCount + "visible");
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadResults(String url) {
        isNextBusy = true;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            nextUrl = rootObject.getString("next");
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            Log.d(TAG, "onResponse: CURRENT" + currentVillages);
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                String villageName = singleObject.getString("village");
                                villageNames.add(villageName);
                                int villageId = singleObject.getInt("id");
                                villageIds.add(villageId);
                                if (currentVillages.contains(villageId)) {
                                    adapter.addtoCurrentVillagesPos(villageId);
                                    Log.d(TAG, "onResponse: loadResults " + currentVillages);
                                }
                            }
                            Log.d(TAG, "onResponse: IDS" + villageIds);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            isNextBusy = false;
                            Log.d(TAG, "onResponse: JSON EXCEPTION " + e);
                        }
                        isNextBusy = false;
                        progressBar.setVisibility(View.GONE);
                        nextProgressBar.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isNextBusy = false;
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getApplicationContext(), "Please check your Internet Connection!",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again!",
                                    Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        nextProgressBar.setVisibility(View.GONE);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_village_list, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        menuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.done) {
            saveChanges();
            return true;
        } else return false;
    }

    private void saveChanges() {
        SparseBooleanArray villagesIdSparse = adapter.getSparseBooleanArray();
        Log.d(TAG, "saveChanges: " + villagesIdSparse);
        if (villagesIdSparse.size() != 0) {
            ArrayList<Integer> selectedIds = new ArrayList<>();
            for (int i = 0; i < villageNames.size(); i++) {
                boolean isSelectedVillage = villagesIdSparse.get(villageIds.get(i));
                if (isSelectedVillage)
                    selectedIds.add(villageIds.get(i));
            }
            Log.d(TAG, "saveChanges: " + villageIds);
            Log.d(TAG, "saveChanges: " + selectedIds);
            JSONArray villagesIdArray = new JSONArray(selectedIds);
            JSONObject paramObject = new JSONObject();
            try {
                paramObject.put("village", villagesIdArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "saveChanges: " + paramObject);
            String url = "http://18.224.202.135/api/user/" + mAdoId + "/";
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.PUT, url, paramObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject jsonObject = new JSONObject(String.valueOf(response));
                                Log.d(TAG, "onResponse: " + jsonObject);
                                Toast.makeText(getApplicationContext(), "Villages Successfully Assigned!",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SearchResultsActivity.this, DdaActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("isAssignedLocation", true);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d(TAG, "onResponse: JSON EXCEPTION " + e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError)
                                Toast.makeText(getApplicationContext(), "Please check your Internet Connection!",
                                        Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getApplicationContext(), "Something went wrong, please try again!",
                                        Toast.LENGTH_LONG).show();
                            Log.d(TAG, "onErrorResponse: loadData " + error);
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Token " + token);
                    return map;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
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
            requestQueue.add(jsonArrayRequest);
        } else
            Toast.makeText(getApplicationContext(), "No village Selected",
                    Toast.LENGTH_SHORT).show();
    }
}
