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
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import br.com.mauker.materialsearchview.MaterialSearchView;

public class villagenameActivity extends AppCompatActivity {
    private static String TAG = "villagenameActivity";
    private String token;
    private ArrayList<String> villagesNames;
    private ArrayList<Integer> villageIds;
    private ArrayList<Integer> currentVillages;
    private ArrayList<String> suggestedVillageNames;
    private ArrayList<Integer> suggestedVillageIds;
    private String mUrl;
    private String nextUrl;
    private boolean isNextBusy = false;
    private VillagesUnderDistrictAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ProgressBar progressBar;
    private ProgressBar listNextProgressBar;
    private String mAdoId;
    private MaterialSearchView materialSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_villagename);
        Toolbar toolbar = findViewById(R.id.village_list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Villages List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        villagesNames = new ArrayList<>();
        villageIds = new ArrayList<>();
        suggestedVillageNames = new ArrayList<>();
        suggestedVillageIds = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.villages_list_recy);
        progressBar = findViewById(R.id.village_list_loading);
        listNextProgressBar = findViewById(R.id.village_list_next);
        materialSearchView = findViewById(R.id.searchView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new VillagesUnderDistrictAdapter(this, villagesNames, villageIds);
        recyclerView.setAdapter(adapter);
        Intent intent = getIntent();
        String districtId = intent.getStringExtra("districtId");
        mAdoId = intent.getStringExtra("adoId");
        currentVillages = intent.getIntegerArrayListExtra("currentVillages");
        Log.d(TAG, "onCreate: " + currentVillages);
        mUrl = "http://18.224.202.135/api/villages-list/district/" + districtId + "/";
        Log.d(TAG, "onCreate: URL " + mUrl);
        SharedPreferences preferences = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        progressBar.setVisibility(View.VISIBLE);
        loadData(mUrl);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int totalCount, pastCount, visibleCount;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastCount = layoutManager.findFirstVisibleItemPosition();
                    visibleCount = layoutManager.getChildCount();
                    if ((pastCount + visibleCount) >= totalCount) {
                        if (!nextUrl.equals("null") && !isNextBusy) {
                            listNextProgressBar.setVisibility(View.VISIBLE);
                            loadData(nextUrl);
                        }
                        Log.d(TAG, "onScrolled:");
                    }
                    Log.d(TAG, "onScrolled: " + totalCount + "total" + pastCount + "past" + visibleCount + "visible");
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    Intent intent = new Intent(villagenameActivity.this, SearchResultsActivity.class);
                    intent.putExtra("searchUrl", mUrl + "?search=" + query);
                    intent.putIntegerArrayListExtra("currentVillages", currentVillages);
                    intent.putExtra("mAdoId", mAdoId);
                    startActivity(intent);
                    return true;
                } else
                    return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    suggestionRequest(newText);
                    return true;
                } else
                    return false;
            }
        });

        materialSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: SIZE" + suggestedVillageNames.size() + "ELEMENTS" + suggestedVillageNames);
                if (i < suggestedVillageNames.size() - 1) {
                    String villageNameSelected = suggestedVillageNames.get(i);
                    Log.d(TAG, "onItemClick: " + villageNameSelected);
                    Intent intent = new Intent(villagenameActivity.this, SearchResultsActivity.class);
                    intent.putExtra("searchUrl", mUrl + "?search=" + villageNameSelected);
                    intent.putIntegerArrayListExtra("currentVillages", currentVillages);
                    intent.putExtra("mAdoId", mAdoId);
                    startActivity(intent);
                }
            }
        });
    }

    private void suggestionRequest(String searchText) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        suggestedVillageNames.clear();
        suggestedVillageIds.clear();
        Log.d(TAG, "suggestionRequest: " + suggestedVillageNames.size() + " ids" + suggestedVillageIds.size());
        String url = mUrl + "?search=" + searchText;
        Log.d(TAG, "suggestionRequest: URL  " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            for (int i = 0; i < 7 && i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                String villageName = singleObject.getString("village");
                                suggestedVillageNames.add(villageName);
                                int villageId = singleObject.getInt("id");
                                suggestedVillageIds.add(villageId);
                            }
                            materialSearchView.clearSuggestions();
                            materialSearchView.clearHistory();
                            Log.d(TAG, "onResponse: SIZE" + suggestedVillageNames.size() + "ELEMENTS" + suggestedVillageNames);
                            materialSearchView.addSuggestions(suggestedVillageNames);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: VILLAGE NAME SUGGESTION " + error);
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getApplicationContext(), "Please check your Internet Connection!",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again!",
                                    Toast.LENGTH_LONG).show();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_village_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.done) {
            saveChanges();
            return true;
        } else if (item.getItemId() == R.id.search) {
            materialSearchView.openSearch();
            return true;
        }
        return false;
    }

    private void loadData(String url) {
        isNextBusy = true;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            nextUrl = rootObject.getString("next");
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject villageObject = resultsArray.getJSONObject(i);
                                String villageName = villageObject.getString("village");
                                villagesNames.add(villageName);
                                int villageId = villageObject.getInt("id");
                                villageIds.add(villageId);
                                if (currentVillages.contains(villageId)) {
                                    adapter.addtoCurrentVillagesPos(villageId);
                                    Log.d(TAG, "onResponse: loaddata " + currentVillages);
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                            listNextProgressBar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                            isNextBusy = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressBar.setVisibility(View.GONE);
                            listNextProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Please try again",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onResponse: JSON EXCEPTION " + e);
                            isNextBusy = false;
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
                        isNextBusy = false;
                        progressBar.setVisibility(View.GONE);
                        listNextProgressBar.setVisibility(View.GONE);
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
        /*jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
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
        });*/
        requestQueue.add(jsonObjectRequest);
    }

    private void saveChanges() {
        SparseBooleanArray villagesIdSparse = adapter.getSparseBooleanArray();
        Log.d(TAG, "saveChanges: " + villagesIdSparse);
        if (villagesIdSparse.size() != 0) {
            ArrayList<Integer> selectedIds = new ArrayList<>();
            for (int i = 0; i < villagesNames.size(); i++) {
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
                                Intent intent = new Intent(villagenameActivity.this, DdaActivity.class);
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

    @Override
    public boolean onSupportNavigateUp() {
        if (materialSearchView.isOpen())
            materialSearchView.closeSearch();
        else
            finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (materialSearchView.isOpen()) {
            materialSearchView.closeSearch();
        }
        else
            super.onBackPressed();
    }
}
