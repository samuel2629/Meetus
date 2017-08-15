package com.silho.ideo.meetus.activities;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.data.RoutesCreator;
import com.silho.ideo.meetus.data.TrajectCreator;
import com.silho.ideo.meetus.firebaseCloudMessaging.MyFirebaseMessagingService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InvitationResumerActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    @BindView(R.id.durationTextView)
    TextView mDurationTextView;
    @BindView(R.id.distanceTextView)
    TextView mDistanceTextView;


    private RoutesCreator mRoutesCreator;
    private GoogleApiClient mClient;
    private double mMyLatitude;
    private double mMyLongitude;
    private String mIdFacebook;
    private String mDurationSender;
    private double mLatitudeDestination;
    private double mLongitudeDestination;
    private TrajectCreator mTrajectCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitation_resumer);
        ButterKnife.bind(this);
        buildGoogleApiClient();

        mIdFacebook = getIntent().getExtras().getString(MyFirebaseMessagingService.ID_FACEBOOK);
        mDurationSender = getIntent().getExtras().getString(MyFirebaseMessagingService.DURATION);
        mLatitudeDestination = getIntent().getExtras().getDouble(MyFirebaseMessagingService.LATITUDE_DEST);
        mLongitudeDestination = getIntent().getExtras().getDouble(MyFirebaseMessagingService.LONGITUDE_DEST);

    }


    private void setRoadItinerary(LatLng latLng, String mode) {
        if (mRoutesCreator != null) {
            mRoutesCreator.addPoint(new LatLng(mMyLatitude, mMyLongitude));
            mRoutesCreator.initializeRoute(latLng, mode);
        }
    }

    private void buildGoogleApiClient() {
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        LatLng myLatLng = new LatLng(mMyLatitude, mMyLongitude);
        googleMap.addMarker(new MarkerOptions().position(myLatLng).title("I'm Here"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 10);
        googleMap.moveCamera(cameraUpdate);
        mRoutesCreator = new RoutesCreator(this, new ArrayList<LatLng>(), googleMap);
        setRoadItinerary(new LatLng(mLatitudeDestination, mLongitudeDestination), "driving");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMyLatitude = LocationServices.FusedLocationApi.getLastLocation(mClient).getLatitude();
        mMyLongitude = LocationServices.FusedLocationApi.getLastLocation(mClient).getLongitude();
        mTrajectCreator = new TrajectCreator(this, mDurationTextView, mDistanceTextView);
        setTransportType(mLatitudeDestination, mLongitudeDestination);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void setTransportType(final double latitude, final double longitude) {

        resestDesign(mFABBiking, mFABTransport, mFABWalking, mFABDriving,
                R.color.colorPrimary, R.color.colorSecondary);

        mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                longitude, "driving");
        setRoadItinerary(new LatLng(latitude, longitude), "driving");

        mFABBiking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABWalking, mFABDriving, mFABTransport, mFABBiking,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "bicycling");
                setRoadItinerary(new LatLng(latitude, longitude), "bicycling");
            }
        });
        mFABTransport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABBiking, mFABWalking, mFABDriving, mFABTransport,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "transit");
                setRoadItinerary(new LatLng(latitude, longitude), "transit");
            }
        });
        mFABDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABBiking, mFABTransport, mFABWalking, mFABDriving,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "driving");
                setRoadItinerary(new LatLng(latitude, longitude), "driving");

            }
        });
        mFABWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABBiking, mFABDriving, mFABTransport, mFABWalking,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "walking");
                setRoadItinerary(new LatLng(latitude, longitude), "walking");
            }
        });
    }

    private void resestDesign(FloatingActionButton floatingActionButton,
                              FloatingActionButton floatingActionButton1,
                              FloatingActionButton floatingActionButton2,
                              FloatingActionButton floatingActionButton3,
                              int color, int color1) {
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,color)));
        floatingActionButton1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
        floatingActionButton2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
        floatingActionButton3.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color1)));
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }
}
