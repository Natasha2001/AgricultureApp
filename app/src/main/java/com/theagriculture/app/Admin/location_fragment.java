package com.theagriculture.app.Admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.theagriculture.app.R;

public class location_fragment extends Fragment {

    TabLayout mtablayout;
    Toolbar mtoolbar;
    TabItem pending;
    TabItem completed;
    TabPageAdapter mpageAdapter;
    ViewPager pager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment,container,false);


        mtablayout = view.findViewById(R.id.tabLayout);
        pending = view.findViewById(R.id.pending);
        completed = view.findViewById(R.id.completed);
        pager = view.findViewById(R.id.viewPager);
        mpageAdapter = new TabPageAdapter(getChildFragmentManager(),mtablayout.getTabCount());
        pager.setAdapter(mpageAdapter);

        mtablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mtablayout));
        pager.setOffscreenPageLimit(3);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
