package com.cowbreedidentifier.cowpoopingapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentListtitle = new ArrayList<>();
    public MyPagerAdapter(FragmentManager fm){
        super(fm);
    }

    public void addFragment(Fragment fragment, String string) {
        fragmentList.add(fragment);
        fragmentListtitle.add(string);
    }
    @Override    public Fragment getItem(int position) {
        if(fragmentList.size()==0) {
            return null;
        }
        return fragmentList.get(position);
    }
    @Override
    public int getCount() {
        return 2;
    }
    @Override    public CharSequence getPageTitle(int position) {
        if(fragmentListtitle.size()==0) {
            return null;
        }
        return fragmentListtitle.get(position);
    }
}
