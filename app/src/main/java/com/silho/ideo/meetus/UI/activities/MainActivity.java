package com.silho.ideo.meetus.UI.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silho.ideo.meetus.adapter.PageAdapter;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.controller.alarmManager.ReminderScheduler;
import com.silho.ideo.meetus.utils.FontHelper;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.container_frameLayout) FrameLayout mFrameLayout;
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.toolbar_title) TextView mToolBarTitle;

    public static final int RC_SIGN_IN = 123;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private boolean mAuthFlag;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(mToolbar);
        MainActivity.this.setTitle("");

        String title = "Scheduler";
        mToolBarTitle.setText(title);

        if(isNetworkAvailable()) {
            login();
        }
    }

    /** Data Methods **/

    private void login() {
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                if(!mAuthFlag){
                    getFacebookDataInfo();
                    ReminderScheduler.scheduleReminder(MainActivity.this);
                    mAuthFlag=true;
                }
            } else {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.FacebookBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .setTheme(R.style.LoginTheme)
                                .build(),
                        RC_SIGN_IN);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                String d  = data.getDataString();
            } else {
                System.out.println("MEEEEEERDE");
            }
        } else {
            System.out.println("MEEEEEERDE");
        }

    }

        protected boolean isNetworkAvailable () {
            ConnectivityManager manager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            boolean isAvailable = false;
            if (networkInfo != null && networkInfo.isConnected()) {
                isAvailable = true;
            }
            return isAvailable;
        }

        private void getFacebookDataInfo () {
            if (AccessToken.getCurrentAccessToken() != null) {
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,picture.type(large)");
                new GraphRequest(AccessToken.getCurrentAccessToken(),
                        "/me", parameters, HttpMethod.GET, response -> {
                    JSONObject data = response.getJSONObject();
                    if (data.has("picture")) {
                        try {
                            setUserDataUI(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();
            } else {
                System.out.println("Access Token NULL");
            }
        }

        private void setUserDataUI (JSONObject data) throws JSONException {
            String name = data.getString("name");
            String profilPic = data.getJSONObject("picture")
                    .getJSONObject("data").getString("url");

            PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), 2, name, profilPic);
            mViewPager.setAdapter(pageAdapter);
            mViewPager.setCurrentItem(1, true);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        mToolBarTitle.setText(R.string.calendar_viewpager_title);
                    } else if (position == 1) {
                        mToolBarTitle.setText(R.string.scheduler_viewpager_title);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        /**Lifecycle Methods **/

        @Override
        public void onPause () {
            super.onPause();
            if (mAuthStateListener != null) {
                mAuth.removeAuthStateListener(mAuthStateListener);
            }

            Log.i(TAG, "onPause Activity");

        }

        @Override
        public void onResume () {
            super.onResume();
            if (mAuth != null) {
                mAuth.addAuthStateListener(mAuthStateListener);
            }
        }

        /** Menu's Methods **/

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            int id = item.getItemId();

            if (id == R.id.log_out) {
                AuthUI.getInstance().signOut(this);
            }
            if (id == R.id.renew) {
                recreate();
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onNavigationItemSelected (@NonNull MenuItem item){
            return false;
        }
    }
