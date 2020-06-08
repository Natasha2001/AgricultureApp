package com.theagriculture.app.Dda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
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

public class notassignedfragment extends Fragment {

    private static final String TAG = "notassignedfragment";
    private ArrayList<String> mHeading;
    private ArrayList<String> Address;
    private ArrayList<String> mDates;
    private DdapendingUnassignedAdapter ddapendingUnassignedAdapter;
    private String urlget = "http://18.224.202.135/api/locations/dda/unassigned";
    private String villagename;
    private String blockname;
    private String district;
    private String state;
    private String token;
    private View view;
    private String locationid;
    private boolean isRefresh;
    private int length_of_array;

    private String nextUrl;
    private boolean isNextBusy = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    public notassignedfragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notassignedfragment, container, false);
        isRefresh = false;
        mHeading = new ArrayList<>();
        Address = new ArrayList<>();
        mDates = new ArrayList<>();
        swipeRefreshLayout = view.findViewById(R.id.refreshpullDda);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(notassignedfragment.this).attach(notassignedfragment.this).commit();
            }
        });
        ddapendingUnassignedAdapter = new DdapendingUnassignedAdapter(getActivity(), mHeading, Address,mDates);
        RecyclerView notassignedreview = view.findViewById(R.id.recyclerViewnotassigned);
        notassignedreview.setAdapter(ddapendingUnassignedAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        notassignedreview.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(notassignedreview.getContext(),
                layoutManager.getOrientation());
        notassignedreview.addItemDecoration(divider);

        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token","");
        Log.d(TAG, "onCreateView: "+token);


        final RequestQueue unassignedrequestqueue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlget, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    nextUrl = jsonObject.getString("next");
                    length_of_array = jsonArray.length();
                    if(length_of_array==0){
                        ddapendingUnassignedAdapter.showunassignedshimmer = false;
                        ddapendingUnassignedAdapter.notifyDataSetChanged();
                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                    }
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject c = jsonArray.getJSONObject(i);
                        locationid = c.getString("id");
                        mDates.add(c.getString("acq_date"));
                        ddapendingUnassignedAdapter.sendlocationId(locationid);
                        villagename = c.getString("village_name");
                        blockname = c.getString("block_name");
                        district = c.getString("district");
                        state = c.getString("state");
                        mHeading.add(villagename + ", " + blockname);
                        Address.add(district + ", " + state);
                    }
                    ddapendingUnassignedAdapter.showunassignedshimmer = false;
                    ddapendingUnassignedAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onResponse: error in this notassignedfragment"+response);
                }catch (JSONException e){
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error );
            }
        }) {

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
        unassignedrequestqueue.add(jsonObjectRequest);
        notassignedreview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int totalCount, pastItemCount, visibleItemCount;
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastItemCount = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        Log.d(TAG, "onScrolled: " + nextUrl);
                        if (!nextUrl.equals("null") && !isNextBusy)
                            getNextLocations();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        isRefresh = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        isRefresh = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (isRefresh)
        {
            getFragmentManager().beginTransaction().detach(notassignedfragment.this)
                    .attach(notassignedfragment.this).commit();
            Log.d(TAG, "onResume: REFRESH");
            isRefresh = false;
        }
    }

    private void getNextLocations() {
        final RequestQueue unassignedrequestqueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, nextUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    nextUrl = jsonObject.getString("next");
                    length_of_array = jsonArray.length();
                    if(length_of_array==0){
                        ddapendingUnassignedAdapter.showunassignedshimmer = false;
                        Log.d(TAG, "onResponse: ");
                        ddapendingUnassignedAdapter.notifyDataSetChanged();
                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        mDates.add(c.getString("acq_date"));
                        villagename = c.getString("village_name");
                        blockname = c.getString("block_name");
                        district = c.getString("district");
                        state = c.getString("state");
                        mHeading.add(villagename + ", " + blockname);
                        Address.add(district + ", " + state);
                        isNextBusy = false;
                        Log.d(TAG, "onResponse: error in this notassignedfragment" + response);
                    }
                    ddapendingUnassignedAdapter.showunassignedshimmer = false;
                    ddapendingUnassignedAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error);
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
        unassignedrequestqueue.add(jsonObjectRequest);
    }

}
