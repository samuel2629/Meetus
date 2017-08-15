package com.silho.ideo.meetus.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.data.RoutesCreator;
import com.silho.ideo.meetus.utils.WorkaroundMapFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Samuel on 11/08/2017.
 */

public class InvitationResumerFragment extends Fragment implements OnMapReadyCallback {

    @BindView(R.id.recyclerViewInvitation)
    RecyclerView mRecyclerView;
    @BindView(R.id.bikingFAB)
    FloatingActionButton mFABBiking;
    @BindView(R.id.transportFAB)
    FloatingActionButton mFABTransport;
    @BindView(R.id.drivingFAB)
    FloatingActionButton mFABDriving;
    @BindView(R.id.walkingFAB)
    FloatingActionButton mFABWalking;


    private RoutesCreator mRoutesCreator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.invitation_resumer, container, false);
        ButterKnife.bind(this, view);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
       /* LatLng myLatLng = new LatLng(mMyLatitude, mMyLongitude);
        googleMap.addMarker(new MarkerOptions().position(myLatLng).title("I'm Here"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 10);
        googleMap.moveCamera(cameraUpdate);*/
        mRoutesCreator = new RoutesCreator(getActivity(), new ArrayList<LatLng>(), googleMap);
    }
}
