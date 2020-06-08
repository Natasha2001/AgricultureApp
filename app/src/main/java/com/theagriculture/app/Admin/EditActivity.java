package com.theagriculture.app.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class EditActivity extends AppCompatActivity {
    private static String TAG = "EditActivity";
    private EditText nameEditText;
    private EditText mobileEditText;
    private EditText emailEditText;
    private Button saveButton;
    private String token;
    private ArrayList<String> districtNames;
    private ArrayList<String> districtIds;
    private ArrayList<String> villageNames;
    private ArrayList<String> villageIds;
    private String place;
    private boolean isDdo;
    private Spinner spinner;
    private String spinnerUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getSupportActionBar().setTitle("Editing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nameEditText = findViewById(R.id.edit_name);
        mobileEditText = findViewById(R.id.edit_number);
        emailEditText = findViewById(R.id.edit_email);
        saveButton = findViewById(R.id.save_changes_button);
        //spinner = findViewById(R.id.edit_district);
        SharedPreferences prefs = getSharedPreferences("tokenFile", MODE_PRIVATE);
        token = prefs.getString("token", "");
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        Log.d(TAG, "onCreate: check id here"+ id);
        Log.d(TAG, "onCreate: gettheidhere"+id);
        isDdo = intent.getBooleanExtra("isDdo", false);
        //place = intent.getStringExtra("place");
        if (isDdo) {
            spinnerUrl = "http://18.224.202.135/api/district/";
        } else {
            spinnerUrl = "http://18.224.202.135/api/villages-list/";
        }
        Log.d(TAG, "onCreate: SPINNER URL " + spinnerUrl);
        //id = "2";
        final String url = "http://18.224.202.135/api/user/" + id + "/";
        getDetails(url);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = mobileEditText.getText().toString();
                String email = emailEditText.getText().toString();
                /*if (spinner.getSelectedItemPosition() == 0) {
                    String message;
                    if (isDdo)
                        message = "District";
                    else
                        message = "Village";
                    Toast.makeText(EditActivity.this, "Please Select a valid " + message,
                            Toast.LENGTH_SHORT).show();
                } else*/
                if (mobile.length() != 10) {
                    Toast.makeText(getApplicationContext(),"enter the valid mobile number",Toast.LENGTH_LONG).show();
                }else if(!isValidEmail(email)){
                    Toast.makeText(getApplicationContext(),"enter the valid email address",Toast.LENGTH_LONG).show();

                }
                else
                    saveChanges(url);
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void getDetails(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            String name = rootObject.getString("name");
                            String number = rootObject.getString("number");
                            String email = rootObject.getString("email");
                            nameEditText.setText(name);
                            mobileEditText.setText(number);
                            emailEditText.setText(email);
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
                        Toast.makeText(getApplicationContext(), "Something went wrong, try again later!",
                                Toast.LENGTH_LONG).show();
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
    }

    private void saveChanges(String url) {
        JSONObject params = new JSONObject();
        String name = nameEditText.getText().toString();
        String number = mobileEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        try {
            params.put("name", name);
            params.put("number", number);
            params.put("email", email);
//            int pos = spinner.getSelectedItemPosition();
            /*Log.d(TAG, "saveChanges: "+villageIds.get(pos));

            if (isDdo)
                params.put("district", districtIds.get(pos));

            else
                params.put("village", villageIds.get(pos));*/
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "saveChanges: PARAMS EXCEPTION saveChanges " + e);
        }
        Log.d(TAG, "saveChanges: params_value "+name+" "+number+" "+email+" ");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        Toast.makeText(getApplicationContext(), "Changes Saved Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: saveChanges " + error);
                        Toast.makeText(getApplicationContext(), "Something went wrong, try again later!"+error,
                                Toast.LENGTH_LONG).show();
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
    }

    private void fetchSpinnerData(String url) {
        districtIds = new ArrayList<>();
        districtNames = new ArrayList<>();
        villageIds = new ArrayList<>();
        villageNames = new ArrayList<>();
        villageNames.add("Select Village Name");
        villageIds.add("null");
        districtNames.add("Select District Name");
        districtIds.add("null");
       /* int startIndex = place.indexOf(",");
        int endIndex = place.length() - 1;
        String toBeReplaced = place.substring(startIndex, endIndex);
        final String extractedPlace = place.replace(toBeReplaced, "");*/
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            JSONArray resultsArray = rootObject.getJSONArray("results");
                            int pos = 0;
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                if (isDdo) {
                                    String district = singleObject.getString("district");
                                    String districtId = singleObject.getString("id");
                                    if (place.equals(district))
                                        pos = i;
                                    districtNames.add(district);
                                    districtIds.add(districtId);
                                } else {
                                    String village = singleObject.getString("village");
                                    String villageId = singleObject.getString("id");
                                    if (place.equals(village))
                                        pos = i;
                                    villageNames.add(village);
                                    villageIds.add(villageId);
                                }
                            }
                            if (isDdo) {
                                ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                        android.R.layout.simple_dropdown_item_1line, districtNames);
                                districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(districtAdapter);
                                spinner.setSelection(pos);
                            } else {
                                Log.d(TAG, "onResponse: ADO " + villageNames);
                                ArrayAdapter<String> villageAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                        android.R.layout.simple_dropdown_item_1line, villageNames);
                                villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(villageAdapter);
                                spinner.setSelection(pos);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onResponse: JSON EXCEPTION " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
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
}
