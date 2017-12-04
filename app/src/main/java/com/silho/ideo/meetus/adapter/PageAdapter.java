package com.silho.ideo.meetus.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.silho.ideo.meetus.UI.fragments.RootFragment;
import com.silho.ideo.meetus.UI.fragments.ScheduleEventFragment;

/**
 * Created by Samuel on 16/08/2017.
 */

public class PageAdapter extends FragmentStatePagerAdapter {

    public static final String USERNAME = "name";
    public static final String URL_PROFIL_PIC = "profil_pic_url";

    private int mInt;
    private String mName, mProfilPic;
    FragmentManager mFragmentManager;

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    public PageAdapter(FragmentManager fm, int i,String name, String profilPic) {
        super(fm);
        mFragmentManager = fm;
        mInt = i;
        mName = name;
        mProfilPic = profilPic;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RootFragment();
            case 1:
                return launchScheduleEventFragment();
            default:
                return launchScheduleEventFragment();
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private ScheduleEventFragment launchScheduleEventFragment() {
            ScheduleEventFragment foreseeFragment = new ScheduleEventFragment();
            Bundle bundle = new Bundle();
            bundle.putString(USERNAME,mName);
            bundle.putString(URL_PROFIL_PIC, mProfilPic);
            foreseeFragment.setArguments(bundle);
        return foreseeFragment;
    }

    @Override
    public int getCount() {
        return mInt;
    }
}
