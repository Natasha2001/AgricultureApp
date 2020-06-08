package com.theagriculture.app.Admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.theagriculture.app.R;
import com.theagriculture.app.login_activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.res.ColorStateList.*;

public class AdminActivity extends AppCompatActivity {

    //all permissions declared

    private final String TAG = "AdminActivity";
    private final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final int RESULT_CODE = 786;

    BottomNavigationView navigation;
    FrameLayout frameLayout;

    //declare all fragments

    private map_fragemnt mapFragmnt;
    private location_fragment locationFragment;
    private ado_fragment adoFragment;
    private ddo_fragment ddoFragment;
    private count_fragment countFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        navigation = findViewById(R.id.navigation);
        frameLayout = findViewById(R.id.frameLayout);
        mapFragmnt = new map_fragemnt();
        locationFragment = new location_fragment();
        adoFragment = new ado_fragment();
        ddoFragment = new ddo_fragment();
        countFragment = new count_fragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.app__bar);
        setSupportActionBar(toolbar);


        if(getPermission()) {

            InitializeFragment(mapFragmnt);
            navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.adminshome:
                            InitializeFragment(mapFragmnt);
                            return true;
                        case R.id.adminslocation:
                            InitializeFragment(locationFragment);
                            return true;
                        case R.id.adminsado:
                            InitializeFragment(adoFragment);
                            return true;
                        case R.id.adminsdda:
                            InitializeFragment(ddoFragment);
                            return true;
                        case R.id.adminsdistrict_state:
                            InitializeFragment(countFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }

    }
    //end of onCreate

    //function to change fragment
    public void InitializeFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }

    //function to get permissions
    private boolean getPermission() {
        List<String> Permission = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.add(ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.add(ACCESS_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.add(READ_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.add(WRITE_EXTERNAL_STORAGE);
        }

        if (!Permission.isEmpty()) {
            String[] permissions = Permission.toArray(new String[Permission.size()]);
            ActivityCompat.requestPermissions(this, permissions, RESULT_CODE);
            return false;
        } else
            return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == RESULT_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if (deniedCount == 0) {
                InitializeFragment(mapFragmnt);
            }
            else {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(AdminActivity.this, permName)) {
                        showDialog("", "This app needs location and files permissions to work without any problems.",
                                "Yes, Grant permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        getPermission();
                                    }
                                },
                                "No, Exit app",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                    }
                    else {
                        showDialog("",
                                "You have denied some permissions. Allow all the permissions at [Setting] > [Permissions]",
                                "Go to Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, "No, Exit App",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                        break;
                    }
                }
            }
        }
    }

    private AlertDialog showDialog(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnclick,
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
//function for top drop down menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_top_bar , menu);
        //return true;

        //MenuItem action_done = menu.findItem(R.id.ic_file_upload);
        //menuIconColor(action_done, R.drawable.tab_color);
        return super.onCreateOptionsMenu(menu);
    }

    /*public void menuIconColor(MenuItem menuItem, int color) {
        color = R.drawable.tab_color_new;
        Drawable drawable = menuItem.getIcon();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            SharedPreferences.Editor editor = getSharedPreferences("tokenFile", MODE_PRIVATE).edit();
            editor.clear();
            editor.commit();
            Intent intent = new Intent(AdminActivity.this, login_activity.class);
            startActivity(intent);
            finish();
            return true;
        }
        if(id == R.id.help){

        }

        if(id == R.id.policy){

        }

        if(id == R.id.service){

        }

        return super.onOptionsItemSelected(item);
    }




    private void showMap() {
        Log.d(TAG, "showMap: getsupport");
        FragmentManager mfragmentmanager = getSupportFragmentManager();
        Log.d(TAG, "showMap: getsupport" + mfragmentmanager);
        mfragmentmanager.beginTransaction().replace(R.id.frameLayout, mapFragmnt).commit();
        //InitializeFragment(mapFragmnt);
    }
    @Override
    public void onBackPressed(){
        //finish();//will pop previous activity from stack
        Intent a = new Intent(Intent.ACTION_MAIN);//will exit app
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}