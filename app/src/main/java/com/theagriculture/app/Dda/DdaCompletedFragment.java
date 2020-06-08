package com.theagriculture.app.Dda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

public class DdaCompletedFragment extends Fragment {
    private static final String TAG = "DdaCompletedFragment";
    private ArrayList<String> mAdoNames;
    private ArrayList<String> Address;
    private ArrayList<String> mIdLsit;
    private DdacompletedAdapter ddacompletedAdapter;
    private String urlget = "http://18.224.202.135/api/locations/dda/completed";
    private String dda;
    private String token;
    private String villagename;
    private String blockname;
    private String district;
    private String nextUrl;
    private ArrayList<String> mDates;
    private boolean isNextBusy = false;
    private View view;
    private int length_of_array;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ongoing,container,false);
        mAdoNames = new ArrayList<String>();
        Address = new ArrayList<String>();
        mIdLsit = new ArrayList<>();
        mDates = new ArrayList<>();
        ddacompletedAdapter = new DdacompletedAdapter(getContext(), mAdoNames, Address, mIdLsit,mDates);
        RecyclerView review = view.findViewById(R.id.recyclerViewongoing);
        review.setAdapter(ddacompletedAdapter);

        swipeRefreshLayout = view.findViewById(R.id.refreshpull_dda);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(DdaCompletedFragment.this).attach(DdaCompletedFragment.this).commit();
            }
        });
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    length_of_array = jsonArray.length();
                    if(length_of_array==0){
                        ddacompletedAdapter.showcomletedshimmer = false;
                        ddacompletedAdapter.notifyDataSetChanged();
                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                    }
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject c = jsonArray.getJSONObject(i);
                        mDates.add(c.getString("acq_date"));
                        JSONObject ob = c.getJSONObject("ado");
                        String ado_name= ob.getString("name");
                        mAdoNames.add(ado_name);
                        String id = c.getString("id");
                        mIdLsit.add(id);
                        villagename = c.getString("village_name");
                        blockname = c.getString("block_name");
                        district = c.getString("district");
                        Address.add(villagename.toUpperCase() + ", " + blockname.toUpperCase() + ", " +
                                district.toUpperCase());
                        Log.d(TAG, "onResponse: some error in if");
                    }
                    ddacompletedAdapter.showcomletedshimmer = false;
                    ddacompletedAdapter.notifyDataSetChanged();
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
                    length_of_array=jsonArray.length();
                    if(length_of_array==0){
                        ddacompletedAdapter.showcomletedshimmer = false;
                        ddacompletedAdapter.notifyDataSetChanged();
                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        mDates.add(c.getString("acq_date"));
                        JSONObject ob = c.getJSONObject("ado");
                        String ado_name= ob.getString("name");
                        dda = c.getString("dda");
                        mAdoNames.add(ado_name);
                        String id = c.getString("id");
                        mIdLsit.add(id);
                        villagename = c.getString("village_name");
                        blockname = c.getString("block_name");
                        district = c.getString("district");
                        Address.add(villagename.toUpperCase() + "," + blockname.toUpperCase() + "," +
                                district.toUpperCase());
                        Log.d(TAG, "onResponse: some error in if");
                        isNextBusy = false;
                    }
                    ddacompletedAdapter.notifyDataSetChanged();
                    ddacompletedAdapter.showcomletedshimmer = false;
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
