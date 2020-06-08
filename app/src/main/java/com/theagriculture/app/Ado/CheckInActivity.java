package com.theagriculture.app.Ado;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.theagriculture.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;

import static com.theagriculture.app.AppNotificationChannels.CHANNEL_1_ID;

public class CheckInActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status> {

    private Button pickImageButton;
    private Button submitButton;
    private EditText villageEditText;
    private EditText farmerEditText;
    private EditText mobileEditText;
    private static int IMAGE_CAPTURE_RC = 191;
    private ArrayList<File> mImages;
    private ArrayList<String> mImagesPath;
    private ReportImageRecyAdapter adapter;
    private String reportSubmitUrl = "http://18.224.202.135/api/report-ado/add/";
    private String imageUploadUrl = "http://18.224.202.135/api/upload/images/";
    private String villageListUrl = "http://18.224.202.135/api/village/";
    private Intent intent;
    private int locationId;
    private String reportId;
    private String token;
    private AlertDialog reportSubmitLoading;
    private boolean isReportSubmitted = false;
    private String TAG = "CheckInActivity";
    private String farmerDetailsUrl = "http://117.240.196.238:8080/api/CRM/getFarmerDetail";
    public static  boolean isEntered = false;
    private EditText murrabbaEditText;
    private EditText killaEditText;
    private EditText remarksEditText;
    private EditText reasonEditText;

    //arraylist
    private ArrayList<String> farmerNames;
    private ArrayList<String> fatherNames;
    private ArrayList<String> farmerFatherNames;

    //spinner
    private Spinner vCodeSpinner;
    private ProgressBar vCodeProgressBar;
    private Spinner nameSpinner;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private ArrayList<String> villageNames;
    private String pk;
    private String actionTaken = "FIR";
    private ProgressBar nameProgressBar;
    private boolean isRequestFinished = false;
    private boolean isTextChanged = false;
    private boolean isBusy = false;
    private NotificationManagerCompat notificationManager;
    private File chalaanFile = null;
    private String imageFilePath;
    private LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private MarkerOptions Dlocation;
    private int photosUploadedCount = 0;
    private RecyclerView recyclerView;
    private ArrayList<String> villageCodes;
    private String destVillageName;
    private RelativeLayout villageNameOther;
    private EditText farmerNameEditText;
    private RelativeLayout farmerNameOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkin_form);
        intent = getIntent();
        Double latitude = intent.getDoubleExtra("lat",0);
        Double longitude = intent.getDoubleExtra("long",0);

        String id = intent.getStringExtra("id");
        locationId = Integer.parseInt(id);
        destVillageName = intent.getStringExtra("village_name");
        SharedPreferences prefs = getSharedPreferences("tokenFile", MODE_PRIVATE);
        pk = prefs.getString("pk", "");
        token = prefs.getString("token", "");
        getSupportActionBar().setTitle("Report Filing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "onCreate: TOKEN " + token);
        pickImageButton = findViewById(R.id.pick_photo);
        submitButton = findViewById(R.id.submit_report_ado);
        murrabbaEditText = findViewById(R.id.sname2);
        killaEditText = findViewById(R.id.sname3);
        remarksEditText = findViewById(R.id.sname4);
        reasonEditText = findViewById(R.id.sname5);
        recyclerView = findViewById(R.id.rvimages);
        villageNameOther = findViewById(R.id.villageNameOther);
        farmerEditText = findViewById(R.id.fname);
        farmerNameOther = findViewById(R.id.farmerNameOther);
        Dlocation = new MarkerOptions().position(new LatLng(30.76338, 76.7689826)).title("Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        buildGoogleApiClient();


        // reference to spinners
        vCodeSpinner = findViewById(R.id.vCodeSpinner);
        vCodeProgressBar = findViewById(R.id.vCodespinner_progressbar3);
        nameSpinner = findViewById(R.id.vCodeSpinner3);
        radioGroup = findViewById(R.id.radio_group);
        //fname = findViewById(R.id.sfname);

        //reference to toggle button
        notificationManager = NotificationManagerCompat.from(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mImages = new ArrayList<>();
        mImagesPath = new ArrayList<>();
        adapter = new ReportImageRecyAdapter(this, mImagesPath);
        recyclerView.setAdapter(adapter);
        fetchSpinnerData(villageListUrl);
        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openImagePicker();
                openCameraIntent();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: koko  "+isEntered);
                if(isEntered){
                    Log.d(TAG, "onClick: inside it damn!");

                    if (actionTaken.equals("Chalaan")) {
                        if (chalaanFile == null)
                            Toast.makeText(getApplicationContext(), "Please add a photo of" +
                                    " Chalaan", Toast.LENGTH_SHORT).show();
                        else if (mImages.isEmpty())
                            Toast.makeText(getApplicationContext(), "Please add atleast one picture of incident!",
                                    Toast.LENGTH_SHORT).show();
                        else {
                            showdialogbox("Sumbit Report", "Are you sure?", "Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (villageEditText.getText().toString().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please enter Village Code",
                                                        Toast.LENGTH_SHORT).show();
                                            else if (vCodeSpinner.getSelectedItemPosition() == 0 || farmerNames == null)
                                                Toast.makeText(getApplicationContext(), "Please select a farmer name and" +
                                                        " father name", Toast.LENGTH_LONG).show();
                                            else if (remarksEditText.getText().toString().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please fill Remarks",
                                                        Toast.LENGTH_SHORT).show();
                                            else if (reasonEditText.getText().toString().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please fill Incident " +
                                                        "Reason", Toast.LENGTH_SHORT).show();
                                            else
                                                submitReport();
                                        }
                                    }, "No",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    }, true);
                        }
                    } else if (mImages.isEmpty())
                        Toast.makeText(getApplicationContext(), "Please add atleast one picture of incident!",
                                Toast.LENGTH_SHORT).show();
                    else {
                        showdialogbox("Sumbit Report", "Are you sure?", "Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (villageEditText.getText().toString().isEmpty())
                                            Toast.makeText(getApplicationContext(), "Please enter Village Code",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (vCodeSpinner.getSelectedItemPosition() == 0 || farmerNames == null)
                                            Toast.makeText(getApplicationContext(), "Please select a farmer name and" +
                                                    " father name", Toast.LENGTH_LONG).show();
                                        else if (remarksEditText.getText().toString().isEmpty())
                                            Toast.makeText(getApplicationContext(), "Please fill Remarks",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (reasonEditText.getText().toString().isEmpty())
                                            Toast.makeText(getApplicationContext(), "Please fill Incident " +
                                                    "Reason", Toast.LENGTH_SHORT).show();
                                        else
                                            submitReport();
                                    }
                                }, "No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }, true);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"enter the location to submit the report",Toast.LENGTH_LONG).show();
                }

            }
        });

        villageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isTextChanged = true;
                isRequestFinished = false;
            }
        });

        vCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (vCodeSpinner.getSelectedItem().toString().equals("Other")) {
                    villageNameOther.setVisibility(View.VISIBLE);
                } else
                    villageNameOther.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (nameSpinner.getSelectedItem().toString().equals("Other")) {
                    farmerNameOther.setVisibility(View.VISIBLE);
                } else {
                    farmerNameOther.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        nameSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (vCodeSpinner.getSelectedItemPosition() == 0) {
                        Toast.makeText(getApplicationContext(), "Select a valid Village Name",
                                Toast.LENGTH_SHORT).show();
                    } else if (vCodeSpinner.getSelectedItem().toString().equals("Other")) {
                        farmerNameOther.setVisibility(View.VISIBLE);
                        farmerFatherNames = new ArrayList<>();
                        farmerFatherNames.add("Other");
                        ArrayAdapter<String> adaptername = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_dropdown_item_1line, farmerFatherNames);
                        adaptername.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        vCodeSpinner.setAdapter(adaptername);
                        vCodeSpinner.setSelection(0);

                    } else {
                        if (!isBusy && !isRequestFinished) {
                            vCodeProgressBar.setVisibility(View.VISIBLE);
                            getFarmerDetails(villageCodes.get(vCodeSpinner.getSelectedItemPosition()));
                        }
                    }
                    return true;
                }


                return false;
            }
        });

        /*action.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    actionTaken = action.getTextOn().toString();
                    chalaanEdittext.setVisibility(View.VISIBLE);
                    addChalaanButton.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onCheckedChanged: " + b + " " + action.getTextOn().toString());
                } else {
                    actionTaken = action.getTextOff().toString();
                    chalaanEdittext.setVisibility(View.GONE);
                    addChalaanButton.setVisibility(View.GONE);
                    chalaanImageView.setVisibility(View.GONE);
                    chalaanFile = null;
                    Log.d(TAG, "onCheckedChanged: " + b + " " + action.getTextOff().toString());
                }
            }
        });
*/
        /*addChalaanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = Environment.getExternalStorageDirectory();
                String start = file.getAbsolutePath();
                new ChooserDialog(CheckInActivity.this)
                        .withStartFile(start)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String s, File file) {
                                chalaanFile = file;
                                Glide.with(CheckInActivity.this)
                                        .load(file)
                                        .into(chalaanImageView);
                                chalaanImageView.setVisibility(View.VISIBLE);
                                Log.d(TAG, "onChoosePath: rectest" + mImages + mImagesPath);
                            }
                        })
                        .withOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                dialogInterface.cancel();
                            }
                        })
                        .build()
                        .show();
            }
        });*/

    }


    private void fetchSpinnerData(String url) {
        villageCodes = new ArrayList<>();
        villageNames = new ArrayList<>();
        villageNames.add("Select Village Name");
        villageCodes.add("null");
       /*int startIndex = place.indexOf(",");
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
                                String village = singleObject.getString("village");
                                String villageId = singleObject.getString("village_code");
                                if (destVillageName.equals(village))
                                    pos = i;
                                villageNames.add(village);
                                villageCodes.add(villageId);
                            }
                            Log.d(TAG, "onResponse: ADO " + villageNames);
                            villageNames.add("Other");
                            villageCodes.add("null");
                            ArrayAdapter<String> villageAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_dropdown_item_1line, villageNames);
                            villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            vCodeSpinner.setAdapter(villageAdapter);
                            vCodeSpinner.setSelection(pos);
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
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.d(TAG, "openCameraIntent: IOEXCEPTION PHOTOFILE: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            pictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(pictureIntent, IMAGE_CAPTURE_RC);
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_CAPTURE_RC) {
            if (resultCode == RESULT_OK) {
                mImagesPath.add(imageFilePath);
                File file = new File(imageFilePath);
                mImages.add(file);
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void submitReport() {
        reportSubmitLoading = new SpotsDialog.Builder().setContext(this).setMessage("Submitting Report")
                .setTheme(R.style.CustomDialog)
                .setCancelable(false)
                .build();
        reportSubmitLoading.show();
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioButtonId);
        String ownLease = radioButton.getText().toString().toLowerCase();
        if (!isReportSubmitted) {
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JSONObject postParams = new JSONObject();
            try {
                String remarks = remarksEditText.getText().toString();
                String incidentReason = reasonEditText.getText().toString();
                String farmername = farmerNames.get(vCodeSpinner.getSelectedItemPosition() - 1);
                String fathername = fatherNames.get(vCodeSpinner.getSelectedItemPosition() - 1);
                //String mobile = mobileEditText.getText().toString();

                postParams.put("farmer_name", farmername);
                postParams.put("father_name", fathername);
                postParams.put("farmer_code", "bfhsdf");
                postParams.put("village_code", "");
                postParams.put("ownership", ownLease);
                //postParams.put("action", actionTaken);
                postParams.put("location", String.valueOf(locationId));
                postParams.put("remarks", remarks);
                postParams.put("incident_reason", incidentReason);
                postParams.put("kila_num", "fgdfgfd");
                postParams.put("murrabba_num", "bdhfbh");
                postParams.put("longitude", "23.123");
                postParams.put("latitude", "42.123");
                postParams.put("action", "chalaan");
                //postParams.put("number", mobile);

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong, please try again!", Toast.LENGTH_SHORT).show();
                reportSubmitLoading.dismiss();
                Log.d(TAG, "submitReport: " + e);
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, reportSubmitUrl, postParams,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject singleObject = new JSONObject(String.valueOf(response));
                                reportId = singleObject.getString("id");
                                Log.d(TAG, "onResponse: " + singleObject);
                                isReportSubmitted = true;
                                uploadPhotos();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                reportSubmitLoading.dismiss();
                                Log.d(TAG, "jsonexception: reportSubmitRequest " + e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError)
                                Toast.makeText(getApplicationContext(), "Check your internet connection!", Toast.LENGTH_LONG).show();
                            else {
                                Toast.makeText(getApplicationContext(), "Something went wrong, Please try again!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onErrorResponse: reportSubmitRequest " + error.getStackTrace());
                            }
                            reportSubmitLoading.dismiss();
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
        } else
            uploadPhotos();
    }

    private void uploadPhotos() {
        if (actionTaken.equals("chalaan"))
            mImages.add(chalaanFile);
        final int progressMax = (int) mImages.get(photosUploadedCount).length() - photosUploadedCount;
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle("Uploading Photos")
                .setContentText("0/" + progressMax)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress((int) mImages.get(photosUploadedCount).length(), 0, false);
        notificationManager.notify(1, notificationBuilder.build());
        for (int pos = photosUploadedCount; pos < mImages.size(); pos++) {
            final int finalPos = pos;
            AndroidNetworking.upload(imageUploadUrl)
                    .addHeaders("Authorization", "Token " + token)
                    .addMultipartParameter("report", reportId)
                    .addMultipartFile("image", mImages.get(pos))
                    .setTag("Upload Images")
                    .setPriority(Priority.HIGH)
                    .build()
                    .setUploadProgressListener(new UploadProgressListener() {
                        @Override
                        public void onProgress(long bytesUploaded, long totalBytes) {
                            Log.d(TAG, "onProgress: " + bytesUploaded + "files uploaded: " + finalPos);
                            if (bytesUploaded == totalBytes) {
                                notificationBuilder.setProgress(0, 0, false)
                                        .setContentText((finalPos + 1) + "/" + mImages.size());
                                notificationManager.notify(1, notificationBuilder.build());
                            } else {
                                notificationBuilder.setProgress((int) totalBytes, (int) (bytesUploaded / totalBytes), false);
                                notificationManager.notify(1, notificationBuilder.build());
                            }
                        }
                    })
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "onResponse: " + response);
                            photosUploadedCount++;
                            if (finalPos == mImages.size() - 1) {
                                Toast.makeText(getApplicationContext(), "Photos Uploaded", Toast.LENGTH_SHORT).show();
                                notificationBuilder.setContentText("Upload Successful!")
                                        .setProgress(0, 0, false)
                                        .setOngoing(false);
                                notificationManager.notify(1, notificationBuilder.build());
                                Toast.makeText(getApplicationContext(), "Report Submitted Successfully", Toast.LENGTH_SHORT).show();
                                reportSubmitLoading.dismiss();
                                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                Intent intent = new Intent(CheckInActivity.this, com.theagriculture.app.login_activity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                notificationBuilder.setProgress(0, 0, false)
                                        .setContentText((finalPos + 1) + "/" + (int) mImages.get(finalPos + 1).length());
                                notificationManager.notify(1, notificationBuilder.build());
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.d(TAG, "onError: " + anError.getErrorBody());
                            notificationBuilder.setContentText("Upload Failed!")
                                    .setProgress(0, 0, false)
                                    .setOngoing(false);
                            notificationManager.notify(1, notificationBuilder.build());
                            reportSubmitLoading.dismiss();
                            Toast.makeText(getApplicationContext(), "Photos Upload failed, please try again", Toast.LENGTH_SHORT).show();
                        }

                        }
                    );

        }
    }

    private void getFarmerDetails(final String villCode) {
        isBusy = true;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        farmerNames = new ArrayList<>();
        fatherNames = new ArrayList<>();
        farmerFatherNames = new ArrayList<>();
        String finalUrl = farmerDetailsUrl + "?key=agriHr@CRM&vCode=" + villCode;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, finalUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            JSONArray dataArray = rootObject.getJSONArray("data");
                            Log.d(TAG, "onResponse: DATAARRAY " + dataArray.length());
                            farmerFatherNames.add("Farmer Name, Father Name");
                            farmerNames.add("null");
                            farmerFatherNames.add("null");
                            /*if (dataArray.length() == 0) {
                                Toast toast = Toast.makeText(CheckInActivity.this, "No Data Found for Village Code "
                                        + villCode, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                isBusy = false;
                            }*/
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject singleObject = dataArray.getJSONObject(i);
                                String farmerId = singleObject.getString("idFarmer");
                                String farmerName = singleObject.getString("FarmerName");
                                farmerNames.add(farmerName);
                                String fatherName = singleObject.getString("father_name");
                                fatherNames.add(fatherName);
                                farmerFatherNames.add(farmerName + ", " + fatherName);
                            }
                            String message = rootObject.getString("message");
                            if (farmerNames.size() == 0)
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            else {
                                ArrayAdapter<String> adaptername = new ArrayAdapter<>(getApplicationContext(),
                                        android.R.layout.simple_dropdown_item_1line, farmerFatherNames);
                                adaptername.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                nameSpinner.setAdapter(adaptername);
//                                ArrayAdapter<String> adapterfname = new ArrayAdapter<>(getApplicationContext(),
//                                        android.R.layout.simple_dropdown_item_1line, fatherNames);
//                                adapterfname.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                //fname.setAdapter(adapterfname);
                            }
                            vCodeProgressBar.setVisibility(View.GONE);
                            isRequestFinished = true;
                            isBusy = false;
                            Log.d(TAG, "onResponse: FARMER" + farmerNames.size());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            nameProgressBar.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: FARMER " + error.networkResponse + "  " + error);
                        isBusy = false;
                        if (error instanceof ClientError) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Invalid Village Code", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        nameProgressBar.setVisibility(View.GONE);
                    }
                }) {
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void getVillageName() {
        String url = "";
        villageNames = new ArrayList<>();
        if (!pk.equals("")) {
            url = "http://18.224.202.135/api/user/" + pk + "/";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject rootObject = new JSONObject(String.valueOf(response));
                                JSONObject villageObject = rootObject.getJSONObject("village");
                                String village = villageObject.getString("village");
                                villageNames.add(village);
                                ArrayAdapter<String> vCodeSpinnerAdapter = new ArrayAdapter<String>(CheckInActivity.this,
                                        android.R.layout.simple_dropdown_item_1line, villageNames);
                                vCodeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                vCodeSpinner.setAdapter(vCodeSpinnerAdapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d(TAG, "onResponse: getVillageCode JSON " + e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse: getVillageCode " + error);
                            if (error instanceof NoConnectionError)
                                Toast.makeText(getApplicationContext(), "Please Check your" +
                                        " internet connection!", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getApplicationContext(), "Something went wrong, Please try again!",
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
        }
    }

    private AlertDialog showdialogbox(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnclick,
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
        return alert;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        startgeofence(Dlocation);


    }
    GeofencingRequest geofencingRequest;

    private void startgeofence(MarkerOptions dlocation) {
        if (dlocation != null) {
            Geofence geofence = creategeofence(dlocation.getPosition(), 400f);
            geofencingRequest = creategeofencerequest(geofence);
            addgeofence(geofence);
        }
    }

    private Geofence creategeofence(LatLng position, float v) {
        return new Geofence.Builder().setRequestId("My Request")
                .setCircularRegion(position.latitude, position.longitude, v)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

    }

    private GeofencingRequest creategeofencerequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    private void addgeofence(Geofence geofence) {
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, creategeofencePendingIntent())
                .setResultCallback(this);
    }

    PendingIntent geofencePendingIntent;

    private PendingIntent creategeofencePendingIntent() {

        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionService2.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void getStatus(Boolean status){
        Log.d("getStatus2", "getStatus: comehere"+ status);
        isEntered = status;
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.d(TAG, "onResult: "+status);

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}

