package com.theagriculture.app.Ado;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
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
import id.zelory.compressor.Compressor;

import static com.theagriculture.app.AppNotificationChannels.CHANNEL_1_ID;

public class CheckInActivity2 extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        android.location.LocationListener,
        ResultCallback<Status> {
    public static boolean isEntered = false;
    private static String TAG = "CheckInActivity2";
    private static int IMAGE_CAPTURE_RC = 123;
    GoogleApiClient mGoogleApiClient;
    GeofencingRequest geofencingRequest;
    PendingIntent geofencePendingIntent;
    private Spinner villageNameSpinner;
    private EditText villageNameEditText;
    private RelativeLayout villageNameOther;
    private Spinner farmerfatherSpinner;
    private EditText farmerfatherEditText;
    private EditText fatherNameEditText;
    private RelativeLayout farmerNameOther;
    private RelativeLayout fatherNameOther;
    private ProgressBar nameProgressBar;
    private EditText murrabbaEditText;
    private EditText killaEditText;
    private RadioGroup radioGroup;
    private EditText remarksEditText;
    private EditText reasonEditText;
    private RecyclerView recyclerView;
    private ReportImageRecyAdapter adapter;
    private Button pickPhotoButton;
    private Button submitReportButton;
    private RadioGroup isFireRadioGroup;
    private EditText amountEditText;
    private String fireParam = "";
    private String reportSubmitUrl = "http://18.224.202.135/api/report-ado/add/";
    private String imageUploadUrl = "http://18.224.202.135/api/upload/images/";
    private String villageListUrl = "http://18.224.202.135/api/user/";
    private String farmerDetailsUrl = "http://117.240.196.238:8080/api/CRM/getFarmerDetail";
    private ArrayList<String> mImagesPath;
    private ArrayList<File> mImages;
    private ArrayList<String> villageCodes;
    private ArrayList<String> villageNames;
    private ArrayList<String> farmerNames;
    private ArrayList<String> farmerIds;
    private ArrayList<String> fatherNames;
    private ArrayList<String> farmerFatherNames;
    private LocationRequest mLocationRequest;
    private MarkerOptions Dlocation;
    private String locationId;
    private String destVillageName;
    private String pk;
    private String token;
    private Double latitude;
    private Double longitude;
    private boolean isBusy = false;
    private boolean isRequestFinished = false;
    private boolean isNameAdapterSet = false;
    private AlertDialog reportSubmitLoading;
    private String imageFilePath;
    private String reportId;
    private boolean isReportSubmitted = false;
    private int photosUploadedCount = 0;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private Context loctionContext;
    private boolean isFirstPic = true;
    private RelativeLayout fineLayout;
    private boolean isChalaan = false;
    private boolean isFir = false;
    private LocationManager locationManager;
    private String lat;
    private String lon;

    public static void getStatus(Boolean status) {
        Log.d("getStatus2", "getStatus: comehere" + status);
        isEntered = status;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkin_form);
        villageNameSpinner = findViewById(R.id.vCodeSpinner);
        villageNameEditText = findViewById(R.id.sname);
        villageNameOther = findViewById(R.id.villageNameOther);
        farmerfatherSpinner = findViewById(R.id.vCodeSpinner3);
        farmerfatherEditText = findViewById(R.id.fname);
        fatherNameEditText = findViewById(R.id.fathername);
        farmerNameOther = findViewById(R.id.farmerNameOther);
        fatherNameOther = findViewById(R.id.fatherNameOther);
        nameProgressBar = findViewById(R.id.vCodespinner_progressbar3);
        murrabbaEditText = findViewById(R.id.sname2);
        killaEditText = findViewById(R.id.sname3);
        radioGroup = findViewById(R.id.radio_group);
        remarksEditText = findViewById(R.id.sname4);
        reasonEditText = findViewById(R.id.sname5);
        recyclerView = findViewById(R.id.rvimages);
        pickPhotoButton = findViewById(R.id.pick_photo);
        submitReportButton = findViewById(R.id.submit_report_ado);
        isFireRadioGroup = findViewById(R.id.radio_group2);
        amountEditText = findViewById(R.id.amount_edittext);
        fineLayout = findViewById(R.id.fine_layout);
        notificationManager = NotificationManagerCompat.from(this);
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("lat", 0);
        longitude = intent.getDoubleExtra("long", 0);
        Dlocation = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        String id = intent.getStringExtra("id");
        locationId = id;
        Log.d(TAG, "onCreate: LOCATION ID " + id);
        destVillageName = intent.getStringExtra("village_name");
        SharedPreferences prefs = getSharedPreferences("tokenFile", MODE_PRIVATE);
        pk = prefs.getString("pk", "");
        token = prefs.getString("token", "");
        getSupportActionBar().setTitle("Report Filing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImagesPath = new ArrayList<>();
        mImages = new ArrayList<>();
        villageCodes = new ArrayList<>();
        villageNames = new ArrayList<>();
        adapter = new ReportImageRecyAdapter(this, mImagesPath);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
        villageListUrl = villageListUrl + pk;
        Log.d(TAG, "onCreate: VILLAGE LIST URL " + villageListUrl);
        fetchVillageNames(villageListUrl);
        villageNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (villageNameSpinner.getSelectedItem().toString().equals("Other"))
                    villageNameOther.setVisibility(View.VISIBLE);
                else
                    villageNameOther.setVisibility(View.GONE);
                isRequestFinished = false;
                isNameAdapterSet = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        farmerfatherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (farmerfatherSpinner.getSelectedItem().toString().equals("Other")) {
                    farmerNameOther.setVisibility(View.VISIBLE);
                    fatherNameOther.setVisibility(View.VISIBLE);
                } else {
                    farmerNameOther.setVisibility(View.GONE);
                    fatherNameOther.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        farmerfatherSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (villageNameSpinner.getSelectedItemPosition() == 0 && villageNameSpinner.getCount() == 2) {
                        Toast.makeText(getApplicationContext(), "Select a valid Village Name",
                                Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (villageNameSpinner.getSelectedItem().toString().equals("Other") && !isNameAdapterSet) {
                        nameProgressBar.setVisibility(View.VISIBLE);
                        Log.d(TAG, "onTouch: 2ND CONDITION ");
                        farmerNameOther.setVisibility(View.VISIBLE);
                        fatherNameOther.setVisibility(View.VISIBLE);
                        farmerFatherNames = new ArrayList<>();
                        farmerNames = new ArrayList<>();
                        farmerFatherNames.add("Farmer Name, Father Name");
                        farmerFatherNames.add("Other");
                        ArrayAdapter<String> adaptername = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_dropdown_item_1line, farmerFatherNames);
                        adaptername.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        farmerfatherSpinner.setAdapter(adaptername);
                        farmerfatherSpinner.setSelection(1);
                        isNameAdapterSet = true;
                        nameProgressBar.setVisibility(View.GONE);
                        return true;
                    } else {
                        if (!isBusy && !isRequestFinished) {
                            if (!villageNameSpinner.getSelectedItem().toString().equals("Other")) {
                                nameProgressBar.setVisibility(View.VISIBLE);
                                getFarmerDetails(villageCodes.get(villageNameSpinner.getSelectedItemPosition()));
                            } else {
                                isRequestFinished = true;
                            }
                            /*else {
                                farmerFatherNames = new ArrayList<>();

                                farmerFatherNames.add("Other");
                                ArrayAdapter<String> adaptername = new ArrayAdapter<>(getApplicationContext(),
                                        android.R.layout.simple_dropdown_item_1line, farmerFatherNames);
                                adaptername.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                farmerfatherSpinner.setAdapter(adaptername);
                                farmerfatherSpinner.setSelection(0);
                                nameProgressBar.setVisibility(View.GONE);

                            }*/
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        pickPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFirstPic) {
                    showdialogbox("Attention", "Only 4 Photos are allowed to be taken", "Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    openCameraIntent();
                                }
                            }, "", null, true);
                    isFirstPic = false;
                } else if (mImages.size() < 4)
                    openCameraIntent();
                else
                    Toast.makeText(getApplicationContext(), "Max Photos Reached...",
                            Toast.LENGTH_SHORT).show();
            }
        });

        submitReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEntered) {
                    Log.d(TAG, "onClick: inside it damn!");

//                    if (actionTaken.equals("Chalaan")) {
//                        if (chalaanFile == null)
//                            Toast.makeText(CheckInActivity2.this, "Please add a photo of" +
//                                    " Chalaan", Toast.LENGTH_SHORT).show();
                    if (mImages.isEmpty())
                        Toast.makeText(getApplicationContext(), "Please add atleast one picture of incident!",
                                Toast.LENGTH_SHORT).show();
                    else {
                        showdialogbox("Sumbit Report", "Are you sure?", "Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.d(TAG, "onClick: YES " + farmerfatherSpinner.getSelectedItemPosition()
                                                + "     " + farmerfatherSpinner.getSelectedItem().toString() + "    "
                                                + (farmerNames == null) + "     " + farmerFatherNames.size());
                                        if (villageNameSpinner.getSelectedItemPosition() == 0 && villageNames.size() == 2)
                                            Toast.makeText(getApplicationContext(), "Please select a Valid Village Name",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (farmerfatherSpinner.getSelectedItemPosition() == 0
                                                || farmerNames == null)
                                            Toast.makeText(getApplicationContext(), "Please select a farmer and father name",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (farmerfatherSpinner.getSelectedItem().toString().equals("Other")) {
                                            if (farmerfatherEditText.getText().toString().trim().isEmpty()
                                                    || fatherNameEditText.getText().toString().trim().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please select a farmer name",
                                                        Toast.LENGTH_LONG).show();
                                            if (fatherNameEditText.getText().toString().trim().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please select a father name",
                                                        Toast.LENGTH_SHORT).show();
                                            if (remarksEditText.getText().toString().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please fill Remarks",
                                                        Toast.LENGTH_SHORT).show();
                                            else if (reasonEditText.getText().toString().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please fill Incident " +
                                                        "Reason", Toast.LENGTH_SHORT).show();
                                            else if (murrabbaEditText.getText().toString().trim().isEmpty())
                                                Toast.makeText(getApplicationContext(), "Please fill Murrabba Number",
                                                        Toast.LENGTH_SHORT).show();
                                            else if (fireParam.equals(""))
                                                Toast.makeText(getApplicationContext(), "Please select an option " +
                                                        "Fire or No Fire", Toast.LENGTH_SHORT).show();
                                            else if (fireParam.equalsIgnoreCase("fire")) {
                                                if (amountEditText.getText().toString().isEmpty())
                                                    Toast.makeText(getApplicationContext(), "Please enter an amount"
                                                            , Toast.LENGTH_SHORT).show();
                                                else if (!isChalaan && !isFir)
                                                    Toast.makeText(getApplicationContext(), "Please select an action" +
                                                            "Chalaan/FIR", Toast.LENGTH_SHORT).show();
                                                else
                                                    submitReport();
                                            } else
                                                submitReport();
                                        }
                                        else if (remarksEditText.getText().toString().isEmpty())
                                            Toast.makeText(getApplicationContext(), "Please fill Remarks",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (reasonEditText.getText().toString().isEmpty())
                                            Toast.makeText(getApplicationContext(), "Please fill Incident " +
                                                    "Reason", Toast.LENGTH_SHORT).show();
                                        else if (murrabbaEditText.getText().toString().trim().isEmpty())
                                            Toast.makeText(getApplicationContext(), "Please fill Murrabba Number",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (fireParam.equals(""))
                                            Toast.makeText(getApplicationContext(), "Please select an option " +
                                                    "Fire or No Fire", Toast.LENGTH_SHORT).show();
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
//                    } else if (mImages.isEmpty())
//                        Toast.makeText(CheckInActivity.this, "Please add atleast one picture of incident!",
//                                Toast.LENGTH_SHORT).show();
                    /*else {
                        showdialogbox("Sumbit Report", "Are you sure?", "Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (villageEditText.getText().toString().isEmpty())
                                            Toast.makeText(CheckInActivity.this, "Please enter Village Code",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (vCodeSpinner.getSelectedItemPosition() == 0 || farmerNames == null)
                                            Toast.makeText(CheckInActivity.this, "Please select a farmer name and" +
                                                    " father name", Toast.LENGTH_LONG).show();
                                        else if (remarksEditText.getText().toString().isEmpty())
                                            Toast.makeText(CheckInActivity.this, "Please fill Remarks",
                                                    Toast.LENGTH_SHORT).show();
                                        else if (reasonEditText.getText().toString().isEmpty())
                                            Toast.makeText(CheckInActivity.this, "Please fill Incident " +
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
                    }*/
                } else {
                    Toast.makeText(getApplicationContext(), "enter the location to submit the report", Toast.LENGTH_LONG).show();
                }
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_RC) {
            if (resultCode == RESULT_OK) {
                File file = new File(imageFilePath);
                try {
                    mImagesPath.add(imageFilePath);
                    File compressedFile = new Compressor(CheckInActivity2.this).compressToFile(file);
                    mImages.add(compressedFile);
                    adapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Unable to load Image, please try again!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fetchVillageNames(String url) {
        villageCodes = new ArrayList<>();
        villageNames = new ArrayList<>();
        villageNames.add("Select Village Name");
        villageCodes.add("null");
       /*int startIndex = place.indexOf(",");
        int endIndex = place.length() - 1;
        String toBeReplaced = place.substring(startIndex, endIndex);
        final String extractedPlace = place.replace(toBeReplaced, "");*/
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            JSONArray resultsArray = rootObject.getJSONArray("village");
                            int pos = 0;
                            boolean isEqual = false;
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject singleObject = resultsArray.getJSONObject(i);
                                String village = singleObject.getString("village");
                                String villageId = singleObject.getString("village_code");
                                Log.d(TAG, "onResponse: current " + village + "     " + destVillageName);
                                String tempDest = destVillageName.toUpperCase();
                                String tempVillageName = village.toUpperCase();
                                if (tempDest.contains(tempVillageName)) {
                                    pos = i;
                                    isEqual = true;
                                }
                                villageNames.add(village);
                                villageCodes.add(villageId);
                            }
                            Log.d(TAG, "onResponse: fetchVillageNames " + villageNames);
                            villageNames.add("Other");
                            villageCodes.add("null");
                            ArrayAdapter<String> villageAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_dropdown_item_1line, villageNames);
                            villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            villageNameSpinner.setAdapter(villageAdapter);
                            if (isEqual)
                                villageNameSpinner.setSelection(pos + 1);
                            else
                                villageNameSpinner.setSelection(pos);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onResponse: fetchVillageNames JSON EXCEPTION " + e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getApplicationContext(), "Please Check your Internet Connection",
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again!",
                                    Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse: fetchVillageNames " + error);
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

    private void getFarmerDetails(final String villCode) {
        isBusy = true;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        farmerNames = new ArrayList<>();
        fatherNames = new ArrayList<>();
        farmerIds = new ArrayList<>();
        farmerFatherNames = new ArrayList<>();
        String finalUrl = farmerDetailsUrl + "?key=agriHr@CRM&vCode=" + villCode;
        Log.d(TAG, "getFarmerDetails: finalUrl " + finalUrl);
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
                            fatherNames.add("null");
                            farmerIds.add("null");
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
                                farmerIds.add(farmerId);
                                String farmerName = singleObject.getString("FarmerName");
                                farmerNames.add(farmerName);
                                String fatherName = singleObject.getString("father_name");
                                fatherNames.add(fatherName);
                                farmerFatherNames.add(farmerName + ", " + fatherName);
                            }
                            String message = rootObject.getString("message");
                            if (farmerNames.size() == 1) {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                farmerFatherNames.add("Other");
                                ArrayAdapter<String> adaptername = new ArrayAdapter<>(getApplicationContext(),
                                        android.R.layout.simple_dropdown_item_1line, farmerFatherNames);
                                adaptername.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                farmerfatherSpinner.setAdapter(adaptername);
                                farmerfatherSpinner.setSelection(1);
                                farmerNameOther.setVisibility(View.VISIBLE);
                                fatherNameOther.setVisibility(View.VISIBLE);
                            } else {
                                farmerFatherNames.add("Other");
                                ArrayAdapter<String> adaptername = new ArrayAdapter<>(getApplicationContext(),
                                        android.R.layout.simple_dropdown_item_1line, farmerFatherNames);
                                adaptername.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                farmerfatherSpinner.setAdapter(adaptername);
                            }
//                                ArrayAdapter<String> adapterfname = new ArrayAdapter<>(getApplicationContext(),
//                                        android.R.layout.simple_dropdown_item_1line, fatherNames);
//                                adapterfname.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //fname.setAdapter(adapterfname);
                            nameProgressBar.setVisibility(View.GONE);
                            isRequestFinished = true;
                            isBusy = false;
                            Log.d(TAG, "onResponse: FARMER getFarmerDetails " + farmerNames.size());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "onResponse: getFarmerDetails " + e);
                            nameProgressBar.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: FARMER " + error.networkResponse + "  " + error);
                        isBusy = false;
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getApplicationContext(), "Please Check your Internet Connection",
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again!",
                                    Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse: getFarmerDetails " + error);
                        nameProgressBar.setVisibility(View.GONE);
                    }
                }) {
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

    private void submitReport() {
        reportSubmitLoading = new SpotsDialog.Builder().setContext(this).setMessage("Submitting Report")
                .setTheme(R.style.CustomDialog)
                .setCancelable(false)
                .build();
        reportSubmitLoading.show();
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(radioButtonId);
        String ownLease = radioButton.getText().toString().toLowerCase();
        if (!isReportSubmitted) {
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JSONObject postParams = new JSONObject();
            try {
                String remarks = remarksEditText.getText().toString();
                String incidentReason = reasonEditText.getText().toString();
                String villageCode, farmerName, fatherName;
                if (villageNameSpinner.getSelectedItem().toString().equals("Other")) {
                    villageCode = villageNameEditText.getText().toString();

                } else {
                    villageCode = villageCodes.get(villageNameSpinner.getSelectedItemPosition());
                }

                if (farmerfatherSpinner.getSelectedItem().toString().equals("Other")) {
                    farmerName = farmerfatherEditText.getText().toString();
                    fatherName = fatherNameEditText.getText().toString();
                    postParams.put("farmer_code", "No Village Code was available");
                } else {
                    farmerName = farmerNames.get(villageNameSpinner.getSelectedItemPosition());
                    fatherName = fatherNames.get(villageNameSpinner.getSelectedItemPosition());
                    postParams.put("farmer_code", farmerIds.get(farmerfatherSpinner.getSelectedItemPosition()));
                }
                //String mobile = mobileEditText.getText().toString();
                String kilaNum = killaEditText.getText().toString();
                String murrabbbaNum = murrabbaEditText.getText().toString();
                postParams.put("farmer_name", farmerName);
                postParams.put("father_name", fatherName);
                postParams.put("village_code", villageCode);
                postParams.put("ownership", ownLease);
                //postParams.put("action", actionTaken);
                postParams.put("location", String.valueOf(locationId));
                postParams.put("remarks", remarks);
                postParams.put("incident_reason", incidentReason);
                postParams.put("kila_num", kilaNum);
                postParams.put("murrabba_num", murrabbbaNum);
                postParams.put("longitude", longitude);
                postParams.put("latitude", latitude);
                postParams.put("fire", fireParam);
                postParams.put("report_latitude",lat);
                postParams.put("report_longitude",lon);
                if (fireParam.equalsIgnoreCase("fire")) {
                    postParams.put("fir", isFir);
                    postParams.put("challan", isChalaan);
                    postParams.put("amount", amountEditText.getText().toString());
                }
                Log.d(TAG, "submitReport: PARAMS " + postParams);
                //postParams.put("action", "chalaan");
                //postParams.put("number", mobile);

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong, please try again!",
                        Toast.LENGTH_LONG).show();
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
                                Toast.makeText(getApplicationContext(), "Check your internet connection!",
                                        Toast.LENGTH_LONG).show();
                            else {
                                Toast.makeText(getApplicationContext(), "Something went wrong, Please try again!",
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, "onErrorResponse: reportSubmitRequest " + error);
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
        } else
            uploadPhotos();
    }

    private void uploadPhotos() {
//        if (actionTaken.equals("chalaan"))
//            mImages.add(chalaanFile);
        final int progressMax = mImages.size() - photosUploadedCount;
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle("Uploading Photos")
                .setContentText("0/" + progressMax)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress((int) mImages.get(photosUploadedCount).length(), 0, false);
        notificationManager.notify(1, notificationBuilder.build());
//        for (int pos = photosUploadedCount; pos < mImages.size(); pos++) {
//            final int finalPos = pos;
        uploadingPhotos();

//        }
    }

    private void uploadingPhotos() {
        AndroidNetworking.upload(imageUploadUrl)
                .addHeaders("Authorization", "Token " + token)
                .addMultipartParameter("report", reportId)
                .addMultipartFile("image", mImages.get(photosUploadedCount))
                .setTag("Upload Images")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        Log.d(TAG, "onProgress: " + bytesUploaded + "files uploaded: " + photosUploadedCount);
                        if (bytesUploaded == totalBytes) {
                            notificationBuilder.setProgress(0, 0, false)
                                    .setContentText((photosUploadedCount + 1) + "/" + mImages.size());
                            notificationManager.notify(1, notificationBuilder.build());
                        } else {
                            notificationBuilder.setProgress((int) totalBytes, (int) (bytesUploaded / totalBytes), false)
                                    .setContentText(photosUploadedCount + "/" + mImages.size());
                            notificationManager.notify(1, notificationBuilder.build());
                        }
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                                     @Override
                                     public void onResponse(JSONObject response) {
                                         Log.d(TAG, "onResponse: " + response);
                                         photosUploadedCount++;
                                         if (photosUploadedCount == mImages.size()) {
                                             Toast.makeText(getApplicationContext(), "Photos Uploaded", Toast.LENGTH_SHORT).show();
                                             notificationBuilder.setContentText("Upload Successful!")
                                                     .setProgress(0, 0, false)
                                                     .setOngoing(false);
                                             notificationManager.notify(1, notificationBuilder.build());
                                             Toast.makeText(getApplicationContext(), "Report Submitted Successfully", Toast.LENGTH_SHORT).show();
                                             reportSubmitLoading.dismiss();
                                             getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                             Intent intent = new Intent(CheckInActivity2.this, AdoActivity.class);
                                             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                             intent.putExtra("isReportSubmitted", true);
                                             startActivity(intent);
                                             finish();
                                         } else {
                                             notificationBuilder.setProgress(0, 0, false)
                                                     .setContentText((photosUploadedCount + 1) + "/" + (int) mImages.get(photosUploadedCount).length());
                                             notificationManager.notify(1, notificationBuilder.build());
                                             uploadingPhotos();
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

    private void startgeofence(MarkerOptions dlocation) {
        if (dlocation != null) {
            Geofence geofence = creategeofence(dlocation.getPosition(), 350f);
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

    private PendingIntent creategeofencePendingIntent() {

        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionService2.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onPause() {
        super.onPause();
        isEntered = false;
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildGoogleApiClient();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.d(TAG, "onResult: " + status);

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = String.valueOf(location.getLatitude());
        lon = String.valueOf(location.getLongitude());

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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

    public void FireRadioButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.fire_radio:
                fireParam = "fire";
                fineLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.no_fire_radio:
                fireParam = "nofire";
                fineLayout.setVisibility(View.GONE);
                break;
        }
    }

    public void onCheckboxesClicked(View view) {
        switch (view.getId()) {
            case R.id.chalaan_checkbox:
                isChalaan = !isChalaan;
                break;
            case R.id.fir_checkbox:
                isFir = !isFir;
                break;
        }
    }
}
