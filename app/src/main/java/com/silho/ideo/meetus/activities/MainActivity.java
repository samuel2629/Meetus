package com.silho.ideo.meetus.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silho.ideo.meetus.adapter.PageAdapter;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.firebaseCloudMessaging.DemoSyncJob;
import com.silho.ideo.meetus.firebaseCloudMessaging.MyJobService;
import com.silho.ideo.meetus.utils.FontHelper;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static String mIdFacebook;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.container_frameLayout) FrameLayout mFrameLayout;
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.toolbar_title) TextView mToolBarTitle;

    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private boolean mAuthFlag;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        ButterKnife.bind(this);
        FontHelper.setCustomTypeface(mFrameLayout);
        JobManager.create(this).addJobCreator(new MyJobService());
        DemoSyncJob.scheduleJob();
        JobManager.instance().getConfig().setAllowSmallerIntervalsForMarshmallow(true);

        setSupportActionBar(mToolbar);
        mToolbar.setElevation(4.0f);
        MainActivity.this.setTitle("");

        String title = "Scheduler";
        mToolBarTitle.setText(title);

        if(isNetworkAvailable()) {
            login();
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
                                    .setAvailableProviders(Arrays.asList
                                            (new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
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
            parameters.putString("fields", "id,name,picture.type(large)");
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
        mIdFacebook = data.getString("id");
        String profilPic = data.getJSONObject("picture")
                .getJSONObject("data").getString("url");

        PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), 2, mIdFacebook, name, profilPic);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setCurrentItem(1, true);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 1){
                    mToolBarTitle.setText("Scheduler");
                } else if(position == 0){
                    mToolBarTitle.setText("Calendar");
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out) {
            AuthUI.getInstance().signOut(this);
        }
        if(id == R.id.renew){
            recreate();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
