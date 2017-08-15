package com.silho.ideo.meetus.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silho.ideo.meetus.fragments.ForeseeFragment;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.fragments.FriendsFragment;
import com.silho.ideo.meetus.utils.CircleTransform;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FORSEE_FRAGMENT = "forsee_fragment";
    private static final String FRIENDS_FRAGMENT = "friends_fragment";
    public static final String USERNAME = "name";
    public static final String URL_PROFIL_PIC = "profil_pic_url";
    public static String ID_FACEBOOK;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.nav_view) NavigationView mNavView;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.container_frameLayout) FrameLayout mFrameLayout;

    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private boolean mAuthFlag;
    private ImageView mImageProfilNavHeader;
    private TextView mFullNameNavHeader;
    private TextView mEmailNavHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        View navHeader = mNavView.getHeaderView(0);
        mImageProfilNavHeader = (ImageView) navHeader.findViewById(R.id.profilPic);
        mFullNameNavHeader = (TextView) navHeader.findViewById(R.id.nameText);
        mEmailNavHeader = (TextView) navHeader.findViewById(R.id.emailText);

        mToolbar.setTitle("Foresee");
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavView.setNavigationItemSelectedListener(this);
        if(isNetworkAvailable()) {
            login();
        } else {
            Snackbar.make(mFrameLayout, "No Network", Snackbar.LENGTH_LONG).show();
        }
    }


    private void login() {
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if(!mAuthFlag){
                        getFacebookDataInfo();
                        mAuthFlag=true;
                    }
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .setTheme(R.style.LoginTheme)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    /** Data Methods **/

    protected boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void getFacebookDataInfo() {
        if(AccessToken.getCurrentAccessToken() != null){
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,email,name,picture.type(large)");
            new GraphRequest(AccessToken.getCurrentAccessToken(),
                    "/me", parameters, HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    JSONObject data = response.getJSONObject();
                    if (data.has("picture")) {
                        try {
                            setUserDataUI(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).executeAsync();
        }

        else {
            System.out.println("Access Token NULL");
        }
    }

    private void setUserDataUI(JSONObject data) throws JSONException {
        String name = data.getString("name");
        String email = data.getString("email");
        ID_FACEBOOK = data.getString("id");
        String profilPic = data.getJSONObject("picture")
                .getJSONObject("data").getString("url");
        Glide.with(MainActivity.this).load(profilPic)
                .thumbnail(0.75f)
                .apply(RequestOptions
                        .bitmapTransform(new CircleTransform(MainActivity.this)))
                .apply(RequestOptions
                        .diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(mImageProfilNavHeader);

        mFullNameNavHeader.setText(name);
        launchForeseeFragment(ID_FACEBOOK, name, profilPic);
        mEmailNavHeader.setText(email);
    }

    private void launchForeseeFragment(String idFacebook, String name, String profilPic) {
        ForeseeFragment savedFragment = (ForeseeFragment) getSupportFragmentManager().findFragmentByTag(FORSEE_FRAGMENT);
        if(savedFragment == null){
            ForeseeFragment foreseeFragment = new ForeseeFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ID_FACEBOOK, idFacebook);
            bundle.putString(USERNAME,name);
            bundle.putString(URL_PROFIL_PIC, profilPic);
            foreseeFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.container_frameLayout, foreseeFragment, FORSEE_FRAGMENT);
            transaction.commit();}
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.container_frameLayout, savedFragment).commit();
        }
    }

    private void launchFriendFragment() {
        FriendsFragment savedFragment = (FriendsFragment) getSupportFragmentManager().findFragmentByTag(FRIENDS_FRAGMENT);
        if(savedFragment == null){
        FriendsFragment friendsFragment = new FriendsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .add(R.id.container_frameLayout, friendsFragment, FRIENDS_FRAGMENT).addToBackStack(null);
        transaction.commit();}
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.container_frameLayout, savedFragment).commit();
        }
    }

    /**Lifecycle Methods **/

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }

        Log.i(TAG, "onPause Activity");

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAuth != null){
        mAuth.addAuthStateListener(mAuthStateListener);}
    }

    /** Menu's Methods **/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.log_out) {
            AuthUI.getInstance().signOut(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.friends) {
            launchFriendFragment();

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
