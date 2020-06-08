package com.theagriculture.app.Dda;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.theagriculture.app.R;

import java.util.ArrayList;

public class VillagesUnderAdo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_villages);
        Intent intent = getIntent();
        ArrayList<String> listOfVillages = intent.getStringArrayListExtra("listArray");
        String adoName = intent.getStringExtra("adoName");
        getSupportActionBar().setTitle("Villages under " + adoName);
        RecyclerView recyclerView = findViewById(R.id.villagesRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        VillagesListRecyAdapter adapter = new VillagesListRecyAdapter(this, listOfVillages);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
