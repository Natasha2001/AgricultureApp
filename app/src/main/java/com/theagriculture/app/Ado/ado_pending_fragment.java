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

import static com.android.volley.VolleyLog.TAG;

public class ado_pending_fragment extends Fragment {

    private ArrayList<String> mtextview1;
    private ArrayList<String> mtextview2;
    private RecyclerView recyclerView;
    private AdoListAdapter adoListAdapter;
    private ArrayList<String> longitude;
    private ArrayList<String> latitude;
    private ArrayList<String> idList;
    private String url = "http://18.224.202.135/api/locations/ado/pending";
    private String nextUrl;
    private boolean isNextBusy = false;
    View view;
    private boolean isRefresh;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ado_pending_fragment,container,false);
        mtextview1 = new ArrayList<>();
        mtextview2 = new ArrayList<>();
        longitude = new ArrayList<>();
        latitude = new ArrayList<>();
        idList = new ArrayList<>();
        isRefresh = false;
        swipeRefreshLayout = view.findViewById(R.id.refreshpull9);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(ado_pending_fragment.this).attach(ado_pending_fragment.this).commit();
            }
        });

        Log.d("pending", "onCreateView: ");
        //add data in the array with load data
        getData(url);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.ado_pending_rv);
        adoListAdapter = new AdoListAdapter(getContext(), mtextview1, mtextview2, idList);
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
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
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            nextUrl = rootObject.getString("next");
                            if(resultsArray.length()== 0){
                                adoListAdapter.mshowshimmer = false;
                                adoListAdapter.notifyDataSetChanged();

                                view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                                //view.getView().setBackground(getActivity().getResources().getDrawable(R.drawable.no_entry_background));
                            }
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                String id = singleObject.getString("id");
                                idList.add(id);
                                String location_name = singleObject.getString("village_name");
                                String location_address = singleObject.getString("block_name") + ", " +
                                        singleObject.getString("district");
                                String slongitude = singleObject.getString("longitude");
                                String slatitude = singleObject.getString("latitude");
                                mtextview1.add(location_name.toUpperCase());
                                mtextview2.add(location_address.toUpperCase());
                                longitude.add(slongitude);
                                latitude.add(slatitude);
                                adoListAdapter.sendPostion(longitude,latitude);
                            }
                            adoListAdapter.mshowshimmer = false;
                            adoListAdapter.notifyDataSetChanged();
                            isNextBusy = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Fragment fragment = getFragmentManager().findFragmentById(R.id.rootView);
                            fragment.getView().setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getActivity(), "Please Check your internet connection",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getActivity(), "Something went wrong, please try again",
                                    Toast.LENGTH_LONG).show();

                        isNextBusy = false;
                        Log.d(TAG, "onErrorResponse: " + error);
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
        if (isRefresh) {
            getFragmentManager().beginTransaction().detach(ado_pending_fragment.this)
                    .attach(ado_pending_fragment.this).commit();
            Log.d(TAG, "onResume: REFRESH");
            isRefresh = false;
        }
    }
}
