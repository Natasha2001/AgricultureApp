package com.theagriculture.app.Admin;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class OnGoingFragment extends Fragment {

    private String ongoingUrl = "http://18.224.202.135/api/locations/ongoing";

    private ArrayList<String> mDDaNames;
    private ArrayList<String> mAdoNames;
    private ArrayList<String> mAddresses;
    private ArrayList<String> mIds;
    private ArrayList<String> mdate;
    private AdminLocationAdapter adapter;
    private LinearLayoutManager layoutManager;
    private String token;
    private String next_ongoing_url;
    private ProgressBar progressBar;
    private boolean isNextBusy = false;
    private  View view;
    private SwipeRefreshLayout swipeRefreshLayout;

    public OnGoingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       view = inflater.inflate(R.layout.fragment_on_going, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.ongoing_recyclerview);
        progressBar = view.findViewById(R.id.locations_loading_ongoing);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout = view.findViewById(R.id.refreshpull3);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(OnGoingFragment.this).attach(OnGoingFragment.this).commit();
            }
        });
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
        mDDaNames = new ArrayList<>();
        mAdoNames = new ArrayList<>();
        mAddresses = new ArrayList<>();
        mIds = new ArrayList<>();
        mdate = new ArrayList<>();
        adapter = new AdminLocationAdapter(getActivity(), mDDaNames, mAdoNames, mAddresses, mIds, true,mdate);
        recyclerView.setAdapter(adapter);
        final SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        getData();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int totalCount, pastItemCount, visibleItemCount;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastItemCount = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        if (!next_ongoing_url.equals("null") && !isNextBusy)
                            get_Ongoing();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    private void getData() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(ongoingUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            next_ongoing_url = rootObject.getString("next");
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            if(resultsArray.length()== 0){
                                adapter.mShowShimmer = false;
                                adapter.notifyDataSetChanged();

//                                view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                                view.setBackgroundResource(R.mipmap.no_entry_background);
                                //view.getView().setBackground(getActivity().getResources().getDrawable(R.drawable.no_entry_background));
                            }
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                JSONObject mDdaObject = singleObject.getJSONObject("dda");
                                String date = singleObject.getString("acq_date");
                                String ddaName = mDdaObject.getString("name");
                                mDDaNames.add(ddaName);
                                mdate.add(date);
                                String id = singleObject.getString("id");
                                mIds.add(id);
                                try {
                                    JSONObject mAdoObject = singleObject.getJSONObject("ado");
                                    String adoName = mAdoObject.getString("name");
                                    mAdoNames.add(adoName);
                                } catch (JSONException e) {
                                    mAdoNames.add("Not Assigned");
                                }
                                String location = singleObject.getString("village_name").toUpperCase() +
                                        ", " + singleObject.getString("block_name").toUpperCase() + ", "
                                        + singleObject.getString("district").toUpperCase();
                                mAddresses.add(location);
                            }
                            adapter.mShowShimmer = false;
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getActivity(), "Check Your Internt Connection Please!", Toast.LENGTH_SHORT).show();
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

    private void get_Ongoing() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(next_ongoing_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            next_ongoing_url = rootObject.getString("next");
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                JSONObject mDdaObject = singleObject.getJSONObject("dda");
                                String date = singleObject.getString("acq_date");
                                String ddaName = mDdaObject.getString("name");
                                mDDaNames.add(ddaName);
                                mdate.add(date);
                                String id = singleObject.getString("id");
                                mIds.add(id);
                                try {
                                    JSONObject mAdoObject = singleObject.getJSONObject("ado");
                                    String adoName = mAdoObject.getString("name");
                                    mAdoNames.add(adoName);
                                } catch (JSONException e) {
                                    mAdoNames.add("Not Assigned");
                                }
                                String location = singleObject.getString("village_name").toUpperCase() + ", " +
                                        singleObject.getString("block_name").toUpperCase() + ", "
                                        + singleObject.getString("district").toUpperCase();
                                mAddresses.add(location);
                                isNextBusy = false;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getActivity(), "Check Your Internt Connection Please!", Toast.LENGTH_SHORT).show();
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
        requestQueue.add(jsonObjectRequest);
        requestFinished(requestQueue);
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


    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}
