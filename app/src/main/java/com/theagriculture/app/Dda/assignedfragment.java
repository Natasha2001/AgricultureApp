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

public class assignedfragment extends Fragment {

    private static final String TAG = "assignedfragment";
    private ArrayList<String> Id;
    private ArrayList<String> Name;
    private ArrayList<String> Address;
    private ArrayList<String> mAdoIds;
    private ArrayList<String> mDate;
    private DdapendingassignedAdapter ddaassignedAdapter;
    private String urlget = "http://18.224.202.135/api/locations/dda/assigned";
    private String token;
    private String villagename;
    private String blockname;
    private String district;
    private String state;
    private String nextUrl;
    private boolean isNextBusy = false;
    private boolean isReferesh;
    private View view;
    private int length_of_arrray;
    private SwipeRefreshLayout swipeRefreshLayout;

    public assignedfragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ongoing,container,false);
        Id = new ArrayList<String>();
        Name = new ArrayList<String>();
        Address = new ArrayList<String>();
        mAdoIds = new ArrayList<>();
        mDate = new ArrayList<>();
        isReferesh = false;
        swipeRefreshLayout = view.findViewById(R.id.refreshpull_dda);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(assignedfragment.this).attach(assignedfragment.this).commit();
            }
        });
        ddaassignedAdapter = new DdapendingassignedAdapter(getActivity(), Id, Name, Address, mAdoIds,mDate);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView review = view.findViewById(R.id.recyclerViewongoing);
        review.setAdapter(ddaassignedAdapter);
        review.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(review.getContext(), layoutManager.getOrientation());
        review.addItemDecoration(divider);
        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token","");
        Log.d(TAG, "onCreateView: "+token);

        Log.d(TAG, "onCreateView: inflated fragment_ongoing");
        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlget, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    nextUrl = jsonObject.getString("next");
                    length_of_arrray = jsonArray.length();
                    if(length_of_arrray==0){
                        ddaassignedAdapter.showassignedshimmer = false;
                        ddaassignedAdapter.notifyDataSetChanged();
                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                    }
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject singleObject = jsonArray.getJSONObject(i);
                        Id.add(singleObject.getString("id"));
                        mDate.add(singleObject.getString("acq_date"));
                        villagename = singleObject.getString("village_name");
                        blockname = singleObject.getString("block_name");
                        district = singleObject.getString("district");
                        state = singleObject.getString("state");
                        Address.add(villagename.toUpperCase() + ", " + blockname.toUpperCase() +
                                ", " + district.toUpperCase());
                        JSONObject adoObject = singleObject.getJSONObject("ado");
                        Name.add(adoObject.getString("name"));
                        String adoId = adoObject.getString("id");
                        mAdoIds.add(adoId);
                    }
                    ddaassignedAdapter.showassignedshimmer = false;
                    ddaassignedAdapter.notifyDataSetChanged();
                }catch (JSONException e){
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
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
        requestQueue.add(jsonObjectRequest);
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
        isReferesh = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        isReferesh = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if(isReferesh)
        {
            getFragmentManager().beginTransaction().detach(assignedfragment.this)
                    .attach(assignedfragment.this).commit();
            Log.d(TAG, "onResume: REFRESH");
            isReferesh = false;
        }
}

    private void getNextLocations() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, nextUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    nextUrl = jsonObject.getString("next");
                    length_of_arrray=jsonArray.length();
                    if(length_of_arrray==0){
                        ddaassignedAdapter.showassignedshimmer = false;
                        ddaassignedAdapter.notifyDataSetChanged();
                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject singleObject = jsonArray.getJSONObject(i);
                        mDate.add(singleObject.getString("acq_date"));
                        Id.add(singleObject.getString("id"));
                        villagename = singleObject.getString("village_name");
                        blockname = singleObject.getString("block_name");
                        district = singleObject.getString("district");
                        state = singleObject.getString("state");
                        Address.add(villagename.toUpperCase() + ", " + blockname.toUpperCase() +
                                ", " + district.toUpperCase());
                        JSONObject adoObject = singleObject.getJSONObject("ado");
                        Name.add(adoObject.getString("name"));
                        String adoId = adoObject.getString("id");
                        mAdoIds.add(adoId);
                    }
                    isNextBusy = false;
                    ddaassignedAdapter.showassignedshimmer = false;
                    ddaassignedAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
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
        requestQueue.add(jsonObjectRequest);
    }
}
