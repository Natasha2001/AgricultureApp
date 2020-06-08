package com.theagriculture.app.Ado;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ReviewReport extends AppCompatActivity {
    private TextView fatherNameRight;
    private TextView fatherNameLeft;
    private TextView nameLeft;
    private TextView nameRight;
    private TextView villCodeRight;
    private TextView villCodeLeft;
    private TextView mobileLeft;
    private TextView mobileRight;
    //    private TextView khasraLeft;
//    private TextView khasraRight;
    private TextView ownLeaseLeft;
    private TextView ownLeaseRight;
    private TextView remarksLeft;
    private TextView remarksRight;
    private TextView reasonLeft;
    private TextView reasonRight;
    /*private LinearLayout statusRow;
    private TextView statusLeft;
    private TextView statusRight;*/
    private static String TAG = "ReviewReport";
    private TextView chalaanLeft;
    private ProgressBar progressBar;
    //private TableLayout tableLayout;
    private String mUrl;
    private boolean isComplete;
    private boolean isAdmin;
    private boolean isOngoing;
    private TextView chalaanRight;
    private RecyclerView recyclerView;
    private ReviewPicsRecyclerviewAdapter adapter;
    private ArrayList<String> mImagesUrl;
    private ArrayList<String> schemedata;
    private ArrayList<String> programNamedata;
    private ArrayList<String> financialYearNamedata;
    private ArrayList<String> dateOfBenefitdata;
    private TextView noSubsidiesTextView;

    private String farmerId;
    private String id;
    private boolean isDdo;
    private TextView villnameleft;
    private TextView villnameright;
    private TextView districtleft;
    private TextView districtright;
    private String urlfarmer = "https://agriharyana.org/api/farmer";

    private RecyclerView tableRecyclerView;
    private ReviewTableRecycleAdapter reviewTableRecycleAdapter;
    private TextView fireLeft;
    private TextView fireRight;
    private LinearLayout actionRow;
    private TextView actionLeft;
    private TextView actionRight;
    private LinearLayout amountRow;
    private TextView amountLeft;
    private TextView amountRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_report);
        //tableLayout = findViewById(R.id.table);
        getSupportActionBar().setTitle("Report Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fatherNameRight = findViewById(R.id.fatherNameRight);
        fatherNameLeft = findViewById(R.id.fatherNameLeft);
        nameLeft = findViewById(R.id.nameLeft);
        nameRight = findViewById(R.id.nameRight);
        villCodeRight = findViewById(R.id.villCodeRight);
        villCodeLeft = findViewById(R.id.villCodeLeft);
       // mobileLeft = findViewById(R.id.mobileLeft);
        //mobileRight = findViewById(R.id.mobileRight);
//        khasraLeft = findViewById(R.id.khasraLeft);
//        khasraRight = findViewById(R.id.khasraRight);
        ownLeaseLeft = findViewById(R.id.own_lease_Left);
        ownLeaseRight = findViewById(R.id.own_lease_Right);
        remarksLeft = findViewById(R.id.remarksLeft);
        remarksRight = findViewById(R.id.remarksRight);
        reasonLeft = findViewById(R.id.reasonLeft);
        reasonRight = findViewById(R.id.reasonRight);
        /*statusRow = findViewById(R.id.statusRow);
        statusLeft = findViewById(R.id.statusLeft);
        statusRight = findViewById(R.id.statusRight);*/
        progressBar = findViewById(R.id.progressBar);
        noSubsidiesTextView = findViewById(R.id.noSubsidies_textview);
        fireLeft = findViewById(R.id.fire_Left);
        fireRight = findViewById(R.id.fire_Right);
        actionRow = findViewById(R.id.action_row);
        actionLeft = findViewById(R.id.action_Left);
        actionRight = findViewById(R.id.action_Right);
        amountRow = findViewById(R.id.amount_row);
        amountLeft = findViewById(R.id.amount_Left);
        amountRight = findViewById(R.id.amount_Right);
        schemedata = new ArrayList<>();
        programNamedata = new ArrayList<>();
        financialYearNamedata = new ArrayList<>();
        dateOfBenefitdata = new ArrayList<>();
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        isDdo = intent.getBooleanExtra("isDdo", false);
        isComplete = intent.getBooleanExtra("isComplete", false);
        isAdmin = intent.getBooleanExtra("isAdmin", false);
        isOngoing = intent.getBooleanExtra("isOngoing", false);
        mUrl = "http://18.224.202.135/api/report-ado/" + id + "/";
        Log.d(TAG, "onCreate: URL " + mUrl);
        /*schemedata.add("State Scheme for Promotion of Cotton Cultivation in Haryana ");
        programNamedata.add("Pest Management Demonstration (IPM)");
        financialYearNamedata.add("2018-2019");
        dateOfBenefitdata.add("2018-11-05 11:51:53");

        schemedata.add("State Scheme for Promotion of Cotton Cultivation in Haryana ");
        programNamedata.add("Pest Management Demonstration (IPM)");
        financialYearNamedata.add("2018-2019");
        dateOfBenefitdata.add("2018-11-05 11:51:53");

        schemedata.add("State Scheme for Promotion of Cotton Cultivation in Haryana ");
        programNamedata.add("Pest Management Demonstration (IPM)");
        financialYearNamedata.add("2018-2019");
        dateOfBenefitdata.add("2018-11-05 11:51:53");*/

        /*actionLeft = findViewById(R.id.actionLeft);
        actionRight = findViewById(R.id.actionRight);
        chalaanLeft = findViewById(R.id.chalaan_amount_left);
        chalaanRight = findViewById(R.id.chalaan_amount_right);*/
        recyclerView = findViewById(R.id.review_pics_recyclerview);
        villnameleft = findViewById(R.id.villnameLeft);
        villnameright = findViewById(R.id.villnameRight);
        districtleft = findViewById(R.id.districtLeft);
        districtright = findViewById(R.id.districtRight);
        tableRecyclerView = findViewById(R.id.tableRecyclerView);
        tableRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (isAdmin || isDdo) {
            reviewTableRecycleAdapter = new ReviewTableRecycleAdapter(this, schemedata, programNamedata, financialYearNamedata, dateOfBenefitdata);
            tableRecyclerView.setAdapter(reviewTableRecycleAdapter);
        } else
            tableRecyclerView.setVisibility(View.GONE);
        mImagesUrl = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new ReviewPicsRecyclerviewAdapter(this, mImagesUrl);
        recyclerView.setAdapter(adapter);
        Button forfeitButton = findViewById(R.id.forfeit);
        Button startButton = findViewById(R.id.start);
//        addRowtoTable("scheme1", "program1", "financial1", "benefit1", true);
//        addRowtoTable("scheme1", "program1", "financial1", "benefit1", true);
//        addRowtoTable("scheme1", "program1", "financial1", "benefit1", true);
//        addRowtoTable("scheme1", "program1", "financial1", "benefit1", true);
        getDetails();
        if (isDdo) {
            if (isComplete)
                startButton.setVisibility(View.VISIBLE);
            else
                forfeitButton.setVisibility(View.VISIBLE);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionOnFarmerSub(0);
                }
            });

            forfeitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionOnFarmerSub(1);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void getDetails() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            String villCode = rootObject.getString("village_code");
                            farmerId = rootObject.getString("farmer_code");
                            Log.d(TAG, "onResponse: farmerid"+farmerId);
                            String name = rootObject.getString("farmer_name");
                            String fatherName = rootObject.getString("father_name");
                            String ownership = rootObject.getString("ownership");
                            //String action = rootObject.getString("action");

                            String remarks = rootObject.getString("remarks");
                            String reason = rootObject.getString("incident_reason");
                            JSONObject location = rootObject.getJSONObject("location");
                            String village_name = location.getString("village_name");
                            String district = location.getString("district");
                            String fire = rootObject.getString("fire");
                            boolean isChalaan, isFir;
                            String amount;
                            if (fire.equalsIgnoreCase("fire"))
                            {
                                isChalaan = rootObject.getBoolean("challan");
                                isFir = rootObject.getBoolean("fir");
                                amount = rootObject.getString("amount");
                                actionLeft.setText("Action");
                                String action = "";
                                if (isChalaan && isFir)
                                    action = "Chalaan And FIR";
                                else if (isChalaan)
                                    action = "Chalaan";
                                else if (isFir)
                                    action = "FIR";
                                actionRight.setText(capitalCase(action));
                                amountLeft.setText("Amount");
                                amountRight.setText(capitalCase(amount));
                                actionRow.setVisibility(View.VISIBLE);
                                amountRow.setVisibility(View.VISIBLE);
                            }
                            //String number = rootObject.getString("number");
                            JSONArray imagesArray = rootObject.getJSONArray("images");
                            for (int i = 0; i < imagesArray.length(); i++) {
                                JSONObject imageObject = imagesArray.getJSONObject(i);
                                String imageUrl = imageObject.getString("image");
                                mImagesUrl.add(imageUrl);
                                adapter.notifyDataSetChanged();
                            }
                            //Log.d(TAG, "onResponse: ACTION " + action);
                            fireLeft.setText("Fire");
                            fireRight.setText(capitalCase(fire));
                            villCodeLeft.setText("Village Code");
                            villCodeRight.setText(capitalCase(villCode));
                            villnameleft.setText("Village Name");
                            villnameright.setText(capitalCase(village_name));
                            districtleft.setText("District");
                            districtright.setText(capitalCase(district));
                            nameLeft.setText("Farmer Name");
                            nameRight.setText(capitalCase(name));
                            //khasraLeft.setText("Khasra Number");
                            //khasraRight.setText(khasraNo);
//                            khasraLeft.setVisibility(View.GONE);
//                            khasraRight.setVisibility(View.GONE);
                            fatherNameLeft.setText("Father Name");
                            fatherNameRight.setText(capitalCase(fatherName));
                            ownLeaseLeft.setText("Ownership/Lease");
                            ownLeaseRight.setText(capitalCase(ownership));
                            /*actionLeft.setText("Action Taken");
                            actionRight.setText(action);
                            if (action.equals("chalaan")) {
                                chalaanLeft.setText("Chalaan Amount");
                                String chalaanAmount = rootObject.getString("amount");
                                chalaanRight.setText(chalaanAmount);
                            } else {
                                chalaanLeft.setVisibility(View.GONE);
                                chalaanRight.setVisibility(View.GONE);
                            }*/
                            remarksLeft.setText("Remarks");
                            remarksRight.setText(remarks);
                            //mobileLeft.setText("Mobile No");
                            //mobileRight.setText(number);
                            //mobileLeft.setVisibility(View.GONE);
                            //mobileRight.setVisibility(View.GONE);
                            reasonLeft.setText("Incident Reason");
                            reasonRight.setText(reason);
                            if (isAdmin) {
//                                statusRow.setVisibility(View.VISIBLE);
                                /*JSONObject locationObject = rootObject.getJSONObject("location");
                                String status = locationObject.getString("status");*/
                                /*statusLeft.setText("ADO Status");
                                statusRight.setText(status);*/
                            }
                            progressBar.setVisibility(View.GONE);
                            if (isDdo || isAdmin) {
                                showSubsidies();
                                //addRowtoTable("aaaaaaa aa aaa aaa aaa aaa aaaa aaa aaa aaa aaa aaa aaa aaa ","   ssssssssss ss","djksjsjmsl","Per umanesimo si intende quel movimento culturale, ispirato da Francesco Petrarca e in parte da Giovanni Boccaccio, volto alla riscoperta dei classici latini e greci nella loro storicità e non più nella loro",true);
                                // tableLayout.setVisibility(View.VISIBLE);
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
                SharedPreferences prefs = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
                String token = prefs.getString("token", "");
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

    private void addRowtoTable(String col1, String col2, String col3, String col4, boolean isDataRow) {
        TableRow tableRow1 = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f);
        TableRow.LayoutParams params2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f);
        if (isDataRow) {
            params.setMargins(5, 0, 5, 5);
            params2.setMargins(0, 0, 5, 5);
        } else {
            params.setMargins(30, 0, 30, 5);
            params2.setMargins(30, 0, 30, 5);
        }
        tableRow1.setLayoutParams(params2);
        TextView colName1 = new TextView(this);
        colName1.setText(col1);
        colName1.setLayoutParams(params2);
        colName1.setGravity(Gravity.CENTER);
        TextView colName2 = new TextView(this);
        colName2.setText(col2);
        colName2.setLayoutParams(params2);
        colName2.setGravity(Gravity.CENTER);
        TextView colName3 = new TextView(this);
        colName3.setText(col3);
        colName3.setLayoutParams(params2);
        colName3.setGravity(Gravity.CENTER);
        TextView colName4 = new TextView(this);
        colName4.setText(col4);
        colName4.setLayoutParams(params2);
        colName4.setGravity(Gravity.CENTER);
        tableRow1.setWeightSum(4f);
        if (isDataRow) {
            colName1.setBackgroundColor(Color.parseColor("#ffffff"));
            colName1.setTextSize(16f);
            colName2.setBackgroundColor(Color.parseColor("#ffffff"));
            colName2.setTextSize(16f);
            colName3.setBackgroundColor(Color.parseColor("#ffffff"));
            colName3.setTextSize(16f);
            colName4.setBackgroundColor(Color.parseColor("#ffffff"));
            colName4.setTextSize(16f);
        } else {
            colName2.setTextSize(18f);
            colName1.setTextSize(18f);
            colName3.setTextSize(18f);
            colName4.setTextSize(18f);
            colName1.setTextColor(Color.parseColor("#000000"));
            colName2.setTextColor(Color.parseColor("#000000"));
            colName3.setTextColor(Color.parseColor("#000000"));
            colName4.setTextColor(Color.parseColor("#000000"));
//            colName1.setTypeface(Typeface.DEFAULT_BOLD);
//            colName2.setTypeface(Typeface.DEFAULT_BOLD);
//            colName3.setTypeface(Typeface.DEFAULT_BOLD);
//            colName4.setTypeface(Typeface.DEFAULT_BOLD);
        }
        tableRow1.addView(colName1);
        tableRow1.addView(colName2);
        tableRow1.addView(colName3);
        tableRow1.addView(colName4);
        //tableLayout.addView(tableRow1);
    }

    //flag = 1 for Add in BlockedList and 0 for remove from Blocked List
    private void actionOnFarmerSub(final int flag) {
        String url = "http://117.240.196.238:8080/api/CRM/setFarmerID?key=agriHr@CRM&fID="
                + farmerId + "&flag=" + flag;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject singleObject = new JSONObject(String.valueOf(response));
                            String message = singleObject.getString("message");
                            if (flag == 1)
                                changeStatus();
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onResponse: JSON blockFarmerSub " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getApplicationContext(), "Check your internet connection",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Something went wrong, " +
                                    "please try again!", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onErrorResponse: blockFarmerSub " + error);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
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

    private void changeStatus() {
        final JSONObject postbody = new JSONObject();
        try {
            postbody.put("status", "completed");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        String urlpatch = "http://18.224.202.135/api/location/" + id + "/";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, urlpatch, postbody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "Location assigned", Toast.LENGTH_SHORT).show();
                finish();
                try {
                    JSONObject c = new JSONObject(String.valueOf(response));
                    Log.d(TAG, "onResponse: " + c);
                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: " + e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error);
                Toast.makeText(getApplicationContext(), "Ado not assigned.Please try again", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                SharedPreferences preferences = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
                String token = preferences.getString("token", "");
                headers.put("Authorization", "Token " + token);
                return headers;
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

    private void showSubsidies() {
        String url = "https://agriharyana.org/api/farmer?idFarmer=" + farmerId;
        Log.d(TAG, "showSubsidies: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            String status = rootObject.getString("success");
                            Log.d(TAG, "onResponse: hereshowsub");
                            if (status.equals("1")) {
                                /*addRowtoTable("Scheme Name", "Program Name", "Financial Year Name",
                                        "Date Of Benefit", false);*/
                                schemedata.add("Scheme Name");
                                programNamedata.add("Program Name");
                                financialYearNamedata.add("Financial Year");
                                dateOfBenefitdata.add("Date Of Benefit");
                                JSONArray dataArray = rootObject.getJSONArray("data");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject singleObject = dataArray.getJSONObject(i);
                                    String col1 = singleObject.getString("schemeName");
                                    String col2 = singleObject.getString("programName");
                                    String col3 = singleObject.getString("financialYearName");
                                    String col4 = singleObject.getString("DateOfBenefit");
                                    schemedata.add(col1);
                                    programNamedata.add(col2);
                                    financialYearNamedata.add(col3);
                                    dateOfBenefitdata.add(col4);
                                    Log.d(TAG, "onResponse: showSubsidies " + singleObject);
                                }
                                reviewTableRecycleAdapter.notifyDataSetChanged();
                            } else {
                                tableRecyclerView.setVisibility(View.GONE);
                                noSubsidiesTextView.setVisibility(View.VISIBLE);
                                String message = rootObject.getString("message");
                                noSubsidiesTextView.setText(message);
                                //Toast.makeText(ReviewReport.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: " + error);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
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

    private String capitalCase(String str)
    {
        String newStr = str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
        return newStr;
    }
}
