package com.silho.ideo.meetus.UI.fragments;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.facebook.Profile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.adapter.EventNearByAdapter;
import com.silho.ideo.meetus.adapter.PersonalCalendarAdapter;
import com.silho.ideo.meetus.api.Service;
import com.silho.ideo.meetus.model.EventfulResponse;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.utils.FontHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Samuel on 30/11/2017.
 */

public class EventsNearByFragment extends Fragment implements OnMapReadyCallback,
        EventNearByAdapter.OnMapSet {

    private static final String TAGER = EventsNearByFragment.class.getSimpleName();

    @BindView(R.id.eventsProgressBar) ProgressBar mProgressBar;
    @BindView(R.id.searchEventNearBy) SearchView mSearchViewEventNearBy;
    @BindView(R.id.eventNearByRecyclerView) RecyclerView mRecyclerViewEventNearBy;
    @BindView(R.id.myEventsButton) Button mMyEventsButton;
    @BindView(R.id.eventsButton) Button mEventsButton;

    private String mLocation ;
    private double mLatitude;
    private double mLongitude;
    private boolean mIsOnEvents = true;

    private EventNearByAdapter mEventNearByAdapter;
    private GoogleMap mGoogleMap;

    private ArrayList<Float> mLatitudes;
    private ArrayList<Float> mLongitudes;
    private ArrayList<String> mTitles;
    private ArrayList<ScheduledEvent> mScheduledEvents;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_nearby, container, false);
        ButterKnife.bind(this, view);
        FontHelper.setCustomTypeface(view);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapEvents);
        setRecyclerViewPositionAndHighlight(view);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mLocation = mLatitude + "," + mLongitude;
            mapFragment.getMapAsync(EventsNearByFragment.this);
            fetchEvents();
        });

        mMyEventsButton.setText(R.string.my_events_button);
        mMyEventsButton.setOnClickListener(view1 ->{
            mIsOnEvents = false;
            PersonalCalendarAdapter personalCalendarAdapter = new PersonalCalendarAdapter(getContext(), new ArrayList<>());
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(Profile.getCurrentProfile().getId()).child("scheduledEvent");
            Query query = databaseReference.orderByChild("tp");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    onMapReady(mGoogleMap);
                    for(DataSnapshot d:dataSnapshot.getChildren()){
                        ScheduledEvent scheduledEvent = d.getValue(ScheduledEvent.class);
                        personalCalendarAdapter.add(scheduledEvent);
                        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(scheduledEvent.getLatitude(),
                                scheduledEvent.getLongitude())).title(scheduledEvent.getPlaceName())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }
                    mScheduledEvents = personalCalendarAdapter.getScheduledEvents();
                    mRecyclerViewEventNearBy.setAdapter(personalCalendarAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        });
        mEventsButton.setText(R.string.events_button);
        mEventsButton.setOnClickListener(view1 -> {
            mIsOnEvents = true;
            onMapReady(mGoogleMap);
            fetchEvents();
        });

        mSearchViewEventNearBy.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onMapReady(mGoogleMap);
                Service.getEvents().eventSearched(mLocation, query).enqueue(new Callback<EventfulResponse>() {
                    @Override
                    public void onResponse(Call<EventfulResponse> call, Response<EventfulResponse> response) {
                        mEventNearByAdapter = new EventNearByAdapter(response.body(), EventsNearByFragment.this);
                        mRecyclerViewEventNearBy.setAdapter(mEventNearByAdapter);
                    }

                    @Override
                    public void onFailure(Call<EventfulResponse> call, Throwable t) {}
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        FontHelper.setCustomTypeface(mMyEventsButton);
        return view;
    }

    @SuppressLint("NewApi")
    private void setRecyclerViewPositionAndHighlight(View view) {
        SnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(mRecyclerViewEventNearBy);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewEventNearBy.setLayoutManager(layoutManager);

        mRecyclerViewEventNearBy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = layoutManager.findFirstVisibleItemPosition();
                    if(mIsOnEvents) {
                        highlightTheCurrentPlaceOnEvents(position);
                    } else {
                        highlightTheCurrentPlacesOnMyEvents(position);
                    }
                }
            }
        });
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)));
        mRecyclerViewEventNearBy.setVisibility(View.GONE);
    }

    private void fetchEvents() {
        if(mLocation != null) {
            Service.getEvents().event(mLocation).enqueue(new Callback<EventfulResponse>() {
                @Override
                public void onResponse(Call<EventfulResponse> call, Response<EventfulResponse> response) {
                    mProgressBar.setVisibility(View.GONE);
                    mRecyclerViewEventNearBy.setVisibility(View.VISIBLE);
                    mEventNearByAdapter = new EventNearByAdapter(response.body(), EventsNearByFragment.this);
                    mRecyclerViewEventNearBy.setAdapter(mEventNearByAdapter);
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
        mGoogleMap = googleMap;
        googleMap.clear();
        LatLng myLatLng = new LatLng(mLatitude, mLongitude);
        googleMap.addMarker(new MarkerOptions().position(myLatLng).title("I'm Here"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 10);
        googleMap.moveCamera(cameraUpdate);
        googleMap.setOnMarkerClickListener(marker ->{
            String title = marker.getTitle();
            int pos = 0;
            if(mIsOnEvents) {
                pos = mTitles.indexOf(title);
            } else {
                for(int i =0; i<mScheduledEvents.size(); i++){
                    if(mScheduledEvents.get(i).getPlaceName().equals(title)) pos = i;
                }
            }
            mRecyclerViewEventNearBy.getLayoutManager().scrollToPosition(pos);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            return true;
        });
    }

    @Override
    public void onMapSet(ArrayList<Float> latitude, ArrayList<Float> longitude, ArrayList<String> title) {
        mLatitudes = latitude;
        mLongitudes = longitude;
        mTitles = title;

        for(int i = 0; i<latitude.size(); i++){
            LatLng latLng = new LatLng(latitude.get(i), longitude.get(i));
            mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(title.get(i))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    private void highlightTheCurrentPlaceOnEvents(int position) {
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(mLatitudes.get(position), mLongitudes.get(position)))
                .title(mTitles.get(position)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
    }

    private void highlightTheCurrentPlacesOnMyEvents(int position){
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(mScheduledEvents.get(position).getLatitude(),
                mScheduledEvents.get(position).getLongitude())).title(mScheduledEvents.get(position).getPlaceName())
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
    }

}
