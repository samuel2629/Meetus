package com.silho.ideo.meetus.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.fragments.ForeseeFragment;
import com.silho.ideo.meetus.fragments.FriendsFragment;
import com.silho.ideo.meetus.fragments.PersonalCalendarFragment;

/**
 * Created by Samuel on 16/08/2017.
 */

public class PageAdapter extends FragmentPagerAdapter {

    public static final String USERNAME = "name";
    public static final String URL_PROFIL_PIC = "profil_pic_url";
    public static String ID_FACEBOOK;

    private int mInt;
    private String mIdFacebook, mName, mProfilPic;

    public PageAdapter(FragmentManager fm, int i, String idFacebook, String name, String profilPic) {
        super(fm);
        mInt = i;
        mIdFacebook = idFacebook;
        mName = name;
        mProfilPic = profilPic;
    }

    public PageAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PersonalCalendarFragment();
            case 1:
                return launchForeseeFragment();
            case 2:
                return new FriendsFragment();
            default:
                return null;
        }
    }

    private ForeseeFragment launchForeseeFragment() {
            ForeseeFragment foreseeFragment = new ForeseeFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ID_FACEBOOK, mIdFacebook);
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
