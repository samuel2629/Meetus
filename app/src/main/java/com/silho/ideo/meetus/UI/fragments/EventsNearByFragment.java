package com.silho.ideo.meetus.UI.fragments;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.adapter.EventNearByAdapter;
import com.silho.ideo.meetus.api.Service;
import com.silho.ideo.meetus.model.EventfulResponse;
import com.silho.ideo.meetus.parsers.RoutesCreator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Samuel on 30/11/2017.
 */

public class EventsNearByFragment extends Fragment implements OnMapReadyCallback{

    //@BindView(R.id.searchEventNearBy) SearchView mSearchViewEventNearBy;
    @BindView(R.id.eventNearByRecyclerView) RecyclerView mRecyclerViewEventNearBy;
    private String mLocation ;
    private EventNearByAdapter mEventNearByAdapter;
    private double mLatitude;
    private double mLongitude;
    private FusedLocationProviderClient mFusedLocationClient;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_nearby, container, false);
        ButterKnife.bind(this, view);

        SnapHelper helper = new LinearSnapHelper();
        mRecyclerViewEventNearBy.setOnFlingListener(null);
        helper.attachToRecyclerView(mRecyclerViewEventNearBy);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewEventNearBy.setLayoutManager(layoutManager);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapEvents);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mLocation = mLatitude + "," + mLongitude;
            mapFragment.getMapAsync(EventsNearByFragment.this);
            fetchRecipes();
        });

        return view;
    }

    private void fetchRecipes() {
        if(mLocation != null) {
            Service.getRecipe().recipes(mLocation).enqueue(new Callback<EventfulResponse>() {
                @Override
                public void onResponse(Call<EventfulResponse> call, Response<EventfulResponse> response) {
                    mEventNearByAdapter = new EventNearByAdapter(response.body());
                    mRecyclerViewEventNearBy.swapAdapter(mEventNearByAdapter, true);
                }

                @Override
                public void onFailure(Call<EventfulResponse> call, Throwable t) {

                }
            });
        } else {
            Toast.makeText(getContext(), "Can't get your current location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        LatLng myLatLng = new LatLng(mLatitude, mLongitude);
        googleMap.addMarker(new MarkerOptions().position(myLatLng).title("I'm Here"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 10);
        googleMap.moveCamera(cameraUpdate);
    }

}
