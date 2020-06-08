package com.theagriculture.app.Ado;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ado_complete_fragment extends Fragment {

    private ArrayList<String> mtextview1;
    private ArrayList<String> mtextview2;
    private RecyclerView recyclerView;
    private AdoListAdapter adoListAdapter;
    private ArrayList<String> longitude;
    private ArrayList<String> latitude;
    private String url = "http://18.224.202.135/api/locations/ado/completed";
    private String nextUrl;
    private boolean isNextBusy = false;
    private View view;
    private ArrayList<String> mId;
    private SwipeRefreshLayout swipeRefreshLayout;

    //tag
    private final String TAG = "ado_complete_fragmnt";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ado_complete_fragment, container, false);
        mtextview1 = new ArrayList<>();
        mtextview2 = new ArrayList<>();
        longitude = new ArrayList<>();
        latitude = new ArrayList<>();
        mId = new ArrayList<>();
        //add data in the array with load data
        getData(url);
        Log.d(TAG, "onCreateView: inside onCreate");

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.ado_completed_rv);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        swipeRefreshLayout = view.findViewById(R.id.refreshpull8);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(ado_complete_fragment.this).attach(ado_complete_fragment.this).commit();
            }
        });
        adoListAdapter = new AdoListAdapter(getContext(), mtextview1, mtextview2, true, mId);
        recyclerView.setAdapter(adoListAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int totalCount, pastItemCount, visibleItemCount;
                if (dy > 0) {
                    totalCount = linearLayoutManager.getItemCount();
                    pastItemCount = linearLayoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = linearLayoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        Log.d(TAG, "onScrolled: " + nextUrl);
                        if (!nextUrl.equals("null") && !isNextBusy)
                            getData(nextUrl);
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }


    private void getData(String url) {
        Log.d(TAG, "getData: inside getdata");
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "onResponse: ");
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            if(resultsArray.length()== 0){
                                adoListAdapter.mshowshimmer = false;
                                adoListAdapter.notifyDataSetChanged();

                                view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                                //view.getView().setBackground(getActivity().getResources().getDrawable(R.drawable.no_entry_background));
                            }
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                String id = singleObject.getString("id");
                                mId.add(id);
                                String location_name = singleObject.getString("village_name");
                                String location_address = singleObject.getString("block_name") + ", "
                                        + singleObject.getString("district");
                                String slongitude = singleObject.getString("longitude");
                                String slatitude = singleObject.getString("latitude");
                                mtextview1.add(location_name.toUpperCase());
                                mtextview2.add(location_address.toUpperCase());
                                longitude.add(slongitude);
                                latitude.add(slatitude);
                            }
                            adoListAdapter.sendPostion(longitude, latitude);
                            adoListAdapter.mshowshimmer = false;
                            adoListAdapter.notifyDataSetChanged();
                            isNextBusy = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onResponse: inside the evception" + e);

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: inside the the error exception" + error);
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getActivity(), "Please Check your internet connection",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getActivity(), "Something went wrong, please try again",
                                    Toast.LENGTH_LONG).show();
                        isNextBusy = false;
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                SharedPreferences prefs = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
                String token = prefs.getString("token", "");
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
}
