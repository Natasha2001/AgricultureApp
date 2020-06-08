package com.theagriculture.app.Admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.theagriculture.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;

public class DownloadReportActivity extends AppCompatActivity {

    private static final String TAG = "DownloadReportActivity";
    private Pattern pattern;
    private Matcher matcher;
    private String status = "";
    private Spinner spinner;
    private ProgressBar progressBar;
    private Button startDateEditText;
    private Button endDateEditText;
    private String mUrl = "http://18.224.202.135/api/district/";
    private ArrayList<String> mDistrictNames;
    private String token = "";
    private int startDay = -1;
    private int startMonth = -1;
    private int startYear = -1;
    private int endDay = -1;
    private int endMonth = -1;
    private int endYear = -1;
    private AlertDialog processingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_report);
        spinner = findViewById(R.id.district_spinner);
        progressBar = findViewById(R.id.district_progressbar);
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        Button downloadButton = findViewById(R.id.download_button);
        getSupportActionBar().setTitle("Download Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        startDay = i2;
                        startMonth = i1 + 1;
                        startYear = i;
                        startDateEditText.setText(i2 + "/" + (i1 + 1) + "/" + i);
                    }
                };
                DialogFragment datePickerFragment = new DatePickerFragment(dateSetListener);
                datePickerFragment.show(getSupportFragmentManager(), "Select Start Date");
            }
        });
        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        endDay = i2;
                        endMonth = i1 + 1;
                        endYear = i;
                        endDateEditText.setText(i2 + "/" + (i1 + 1) + "/" + i);
                    }
                };
                DialogFragment datePickerDialog = new DatePickerFragment(dateSetListener);
                datePickerDialog.show(getSupportFragmentManager(), "Select End Date");
            }
        });
        mDistrictNames = new ArrayList<>();
        mDistrictNames.add("Select District");
        mDistrictNames.add("ALL DISTRICTS");
        SharedPreferences preferences = getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        RequestQueue district_requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, mUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject singleObject = response.getJSONObject(i);
                                mDistrictNames.add(singleObject.getString("district").toUpperCase());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        loadSpinnerData();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "something went wrong", Toast.LENGTH_LONG).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        district_requestQueue.add(jsonArrayRequest);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processingDialog = new SpotsDialog.Builder().setMessage("Processing Request...")
                        .setContext(DownloadReportActivity.this)
                        .setTheme(R.style.CustomDialog)
                        .setCancelable(false)
                        .build();
                processingDialog.show();
                if (isValid())
                    downloadReport();
                else {
                    processingDialog.dismiss();
                }

            }
        });
    }

    public void onRadioButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.pending_radio:
                status = "pending";
                break;
            case R.id.ongoing_radio:
                status = "ongoing";
                break;
            case R.id.completed_radio:
                status = "completed";
                break;
        }
    }


    private void loadSpinnerData() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mDistrictNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void downloadReport() {
        String startDate = startYear + "-" + startMonth + "-" + startDay;
        String endDate = endYear + "-" + endMonth + "-" + endDay;
        String district = mDistrictNames.get(spinner.getSelectedItemPosition());
        String url = "http://18.224.202.135/api/generate-location-report/?start=" + startDate + "&end=" + endDate +
                "&status=" + status;
        if (!district.equalsIgnoreCase("All Districts"))
            url = url + "&district=" + district;
        Log.d(TAG, "downloadReport: URL " + url);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            String reportDownloadUrl = rootObject.getString("csvFile");
                            Uri uri = Uri.parse(reportDownloadUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        processingDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: " + error);
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getApplicationContext(),
                                    "Check your internet connection!", Toast.LENGTH_LONG).show();
                        else {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong, Please try again!", Toast.LENGTH_SHORT).show();
                        }
                        processingDialog.dismiss();
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
    }

    private boolean isValid() {
        if (status.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please select status for report",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(), "Please select a valid district",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (startDay == -1) {
            Toast.makeText(getApplicationContext(), "Please select a valid start date",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (endDay == -1) {
            Toast.makeText(getApplicationContext(), "Please select a valid end date",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (startDay > endDay && startMonth >= endMonth && startYear >= endYear) {
            showDialogBox("Notice", "Start Date cannot be greater than End Date",
                    "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, "", null, true);
            return false;
        }
        /*else if (startYear > endYear) {
            showDialogBox("Notice", "Start Year cannot be greater than End Year",
                    "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, "", null, true);
            return false;
        } else if (startMonth > endMonth) {
            showDialogBox("Notice", "Start Month cannot be greater than End Month",
                    "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, "", null, true);
            return false;
        } else if (startDay > endDay) {
            showDialogBox("Notice", "Start Day cannot be greater than End Day",
                    "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, "", null, true);
            return false;
        }*/
        return true;
    }

    private void showDialogBox(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnclick,
                               String negativeLabel, DialogInterface.OnClickListener negativeOnclick,
                               boolean isCancelable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnclick);
        builder.setNegativeButton(negativeLabel, negativeOnclick);
        builder.setCancelable(isCancelable);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
