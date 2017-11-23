package com.silho.ideo.meetus.UI.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.MainActivity;
import com.silho.ideo.meetus.adapter.ItemNearbyAdapter;
import com.silho.ideo.meetus.adapter.PageAdapter;
import com.silho.ideo.meetus.parsers.PlaceNearbyCreator;
import com.silho.ideo.meetus.parsers.RoutesCreator;
import com.silho.ideo.meetus.parsers.TrajectCreator;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.model.User;
import com.silho.ideo.meetus.utils.FontHelper;
import com.silho.ideo.meetus.utils.WorkaroundMapFragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Samuel on 01/08/2017.
 */

public class ForeseeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationSource.OnLocationChangedListener,
        LocationListener, ItemNearbyAdapter.OnItemClicked, OnMapReadyCallback,
        FriendsFragment.OnFriendSelectionListener, View.OnClickListener {

    private static final String TAG = ForeseeFragment.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 2;
    private static final int DATE_PICKER = 3;
    private static final int TIME_PICKER = 4;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 6;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 7;

    @BindView(R.id.recyclerViewItemNearby) RecyclerView mRecyclerView;
    @BindView(R.id.durationTextView) TextView mDurationTextView;
    @BindView(R.id.addressTextView) TextView mAddressTextView;
    @BindView(R.id.scrollView) ScrollView mScrollView;
    @BindView(R.id.searchPlacesFAB) FloatingActionButton mFABsearchPlacesButton;
    @BindView(R.id.restaurantTypeFAB) FloatingActionButton mFABRestaurantType;
    @BindView(R.id.visitTypeFAB) FloatingActionButton mFABVisitType;
    @BindView(R.id.coffeeTypeFAB) FloatingActionButton mFABCoffeeType;
    @BindView(R.id.transportFAB) FloatingActionButton mFABTransport;
    @BindView(R.id.drivingFAB) FloatingActionButton mFABDriving;
    @BindView(R.id.walkingFAB) FloatingActionButton mFABWalking;
    @BindView(R.id.scheduleButton) FloatingActionButton mFABScheduleButton;
    @BindView(R.id.positionFAB) FloatingActionButton mFABPosition;

    private GoogleApiClient mClient;
    private PlaceNearbyCreator mPlaceNearbyCreator;
    private TrajectCreator mTrajectCreator;
    private RoutesCreator mRoutesCreator;
    private DatabaseReference mDatabaseReference;

    private double mMyLatitude;
    private double mMyLongitude;
    private double mLatitudeDestination;
    private double mLongitudeDestination;
    private String mToken;
    private String mIdFacebook;
    private String mUsername;
    private String mOwnerProfilPic;
    private String mNamePlace;
    private int mTransportType = 0;

    private int mYear;
    private int mMonth;
    private int mDay;
    private ArrayList<User> mFriend;
    private long mTime;
    private User mUser;
    private HashMap<String, User> hashMap;
    private Location mLastLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_foresee, container, false);
        FontHelper.setCustomTypeface(view);
        ButterKnife.bind(this, view);

        mFABRestaurantType.setOnClickListener(this);
        mFABCoffeeType.setOnClickListener(this);
        mFABVisitType.setOnClickListener(this);
        mFABPosition.setOnClickListener(this);
        mFABTransport.setOnClickListener(this);
        mFABDriving.setOnClickListener(this);
        mFABWalking.setOnClickListener(this);

        hashMap = new HashMap<>();

        mFriend = new ArrayList<>();

        mToken = FirebaseInstanceId.getInstance().getToken();
        if(getArguments() != null) {
            mOwnerProfilPic = getArguments().getString(PageAdapter.URL_PROFIL_PIC);
            mIdFacebook = Profile.getCurrentProfile().getId();
            mUsername = getArguments().getString(PageAdapter.USERNAME);
        }

        buildGoogleApiClient();
        setDate();

        if (Profile.getCurrentProfile().getId() != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            mDatabaseReference = database.getReference().child("users").child(Profile.getCurrentProfile().getId());
        }

        mPlaceNearbyCreator = new PlaceNearbyCreator(getActivity(), mRecyclerView);
        mTrajectCreator = new TrajectCreator(getActivity(), mDurationTextView);

        mFABsearchPlacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlacePickerClicked(view);
            }
        });

        return view;
    }

    /**Data Methods**/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        LatLng myLatLng = new LatLng(mMyLatitude, mMyLongitude);
        googleMap.addMarker(new MarkerOptions().position(myLatLng).title("I'm Here"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 10);
        googleMap.moveCamera(cameraUpdate);
        mRoutesCreator = new RoutesCreator(getActivity(), new ArrayList<LatLng>(), googleMap);
        ((WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.restaurantTypeFAB:
                resetDesignTrajectsTypes();
                setVisibility();
                resestDesign(mFABsearchPlacesButton, mFABCoffeeType, mFABVisitType,
                        mFABRestaurantType, R.color.colorPrimary, R.color.colorSecondary);

                mPlaceNearbyCreator.getWebServicesPlaceApi(mLastLocation, "restaurant");
                mPlaceNearbyCreator.initializeRecyclerviewAndAdapter(ForeseeFragment.this);
                break;
            case R.id.coffeeTypeFAB:
                resetDesignTrajectsTypes();
                setVisibility();
                resestDesign(mFABsearchPlacesButton, mFABRestaurantType, mFABVisitType,
                        mFABCoffeeType, R.color.colorPrimary, R.color.colorSecondary);

                mPlaceNearbyCreator.getWebServicesPlaceApi(mLastLocation, "cafe");
                mPlaceNearbyCreator.initializeRecyclerviewAndAdapter(ForeseeFragment.this);
                break;
            case R.id.visitTypeFAB:

                resetDesignTrajectsTypes();
                setVisibility();
                resestDesign(mFABsearchPlacesButton, mFABRestaurantType, mFABCoffeeType,
                        mFABVisitType, R.color.colorPrimary, R.color.colorSecondary);

                mPlaceNearbyCreator.getWebServicesPlaceApi(mLastLocation,
                        "art_gallery|movie_theater|museum");
                mPlaceNearbyCreator.initializeRecyclerviewAndAdapter(ForeseeFragment.this);
                break;
            case R.id.positionFAB:
                resestDesign(mFABTransport, mFABWalking, mFABDriving, mFABPosition,
                        R.color.colorPrimary, R.color.colorSecondary);
                mTransportType = 0;
                mDurationTextView.setText("");
                initializeRoadItinerary(new LatLng(mLatitudeDestination, mLongitudeDestination), "null");
                break;
            case R.id.transportFAB :
                resestDesign(mFABWalking, mFABDriving, mFABPosition, mFABTransport,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, mLatitudeDestination,
                        mLongitudeDestination, "transit");
                mTransportType = 2;
                initializeRoadItinerary(new LatLng(mLatitudeDestination, mLongitudeDestination), "transit");
                break;
            case R.id.drivingFAB :
                resestDesign(mFABTransport, mFABWalking, mFABPosition, mFABDriving,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, mLatitudeDestination,
                        mLongitudeDestination, "driving");
                mTransportType = 3;
                initializeRoadItinerary(new LatLng(mLatitudeDestination, mLongitudeDestination), "driving");
                break;
            case R.id.walkingFAB :
                resestDesign(mFABDriving, mFABTransport, mFABPosition, mFABWalking,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, mLatitudeDestination,
                        mLongitudeDestination, "walking");
                mTransportType = 4;
                initializeRoadItinerary(new LatLng(mLatitudeDestination, mLongitudeDestination), "walking");
                break;
            default:
                initializePlaceType();
                initializeTransportType();
                break;
        }
    }

    private void initializePlaceType() {
        mPlaceNearbyCreator.getWebServicesPlaceApi(mLastLocation, "");
        mPlaceNearbyCreator.initializeRecyclerviewAndAdapter(this);
    }

    private void initializeTransportType() {
        resestDesign(mFABTransport, mFABWalking, mFABDriving, mFABPosition,
                R.color.colorPrimary, R.color.colorSecondary);
        mTransportType = 0;
        initializeRoadItinerary(new LatLng(mLatitudeDestination, mLongitudeDestination), "null");
        mDurationTextView.setText("");
    }

    private void initializeRoadItinerary(LatLng latLng, String mode) {
        if (mRoutesCreator != null) {
            mRoutesCreator.addPoint(new LatLng(mMyLatitude, mMyLongitude));
            mRoutesCreator.initializeRoute(latLng, mode);
        }
    }

    private void setDate() {
        mFABScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.setTargetFragment(ForeseeFragment.this, DATE_PICKER);
                datePickerFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    /**Api Connection Methods**/

    private void buildGoogleApiClient() {
        mClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            setLocation();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION || requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setLocation() {
        LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
                mMyLatitude = location.getLatitude();
                mMyLongitude = location.getLongitude();
                initializePlaceType();
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(ForeseeFragment.this);

                mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            mUser = new User(mToken, mMyLatitude, mMyLongitude, mIdFacebook, mUsername, mOwnerProfilPic);
                            mDatabaseReference.setValue(mUser);
                        } else {
                            mDatabaseReference.child("token").setValue(FirebaseInstanceId.getInstance().getToken());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(1000 * 60);
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, locationRequest, this);
    }

    private void postRequestToServer() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("latitudeDestination", mLatitudeDestination);
        params.put("longitudeDestination", mLongitudeDestination);
        params.put("placeName", mNamePlace);
        params.put("time", mTime);

        params.put("idFacebook", mIdFacebook);
        params.put("username", mUsername);

        JSONArray array = new JSONArray();
        for(int i=0; i<mFriend.size(); i++){
            array.put(mFriend.get(i).transformToJsonObject());
        }

        params.put("friendsList", array);

        client.post("https://meetusite.herokuapp.com/request", params,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
      mDatabaseReference.child("latitude").setValue(location.getLatitude());
      mDatabaseReference.child("longitude").setValue(location.getLongitude());
    }

    /**Place Pickers Methods**/

    @Override
    public void onItemClicked(double latitude, double longitude, String name) {
        mLatitudeDestination = latitude;
        mLongitudeDestination = longitude;
        mNamePlace = name;
        initializeTransportType();
        initializeRoadItinerary(new LatLng(latitude, longitude), "null");
    }

    public void onPlacePickerClicked(View view) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;
        }
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(getActivity());
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
        } catch (Exception e) {
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {

            Place place = PlacePicker.getPlace(getActivity(), data);
            mNamePlace = String.valueOf(place.getName());
            String address = String.valueOf(place.getAddress());
            mLatitudeDestination = place.getLatLng().latitude;
            mLongitudeDestination = place.getLatLng().longitude;

            mTrajectCreator = new TrajectCreator(getActivity(), mDurationTextView);
            initializeTransportType();
            resetDesignAfterActivityResult(mNamePlace, address);

        }
        if(requestCode == DATE_PICKER){
            if(resultCode == Activity.RESULT_OK){
                Bundle bundle = data.getExtras();
                mYear = bundle.getInt("selectedYear");
                mMonth = bundle.getInt("selectedMonth");
                mDay = bundle.getInt("selectedDay");

                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.setTargetFragment(ForeseeFragment.this, TIME_PICKER);
                timePickerFragment.show(getFragmentManager(), "timePicker");
            }
        }
        if(requestCode == TIME_PICKER){
            if(resultCode == RESULT_OK){
                Bundle bund = data.getExtras();
                int hour = bund.getInt("selectedHour");
                int minute = bund.getInt("selectedMinutes");
                mTime = componentTimeToTimestamp(mYear, mMonth, mDay, hour, minute);
                if(mFriend.size() == 0){
                    setEvent(null);
                } else {
                    postRequestToServer();
                    setEvent(mFriend);
                }
            }
        }
    }

    private void setEvent(ArrayList<User> friends) {
        DatabaseReference databaseReference = mDatabaseReference.child("scheduledEvent");
        ScheduledEvent scheduledEvent = new ScheduledEvent(mTime, mNamePlace, mLatitudeDestination, mLongitudeDestination, true, friends, mTransportType);
        databaseReference.child(Long.toString(mTime)).setValue(scheduledEvent);
        Toast.makeText(getContext(), "Event Created Successfully", Toast.LENGTH_SHORT).show();
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    long componentTimeToTimestamp(int year, int month, int day, int hour, int minute) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return (c.getTimeInMillis() / 1000L);
    }

    /**Lifecycle Methods**/

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
        mScrollView.smoothScrollTo(0,0);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop Fragment");
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
        SharedPreferences preferences = getContext().getSharedPreferences(Profile.getCurrentProfile().getId(), 0);
        preferences.edit().clear().apply();

    }

    /**UI Methods**/

    private void resetDesignAfterActivityResult(String namePlace, String address) {
        mRecyclerView.setVisibility(View.GONE);
        mAddressTextView.setVisibility(View.VISIBLE);
        mAddressTextView.setText(namePlace + " " + address);
        mFABsearchPlacesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorSecondary)));
        mFABRestaurantType.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABCoffeeType.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABVisitType.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABDriving.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABWalking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABPosition.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorSecondary)));
        mFABTransport.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
    }

    private void resetDesignTrajectsTypes() {
        mDurationTextView.setText("");
        mFABPosition.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABWalking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABDriving.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        mFABTransport.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
    }

    private void resestDesign(FloatingActionButton floatingActionButton,
                              FloatingActionButton floatingActionButton1,
                              FloatingActionButton floatingActionButton2,
                              FloatingActionButton floatingActionButton4,
                              int color, int color1) {
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(),color)));
        floatingActionButton1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), color)));
        floatingActionButton2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), color)));
        floatingActionButton4.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), color1)));
    }

    private void setVisibility() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mAddressTextView.setVisibility(View.GONE);
    }

    @Override
    public void onFriendSelectioned(User user, String id) {
        if(hashMap.containsKey(id)){
            hashMap.remove(id);
        } else {
            hashMap.put(id, user);
        }

        mFriend = new ArrayList<>(hashMap.values());
    }
}
