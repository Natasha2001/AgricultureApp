package com.theagriculture.app.Admin;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.theagriculture.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ado_fragment extends Fragment {
    private ArrayList<String> username;
    private ArrayList<String> userinfo;
    private ArrayList<String> mUserId;
    private ArrayList<String> mPkList;
    private ArrayList<String> mDdoNames;
    private ArrayList<String> mDistrictNames;
    private ArrayList<String> mdistrictlist;

    private String district_list_url;

    private String token;
    private GridLayoutManager gridlayout;
    private View view;
    private final String TAG = "ado_fragment";
    private RecyclerView Rview;
    private AlertDialog dialog;

    private RecyclerView adolist;
    private RecyclerViewAdapter_district customadapter;


    public ado_fragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ado_fragment, container, false);
        Log.d(TAG, "onCreateView: ");
        adolist=view.findViewById(R.id.adolist);
        district_list_url ="http://18.224.202.135/api/district/";
        username = new ArrayList<>();
        userinfo = new ArrayList<>();
        mUserId = new ArrayList<>();
        mPkList = new ArrayList<>();
        mDdoNames = new ArrayList<>();
        mDistrictNames = new ArrayList<>();
        mdistrictlist = new ArrayList<>();
        //   mdistrictlist.add("Select District");


        //  progressBar = view.findViewById(R.id.ado_list_progressbar);
    /*    recyclerViewAdater = new RecyclerViewAdater(getActivity(), username, userinfo, mUserId, false,
                mPkList, mDdoNames, mDistrictNames);
       // Rview = view.findViewById(R.id.recyclerViewado);
        Rview.setAdapter(recyclerViewAdater);
        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        layoutManager = new LinearLayoutManager(getActivity());
        Rview.setLayoutManager(layoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        Rview.addItemDecoration(divider);
        recyclerViewAdater.mShowShimmer = false;

        dialog = new SpotsDialog.Builder().setContext(getActivity()).setMessage("Loading...")
                .setTheme(R.style.CustomDialog)
                .setCancelable(false).build();




        Rview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int totalCount, pastItemCount, visibleItemCount;
                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    pastItemCount = layoutManager.findFirstVisibleItemPosition();
                    visibleItemCount = layoutManager.getChildCount();
                    if ((pastItemCount + visibleItemCount) >= totalCount) {
                        Log.d(TAG, "onScrolled: " + totalCount + " " + pastItemCount + " " + visibleItemCount);
                        if (!nextUrl.equals("null") && !isNextBusy)
                            getNextAdos();
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        }); */

   /*     spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if(spinner.getSelectedItem().toString().equals("Select District")){

                    }
                    else {
                        dialog.show();
                        Log.d(TAG, "onItemSelected: yoyo");
                        username.clear();
                        userinfo.clear();
                        mUserId.clear();
                        mPkList.clear();
                        mDdoNames.clear();
                        mDistrictNames.clear();
                        getadolist(spinner.getSelectedItem().toString());
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }); */
        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");

//        adolist.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL));
//        adolist.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
       /* float columnWidthDp = 85;
            DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
            float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
            int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); */

      /*  Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;
        int columns = Math.round(dpWidth/300); */

//        adolist.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL));
//        adolist.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        gridlayout=new GridLayoutManager(getActivity(),3);
        adolist.setLayoutManager(gridlayout);


        RequestQueue district_requestQueue= Volley.newRequestQueue(getActivity());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, district_list_url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for(int i=0;i<response.length();i++){
                    try {
                        JSONObject singleObject = response.getJSONObject(i);
                        if (singleObject.getString("district").equalsIgnoreCase("gurugram"))
                            continue;
                        mdistrictlist.add(singleObject.getString("district"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(mdistrictlist);

                customadapter = new RecyclerViewAdapter_district(getActivity(),mdistrictlist);
                adolist.setAdapter(customadapter);
                // loadSpinnerData();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(),"something went wrong",Toast.LENGTH_LONG).show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Token "+token);
                return map;
            }
        };

        district_requestQueue.add(jsonArrayRequest);





        return view;
    }



   /* void loadSpinnerData(){


        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(),  android.R.layout.simple_spinner_dropdown_item, mdistrictlist);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner_probar.setVisibility(View.GONE);
    }

    void getadolist(String district){

        ado_list="http://18.224.202.135/api/users-list/ado/?search="+district;

        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ado_list, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                view.setBackground(getResources().getDrawable(R.drawable.data_background));


                Log.d(TAG, "onResponse: sizes"+username.size()+userinfo.size());

                try {
                    JSONObject rootObject = new JSONObject(String.valueOf(response));
                    nextUrl = rootObject.getString("next");
                    Log.d(TAG, "onResponse: " + nextUrl);
                    JSONArray resultsArray = rootObject.getJSONArray("results");
                    if(resultsArray.length()== 0){
                        recyclerViewAdater.mShowShimmer = false;
                        recyclerViewAdater.notifyDataSetChanged();

                        view.setBackground(getActivity().getResources().getDrawable(R.mipmap.no_entry_background));
                        //view.getView().setBackground(getActivity().getResources().getDrawable(R.drawable.no_entry_background));
                    }
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject singleObject = resultsArray.getJSONObject(i);
                        username.add(singleObject.getString("name").toUpperCase());
                        JSONArray villageArray = singleObject.getJSONArray("village");
                        Log.d(TAG, "onResponse: LENGTH " + villageArray.length());
                        if (villageArray.length() == 0)
                            userinfo.add("NOT ASSIGNED");
                        for (int j = 0; j < 1; j++) {
                            try {
                                JSONObject villageObject = villageArray.getJSONObject(i);
                                userinfo.add(villageObject.getString("village").toUpperCase());
                            } catch (JSONException e) {
                                userinfo.add("NOT ASSIGNED");
                            }
                        }
                        JSONObject authObject = singleObject.getJSONObject("auth_user");
                        String pk = authObject.getString("pk");
                        mPkList.add(pk);
                        String id = singleObject.getString("id");
                        mUserId.add(id);
                        try {
                            JSONObject ddaObject = singleObject.getJSONObject("dda");
                            String ddaName = ddaObject.getString("name");
                            mDdoNames.add(ddaName);
                            try {
                                JSONObject districtObject = ddaObject.getJSONObject("district");
                                String districtName = districtObject.getString("district");
                                mDistrictNames.add(districtName.toUpperCase());
                            } catch (JSONException e) {
                                mDistrictNames.add("NOT ASSIGNED");
                            }
                        } catch (JSONException e) {
                            mDdoNames.add("Not Assigned");
                        }
                    }

                    recyclerViewAdater.mShowShimmer = false;
                    recyclerViewAdater.notifyDataSetChanged();
                    dialog.dismiss();

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: JSON" + e);
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getActivity(), "Check Your Internt Connection Please!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorResponse: " + error);
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

    private void getNextAdos() {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        isNextBusy = true;
        Log.d(TAG, "getNextAdos: count ");
        progressBar.setVisibility(View.VISIBLE);
        final JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, nextUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rootObject = new JSONObject(String.valueOf(response));
                    nextUrl = rootObject.getString("next");
                    JSONArray resultsArray = rootObject.getJSONArray("results");
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject singleObject = resultsArray.getJSONObject(i);
                        username.add(singleObject.getString("name").toUpperCase());
                        JSONArray villageArray = singleObject.getJSONArray("village");
                        Log.d(TAG, "onResponse: LENGTH " + villageArray.length());
                        if (villageArray.length() == 0)
                            userinfo.add("NOT ASSIGNED");
                        for (int j = 0; j < 1; j++) {
                            try {
                                JSONObject villageObject = villageArray.getJSONObject(i);
                                userinfo.add(villageObject.getString("village").toUpperCase());
                            } catch (JSONException e) {
                                userinfo.add("NOT ASSIGNED");
                            }
                        }
                        JSONObject authObject = singleObject.getJSONObject("auth_user");
                        String pk = authObject.getString("pk");
                        mPkList.add(pk);
                        String id = singleObject.getString("id");
                        mUserId.add(id);
                        try {
                            JSONObject ddaObject = singleObject.getJSONObject("dda");
                            String ddaName = ddaObject.getString("name");
                            mDdoNames.add(ddaName);
                            try {
                                JSONObject districtObject = ddaObject.getJSONObject("district");
                                String districtName = districtObject.getString("district");
                                mDistrictNames.add(districtName.toUpperCase());
                            } catch (JSONException e) {
                                mDistrictNames.add("NOT ASSIGNED");
                            }
                        } catch (JSONException e) {
                            mDdoNames.add("Not Assigned");
                        }
                    }
                    Log.d(TAG, "onResponse: " + username);
                    recyclerViewAdater.notifyDataSetChanged();
                    isNextBusy = false;

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError)
                    Toast.makeText(getActivity(), "Check Your Internt Connection Please!",
                            Toast.LENGTH_SHORT).show();
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

        requestQueue.add(jsonArrayRequest);
        requestFinished(requestQueue);
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

    }



    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                progressBar.setVisibility(View.GONE);
            }
        });

    } */

}
