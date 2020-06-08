package com.theagriculture.app.Admin.AdoDdoActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.theagriculture.app.R;

public class AdoDdoActivity extends AppCompatActivity {
    private AdoDdoActivityPagerAdapter adapter;
    private boolean isDdo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ddo);
        Intent intent = getIntent();
        String id = intent.getStringExtra("Id");
        isDdo = intent.getBooleanExtra("isDdo", false);
        Log.d("ddoId", "onCreate: " + id);
        TabLayout tabLayout = findViewById(R.id.ddo_activity_tablayout);
        tabLayout.addTab(tabLayout.newTab());
        if (isDdo)
            tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        ViewPager viewPager = findViewById(R.id.ddo_activity_viewpager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String title = intent.getStringExtra("name");
        getSupportActionBar().setTitle(title);
        int tabCount;
        if (isDdo)
            tabCount = 3;
        else
            tabCount = 2;
        adapter = new AdoDdoActivityPagerAdapter(getSupportFragmentManager(), tabCount);
        adapter.addFragment(new AdoDdoPending(id, isDdo));
        if (isDdo)
            adapter.addFragment(new AdoDdoOngoing(id, isDdo));
        adapter.addFragment(new AdoDdoCompleted(id, isDdo));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
