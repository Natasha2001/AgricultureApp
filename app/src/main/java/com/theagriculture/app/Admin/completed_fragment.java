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
import androidx.annotation.Nullable;
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

public class completed_fragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<String> mDdaNames;
    private ArrayList<String> mAdoNames;
    private ArrayList<String> mAddresses;
    private ArrayList<String> mIds;
    private ArrayList<String> mdate;
    private AdminLocationAdapter adapter;
    private LinearLayoutManager layoutManager;
    private String completedUrl = "http://18.224.202.135/api/locations/completed";
    private String nextUrl;
    private String token;
    private ProgressBar progressBar;
    private boolean isNextBusy;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.completed_fragment,container,false);
        recyclerView = view.findViewById(R.id.completed_recyclerview);
        progressBar = view.findViewById(R.id.locations_loading_completed);
        swipeRefreshLayout = view.findViewById(R.id.refreshpull1);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(completed_fragment.this).attach(completed_fragment.this).commit();
            }
        });
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
        mDdaNames = new ArrayList<>();
        mAdoNames = new ArrayList<>();
        mAddresses = new ArrayList<>();
        mIds = new ArrayList<>();
        mdate = new ArrayList<>();
        adapter = new AdminLocationAdapter(getActivity(), mDdaNames, mAdoNames, mAddresses, true, mIds,mdate);
        recyclerView.setAdapter(adapter);
        SharedPreferences prefs = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
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
                        if (!isNextBusy && nextUrl.equals("null"))
                            getNextLocations();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    private void getData() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(completedUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            nextUrl = rootObject.getString("next");
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            if(resultsArray.length()== 0){
                                adapter.mShowShimmer = false;
                                adapter.notifyDataSetChanged();

                                view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                                //view.getView().setBackground(getActivity().getResources().getDrawable(R.drawable.no_entry_background));
                            }
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                JSONObject ddaObject = singleObject.getJSONObject("dda");
                                String date = singleObject.getString("acq_date");
                                mDdaNames.add(ddaObject.getString("name"));
                                String id = singleObject.getString("id");
                                mIds.add(id);
                                mdate.add(date);
                                JSONObject adoObject = singleObject.getJSONObject("ado");
                                String adoName = adoObject.getString("name");
                                if (adoName.equals("null"))
                                    mAdoNames.add("Not Assigned");
                                else
                                    mAdoNames.add(adoName);
                                String location = singleObject.getString("village_name").toUpperCase()
                                        + ", " + singleObject.getString("block_name").toUpperCase() + ", "
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

    private void getNextLocations() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        progressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(nextUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            nextUrl = rootObject.getString("next");
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                mDdaNames.add(singleObject.getString("dda"));
                                String date = singleObject.getString("acq_date");
                                String id = singleObject.getString("id");
                                mIds.add(id);
                                mdate.add(date);
                                String adoName = singleObject.getString("ado");
                                if (adoName.isEmpty())
                                    mAdoNames.add("Not Assigned");
                                else
                                    mAdoNames.add(adoName);
                                String location = singleObject.getString("village_name").toUpperCase()
                                        + ", " + singleObject.getString("block_name").toUpperCase() + ", "
                                        + singleObject.getString("district").toUpperCase();
                                mAddresses.add(location);
                            }
                            adapter.notifyDataSetChanged();
                            isNextBusy = false;

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
