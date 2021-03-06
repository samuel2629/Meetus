package com.silho.ideo.meetus.adapter;

import android.os.Bundle;

import com.silho.ideo.meetus.UI.fragments.ForeseeFragment;
import com.silho.ideo.meetus.UI.fragments.PersonalCalendarFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by Samuel on 16/08/2017.
 */

public class PageAdapter extends FragmentStatePagerAdapter {

    public static final String USERNAME = "name";
    public static final String URL_PROFIL_PIC = "profil_pic_url";

    private int mInt;
    private String mName, mProfilPic;

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    public PageAdapter(FragmentManager fm, int i,String name, String profilPic) {
        super(fm);
        mInt = i;
        mName = name;
        mProfilPic = profilPic;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PersonalCalendarFragment();
            case 1:
                return launchForeseeFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private ForeseeFragment launchForeseeFragment() {
            ForeseeFragment foreseeFragment = new ForeseeFragment();
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
