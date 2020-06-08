package com.theagriculture.app.Admin.AdoDdoActivity;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class AdoDdoActivityPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;
    private int tabCount;

    public AdoDdoActivityPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        fragments = new ArrayList<>();
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (tabCount == 3) {
            switch (position) {
                case 0:
                    return "Pending";
                case 1:
                    return "Ongoing";
                case 2:
                    return "Completed";
            }
        } else
            switch (position) {
                case 0:
                    return "Pending";
                case 1:
                    return "Completed";
            }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }
}
