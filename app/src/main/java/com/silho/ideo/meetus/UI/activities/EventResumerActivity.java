package com.silho.ideo.meetus.UI.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.adapter.FriendsAdapter;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.parsers.RoutesCreator;
import com.silho.ideo.meetus.parsers.TrajectCreator;
import com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService;
import com.silho.ideo.meetus.UI.fragments.ForeseeFragment;
import com.silho.ideo.meetus.model.User;
import com.silho.ideo.meetus.utils.FontHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class EventResumerActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.root) RelativeLayout mRelativeLayout;
    @BindView(R.id.recyclerViewInvitation) RecyclerView mRecyclerView;
    @BindView(R.id.positionFAB) FloatingActionButton mFABposition;
    @BindView(R.id.transportFAB) FloatingActionButton mFABTransport;
    @BindView(R.id.drivingFAB) FloatingActionButton mFABDriving;
    @BindView(R.id.walkingFAB) FloatingActionButton mFABWalking;
    @BindView(R.id.durationTextView) TextView mDurationTextView;
    @BindView(R.id.placeTextView) TextView mPlaceNameTextView;
    @BindView(R.id.dateTextView) TextView mDateTextView;
    @BindView(R.id.frameLayoutInvitation) FrameLayout mFrameLayout;
    @BindView(R.id.accept_button) FloatingActionButton mAcceptButton;
    @BindView(R.id.decline_button) FloatingActionButton mDeclineButton;

    private RoutesCreator mRoutesCreator;
    private GoogleApiClient mClient;
    private double mMyLatitude;
    private double mMyLongitude;
    private double mLatitudeDestination;
    private double mLongitudeDestination;
    private TrajectCreator mTrajectCreator;
    private String mPlaceName;
    private long mTime;
    private String mIdFacebook;
    private JSONArray mFriendList;
    private String mIdFacebookCurrent;
    private int mTransportType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_resumer);
        ButterKnife.bind(this);
        FontHelper.setCustomTypeface(mRelativeLayout);
        mIdFacebookCurrent = Profile.getCurrentProfile().getId();

        buildGoogleApiClient();
        getEventInformations();
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

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ForeseeFragment.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ForeseeFragment.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        mMyLatitude = LocationServices.FusedLocationApi.getLastLocation(mClient).getLatitude();
        mMyLongitude = LocationServices.FusedLocationApi.getLastLocation(mClient).getLongitude();
        mTrajectCreator = new TrajectCreator(this, mDurationTextView);
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

    private void setRoadItinerary(LatLng latLng, String mode) {
        if (mRoutesCreator != null) {
            mRoutesCreator.addPoint(new LatLng(mMyLatitude, mMyLongitude));
            mRoutesCreator.initializeRoute(latLng, mode);
        }
    }

    private void setTransportType(final double latitude, final double longitude) {

        switch (mTransportType){
            case 0:
                resestDesign(mFABWalking, mFABDriving, mFABTransport, mFABposition,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTransportType = 0;
                setRoadItinerary(new LatLng(latitude, longitude), "null");
                break;
            case 2:
                resestDesign(mFABposition, mFABWalking, mFABDriving, mFABTransport,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "transit");
                mTransportType = 2;
                setRoadItinerary(new LatLng(latitude, longitude), "transit");
                break;
            case 3:
                resestDesign(mFABposition, mFABTransport, mFABWalking, mFABDriving,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "driving");
                mTransportType = 3;
                setRoadItinerary(new LatLng(latitude, longitude), "driving");
                break;
            case 4 :
                resestDesign(mFABposition, mFABDriving, mFABTransport, mFABWalking,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "walking");
                mTransportType = 4;
                setRoadItinerary(new LatLng(latitude, longitude), "walking");
                break;
        }

        mFABposition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABWalking, mFABDriving, mFABTransport, mFABposition,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTransportType = 0;
                setRoadItinerary(new LatLng(latitude, longitude), "null");
            }
        });
        mFABTransport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABposition, mFABWalking, mFABDriving, mFABTransport,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "transit");
                mTransportType = 2;
                setRoadItinerary(new LatLng(latitude, longitude), "transit");
            }
        });
        mFABDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABposition, mFABTransport, mFABWalking, mFABDriving,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "driving");
                mTransportType = 3;
                setRoadItinerary(new LatLng(latitude, longitude), "driving");

            }
        });
        mFABWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resestDesign(mFABposition, mFABDriving, mFABTransport, mFABWalking,
                        R.color.colorPrimary, R.color.colorSecondary);

                mTrajectCreator.getWebServicesPlaceApi(mMyLatitude, mMyLongitude, latitude,
                        longitude, "walking");
                mTransportType = 4;
                setRoadItinerary(new LatLng(latitude, longitude), "walking");
            }
        });
    }

    private void getEventInformations() {
        if(getIntent().getExtras() != null) {
            mLatitudeDestination = getIntent().getExtras().getDouble(MyFirebaseMessagingService.LATITUDE_DEST);
            mLongitudeDestination = getIntent().getExtras().getDouble(MyFirebaseMessagingService.LONGITUDE_DEST);
            mPlaceName = getIntent().getExtras().getString(MyFirebaseMessagingService.PLACE_NAME);
            mTime = getIntent().getExtras().getLong(MyFirebaseMessagingService.TIME);
            mIdFacebook = getIntent().getExtras().getString(MyFirebaseMessagingService.ID_FACEBOOK);
            mTransportType = getIntent().getExtras().getInt(MyFirebaseMessagingService.TRANSPORT_TYPE);

            ArrayList<User> friendsListInvited = getIntent().getExtras().getParcelableArrayList(MyFirebaseMessagingService.FRIENDS_LIST_INVITED);
            bindFriendsOnCheckedEvent(friendsListInvited);

            String friendsList = getIntent().getExtras().getString(MyFirebaseMessagingService.FRIENDS_LIST);
            if (friendsList != null) {
                try {
                    bindFriendsOnNotificationReceived(new JSONArray(friendsList));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String place = "Place : " + mPlaceName;
            mPlaceNameTextView.setText(place);
            String date = "Date : " + getDate(mTime * 1000);
            mDateTextView.setText(date);
        }
    }

    private String getDate(long time) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(time);
        return DateFormat.format("EEE d MMM yyyy 'at' HH:mm", cal).toString();
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

    private void bindFriendsOnCheckedEvent(ArrayList<User> friendsListInvited) {
        if(friendsListInvited != null) {
            final ArrayList<FriendsAdapter.FriendItem> friendItems = new ArrayList<>();
            for (int i = 0; i < friendsListInvited.size(); i++) {
                String id = friendsListInvited.get(i).getIdFacebook();
                String name =friendsListInvited.get(i).getName();
                String image = friendsListInvited.get(i).getProfilPic();
                FriendsAdapter.FriendItem friendItem = new FriendsAdapter.FriendItem(id, name, image);
                friendItems.add(friendItem);
            }
            FriendsAdapter adapter = new FriendsAdapter(friendItems);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL));
            mAcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeTransportType();
                }
            });
            mDeclineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postResponseToServer(2);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(Profile.getCurrentProfile().getId()).child("scheduledEvent")
                            .child(Long.toString(mTime));
                    databaseReference.removeValue();
                    finish();
                }
            });
        } else {
            mFrameLayout.setVisibility(View.GONE);
        }
    }

    private void changeTransportType() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("users").child(mIdFacebookCurrent)
                .child("scheduledEvent").child(Long.toString(mTime));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ScheduledEvent scheduledEvent = dataSnapshot.getValue(ScheduledEvent.class);
                if (scheduledEvent != null) {
                    int transportType = scheduledEvent.getTransportType();
                    if (mTransportType != transportType) {
                        databaseReference.child("transportType").setValue(mTransportType);
                        Toast.makeText(EventResumerActivity.this, "Registered", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        finish();
    }

    private void bindFriendsOnNotificationReceived(final JSONArray friendList) {
        if(friendList != null) {
            mFrameLayout.setVisibility(View.VISIBLE);
            mAcceptButton.setVisibility(View.VISIBLE);
            mDeclineButton.setVisibility(View.VISIBLE);
            final ArrayList<FriendsAdapter.FriendItem> friendItems = new ArrayList<>();
            for (int i = 0; i < friendList.length(); i++) {
                try {
                    String id = friendList.getJSONObject(i).getString("idFacebook");
                    String name = friendList.getJSONObject(i).getString("name");
                    String image = friendList.getJSONObject(i).getString("profilPic");
                    FriendsAdapter.FriendItem friendItem = new FriendsAdapter.FriendItem(id, name, image);
                    friendItems.add(friendItem);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mIdFacebook);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    friendList.put(user.transformToJsonObject());
                    mFriendList = friendList;
                    String id = user.getIdFacebook();
                    String profilPic = user.getProfilPic();
                    String name = user.getName();
                    FriendsAdapter.FriendItem friendItem= new FriendsAdapter.FriendItem(id, name, profilPic);
                    friendItems.add(friendItem);
                    FriendsAdapter adapter = new FriendsAdapter(friendItems);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(EventResumerActivity.this);
                    mRecyclerView.setLayoutManager(layoutManager);
                    mRecyclerView.setAdapter(adapter);
                    mRecyclerView.addItemDecoration(new DividerItemDecoration(EventResumerActivity.this,
                            DividerItemDecoration.VERTICAL));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mAcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postResponseToServer(1);
                    createNewEventReceveid(friendItems);
                    finish();
                }
            });

            mDeclineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postResponseToServer(2);
                    finish();
                }
            });
        } else {
            mFrameLayout.setVisibility(View.GONE);
        }
    }

    private void createNewEventReceveid(ArrayList<FriendsAdapter.FriendItem> friendItems) {
        ArrayList<User> users = new ArrayList<>();
        for(FriendsAdapter.FriendItem friendItem:friendItems){
            User user = new User();
            user.setName(friendItem.getName());
            user.setProfilPic(friendItem.getImage());
            user.setIdFacebook(friendItem.getId());
            users.add(user);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mIdFacebookCurrent).child("scheduledEvent");
        ScheduledEvent scheduledEvent = new ScheduledEvent(mTime, mPlaceName, mLatitudeDestination, mLongitudeDestination, false, users, mTransportType);
        databaseReference.child(Long.toString(mTime)).setValue(scheduledEvent);
    }

    private void postResponseToServer(int acceptedOrDelined) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("time", mTime);
        params.put("idFacebook", mIdFacebookCurrent);
        params.put("acceptedOrDeclined", acceptedOrDelined);

        params.put("friendsList", mFriendList);

        client.post("https://meetusite.herokuapp.com/acceptedOrDeclined", params,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Toast.makeText(EventResumerActivity.this, responseString, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
