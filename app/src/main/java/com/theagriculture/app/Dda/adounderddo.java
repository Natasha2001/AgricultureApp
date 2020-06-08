package com.theagriculture.app.Dda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class adounderddo extends Fragment {

    RecyclerView recyclerView;
    ArrayList<String> ado_names;
    private ArrayList<String> adoIds;
    private String urlget = "http://18.224.202.135/api/ado/";
    private String nextUrl;
    private adounderddoadapter adapter;
    private final String TAG ="adouderddo";
    private String token;
    private boolean isNextBusy = false;
    private ProgressBar progressBar;
    private ArrayList<ArrayList<Integer>> villagesMap;
    private ArrayList<String> adoPhones;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adounderddo_list,container,false);
        ado_names = new ArrayList<>();
        adoIds = new ArrayList<>();
        villagesMap = new ArrayList<>();
        adoPhones = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewadounderddo);
        progressBar = view.findViewById(R.id.ado_list_loading);
        adapter = new adounderddoadapter(getContext(), ado_names, adoIds, villagesMap, adoPhones);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        loadData(urlget);
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
                            progressBar.setVisibility(View.VISIBLE);
                            loadData(nextUrl);
                        }
                        Log.d(TAG, "onScrolled:");
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    private void loadData(String url){
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        isNextBusy = true;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    nextUrl = jsonObject.getString("next");
                    JSONArray resultsArray = jsonObject.getJSONArray("results");
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject c = resultsArray.getJSONObject(i);
                        ado_names.add(c.getString("name"));
                        JSONObject authObject = c.getJSONObject("auth_user");
                        String adoId = authObject.getString("pk");
                        adoIds.add(adoId);
                        JSONArray villageArray = c.getJSONArray("village");
                        ArrayList<Integer> villageIds = new ArrayList<>();
                        for (int j = 0; j < villageArray.length(); j++) {
                            JSONObject singleVillage = villageArray.getJSONObject(j);
                            int villageId = singleVillage.getInt("id");
                            Log.d(TAG, "onResponse: IDS " + villageId);
                            villageIds.add(villageId);
                        }
                        String adoPhone = c.getString("number");
                        adoPhones.add(adoPhone);
                        villagesMap.add(villageIds);
                        if (i == 0) {
                            JSONObject ddaObject = c.getJSONObject("dda");
                            JSONObject districtObject = ddaObject.getJSONObject("district");
                            String districtId = districtObject.getString("id");
                            adapter.sendDistrictId(districtId);
                            Log.d(TAG, "onResponse: DISTRICT ID " + districtId);
                        }
                    }
                    adapter.showShimmer = false;
                    isNextBusy = false;
                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
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
                Log.d(TAG, "onErrorResponse: " + error.getLocalizedMessage());
                if (error instanceof NoConnectionError)
                    Toast.makeText(getActivity(), "Please Check your internet connection",
                            Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getActivity(), "Something went wrong, please try again!",
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
}
