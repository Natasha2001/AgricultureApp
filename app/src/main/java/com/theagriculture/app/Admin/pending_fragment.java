package com.theagriculture.app.Admin;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theagriculture.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class pending_fragment extends Fragment {

    //vars
    private ArrayList<String> mDdaName;
    private ArrayList<String> mAdaName;
    private ArrayList<String> mAddress;
    private ArrayList<String> mpkado;
    private ArrayList<String> mpkdda;
    private ArrayList<String> mdate;
    private String token;
    private String villagename;
    private String blockname;
    private String district;

    //tags
    private static final String TAG = "pending_fragment";
    private String pendingUrl = "http://18.224.202.135/api/locations/pending";
    private String nextPendingUrl = "null";
    private LinearLayoutManager layoutManager;
    private AdminLocationAdapter recyclerViewAdater;
    private ProgressBar progressBar;
    private boolean isNextBusy = false;
    private boolean isSendingNotifications = false;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;

    public pending_fragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.pending_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewpending);
        progressBar = view.findViewById(R.id.locations_loading);
        swipeRefreshLayout = view.findViewById(R.id.refreshpull4);
        FloatingActionButton notificationButton = view.findViewById(R.id.notifications_button);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(pending_fragment.this).attach(pending_fragment.this).commit();
            }
        });
        mDdaName = new ArrayList<>();
        mAdaName = new ArrayList<>();
        mAddress = new ArrayList<>();
        mpkado = new ArrayList<>();
        mpkdda = new ArrayList<>();
        mdate = new ArrayList<>();
        recyclerViewAdater = new AdminLocationAdapter(getActivity(), mDdaName, mAdaName,true, mAddress, null,mpkado,mpkdda,mdate);
        recyclerView.setAdapter(recyclerViewAdater);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
        final SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        Log.d(TAG, "onCreateView: " + token);
        Log.d(TAG, "onCreateView: inflated fragment_ongoing");

        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        final JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, pendingUrl, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    nextPendingUrl = jsonObject.getString("next");
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        String date = c.getString("acq_date");
                        mdate.add(date);
                        try {
                            JSONObject adoobj = c.getJSONObject("ado");

                            JSONObject authado = adoobj.getJSONObject("auth_user");
                            mpkado.add(authado.getString("pk"));

                        } catch (JSONException e) {
                            mpkado.add("null");
                        }
                        try {
                            JSONObject ddaobj = c.getJSONObject("dda");
                            JSONObject authddo = ddaobj.getJSONObject("auth_user");
                            mpkdda.add(authddo.getString("pk"));
                        } catch (JSONException e)
                        {
                            mpkdda.add("null");
                        }
                        villagename = c.getString("village_name");
                        blockname = c.getString("block_name");
                        district = c.getString("district");
                        try {
                            JSONObject mDdaObject = c.getJSONObject("dda");
                            String ddaName = mDdaObject.getString("name");
                            mDdaName.add(ddaName);
                        } catch (JSONException e) {
                            mDdaName.add("Not Assigned");
                        }
                        try {
                            JSONObject mAdoObject = c.getJSONObject("ado");
                            String adoName = mAdoObject.getString("name");
                            mAdaName.add(adoName);
                        } catch (JSONException e) {
                            mAdaName.add("Not Assigned");
                        }
                        mAddress.add(villagename.toUpperCase() + ", " +
                                blockname.toUpperCase() + ", " + district.toUpperCase());
                    }
                    recyclerViewAdater.mShowShimmer = false;
                    recyclerViewAdater.notifyDataSetChanged();
                    Log.d(TAG, "onResponse: SIZE " + mdate.size() + "hgfh"+ mDdaName.size());
                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getActivity(), "Check Your Internet Connection Please!",
                            Toast.LENGTH_LONG).show();
                else Toast.makeText(getActivity(), "Something went wrong, Please try again later!",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "onErrorResponse: " + error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };


        requestQueue.add(jsonObjectRequest1);
        jsonObjectRequest1.setRetryPolicy(new RetryPolicy() {
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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int totalCount, pastItemCount, visibleItemCount;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                Log.d(TAG, "onScrolled: out DX " + dx + " DY " + dy);
                if (dy > 0) {
                    Log.d(TAG, "onScrolled: inside");
                    totalCount = layoutManager.getItemCount();
                    pastItemCount = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        if (!isNextBusy)
                            loadNextLocations();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (!isSendingNotifications)
                    sendNotifications();
                else
                    Toast.makeText(getActivity(), "Please wait, Notifications request in progress",
                            Toast.LENGTH_SHORT).show();*/

                Intent intent = new Intent(getActivity(),NotificationActivity.class);
                startActivity(intent);


            }
        });
        return view;
    }

    private void loadNextLocations() {
        if (!nextPendingUrl.equals("null")) {
            getNextPendingLocations();
        }
    }

    private void getNextPendingLocations() {
        Log.d(TAG, "getNextPendingLocations: inside");
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        progressBar.setVisibility(View.VISIBLE);
        isNextBusy = true;
        final JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, nextPendingUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonObject = new JSONObject(String.valueOf(response));
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        nextPendingUrl = jsonObject.getString("next");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject c = jsonArray.getJSONObject(i);
                            String date = c.getString("acq_date");
                            mdate.add(date);

                            try {
                                JSONObject adoobj = c.getJSONObject("ado");

                                JSONObject authado = adoobj.getJSONObject("auth_user");
                                mpkado.add(authado.getString("pk"));

                            } catch (JSONException e) {
                                mpkado.add("null");
                            }
                            try {
                                JSONObject ddaobj = c.getJSONObject("dda");
                                JSONObject authddo = ddaobj.getJSONObject("auth_user");
                                mpkdda.add(authddo.getString("pk"));
                            } catch (JSONException e)
                            {
                                mpkdda.add("null");
                            }

                            villagename = c.getString("village_name");
                            blockname = c.getString("block_name");
                            district = c.getString("district");
                            try {
                                JSONObject mDdaObject = c.getJSONObject("dda");
                                String ddaName = mDdaObject.getString("name");
                                mDdaName.add(ddaName);
                            } catch (JSONException e)
                            {
                                mDdaName.add("NOT ASSIGNED");
                            }
                            try {
                                JSONObject mAdoObject = c.getJSONObject("ado");
                                String adoName = mAdoObject.getString("name");
                                mAdaName.add(adoName);
                            } catch (JSONException e) {
                                mAdaName.add("NOT ASSIGNED");
                            }
                            mAddress.add(villagename.toUpperCase() + ", "
                                    + blockname.toUpperCase() + ", " + district.toUpperCase());
                        }
                        Log.d(TAG, "onResponse:mdate size"+mdate.size());
                        recyclerViewAdater.notifyDataSetChanged();
                        isNextBusy = false;
                    } catch (JSONException e) {
                        Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                        e.printStackTrace();
                        isNextBusy = false;
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof NoConnectionError)
                        Toast.makeText(getActivity(), "Check Your Internt Connection Please!",
                                Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), "Something went wrong, Please try again later!",
                                Toast.LENGTH_LONG).show();
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
        jsonObjectRequest1.setRetryPolicy(new RetryPolicy() {
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
        requestQueue.add(jsonObjectRequest1);
        requestFinished(requestQueue);

    }

    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void sendNotifications() {
        isSendingNotifications = true;
        String url = "http://18.224.202.135/api/trigger/sms/pending";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: sendNotifications " + response);
                        Toast.makeText(getActivity(), "Notifications Successfully Sent!",
                                Toast.LENGTH_SHORT).show();
                        isSendingNotifications = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getActivity(), "Check Your Internt Connection Please!",
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), "Something went wrong, please try again!",
                                    Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse: sendNotifications " + error);
                        isSendingNotifications = false;
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
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

