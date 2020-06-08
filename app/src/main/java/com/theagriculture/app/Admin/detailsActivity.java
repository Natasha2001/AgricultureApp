package com.theagriculture.app.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.theagriculture.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class detailsActivity extends AppCompatActivity {

    private String ado_name;
    private String dda_name;
    private RelativeLayout parent;
    private TextView noDetails_text;
    private String id;
    private String urlado;
    private String urldda;
    private String ado_id;
    private String dda_id;

    private String name;
    private String number;
    private String email;
    private String TAG="Detail class";
    private String token;

    private TextView aname;
    private TextView aemail;
    private TextView anumber;

    private TextView bname;
    private TextView bemail;
    private TextView bnumber;

    private boolean both = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent intent = getIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("DETAILS");
        ado_name = intent.getStringExtra("ado_name");
        dda_name = intent.getStringExtra("dda_name");
        ado_id = intent.getStringExtra("ado_pk");
        dda_id = intent.getStringExtra("dda_pk");
        parent = findViewById(R.id.parent);
        noDetails_text = findViewById(R.id.nodetail_text);

        aname= findViewById(R.id.adodetail_name);
        aemail = findViewById(R.id.adodetail_email);
        anumber = findViewById(R.id.adodetail_phone);

        bname = findViewById(R.id.ddadetail_name);
        bemail = findViewById(R.id.ddadetail_email);
        bnumber = findViewById(R.id.ddadetail_phone);


        SharedPreferences prefs = getSharedPreferences("tokenFile", MODE_PRIVATE);
        token = prefs.getString("token", "");

        urlado = "http://18.224.202.135/api/user/" + ado_id + "/";
        urldda = "http://18.224.202.135/api/user/" + dda_id + "/";

        Log.d(TAG, "onCreate: "+ado_id);
        Log.d(TAG, "onCreate: "+dda_id);







        if(ado_name.equals("NOT ASSIGNED") && dda_name.equals("NOT ASSIGNED")){
            both = false;
            parent.setVisibility(View.GONE);
            noDetails_text.setVisibility(View.VISIBLE);
        }else if(ado_name.equals("NOT ASSIGNED") && !(dda_name.equals("NOT ASSIGNED"))){
            both = false;
            loadData(urldda,false);

        }else if(!(ado_name.equals("NOT ASSIGNED")) && dda_name.equals("NOT ASSIGNED")){
            both = false;
            loadData(urlado,true);
        }else if(!(ado_name.equals("NOT ASSIGNED")) && !(dda_name.equals("NOT ASSIGNED"))){
            both = true;
            loadData(urlado,true);
        }



    }

    private void loadData(String url , final boolean isado) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            name = rootObject.getString("name");
                            number = rootObject.getString("number");
                            email = rootObject.getString("email");

                            if(isado){

                                if (!name.isEmpty())
                                    aname.setText(name.toUpperCase());
                                else
                                    aname.setText("NONE");
                                if (!number.isEmpty())
                                    anumber.setText(number.toUpperCase());
                                else
                                    anumber.setText("NONE");
                                if (!email.isEmpty())
                                    aemail.setText(email.toUpperCase());
                                else
                                    aemail.setText("NONE");

                            }
                            else{
                                if (!name.isEmpty())
                                    bname.setText(name.toUpperCase());
                                else
                                    bname.setText("NONE");
                                if (!number.isEmpty())
                                    bnumber.setText(number.toUpperCase());
                                else
                                    bnumber.setText("NONE");
                                if (!email.isEmpty())
                                    bemail.setText(email.toUpperCase());
                                else
                                    bemail.setText("NONE");


                            }


                            //fetchSpinnerData(spinnerUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onResponse: JSON EXCEPTION: getDetails " + e);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: getDetails " + error);
                        if (error instanceof ClientError) {
                            if (isado) {

                                Toast.makeText(getApplicationContext(), "ado not assigned",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "dda not assigned",
                                        Toast.LENGTH_LONG).show();

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong, try again later!",
                                    Toast.LENGTH_LONG).show();


                        }


                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
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
        requestFinished(requestQueue);

    }

    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                if(both){
                    loadData(urldda,false);
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
