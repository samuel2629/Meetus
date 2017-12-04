package com.silho.ideo.meetus.UI.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.silho.ideo.meetus.R;

/**
 * Created by Samuel on 04/12/2017.
 */

public class RootFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);
        getFragmentManager().beginTransaction().replace(R.id.container_frameLayout, new PersonalCalendarFragment()).commit();
        return view;
    }
}
