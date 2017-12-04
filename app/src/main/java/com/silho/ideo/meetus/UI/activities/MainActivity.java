package com.silho.ideo.meetus.UI.activities;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silho.ideo.meetus.UI.fragments.EventsNearByFragment;
import com.silho.ideo.meetus.UI.fragments.PersonalCalendarFragment;
import com.silho.ideo.meetus.adapter.PageAdapter;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.controller.alarmManager.ReminderScheduler;
import com.silho.ideo.meetus.utils.FontHelper;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.container_frameLayout) FrameLayout mFrameLayout;
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.toolbar_title) TextView mToolBarTitle;

    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private boolean mAuthFlag;
    private int mPagePosition = 1;
    private MenuItem mapMenuItem;
    private MenuItem listMenuItem;
    private boolean isOnMapItemMenuClicked;
    private MenuItem renewMenuItem;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        ButterKnife.bind(this);
        FontHelper.setCustomTypeface(mFrameLayout);

        isOnMapItemMenuClicked = false;

        setSupportActionBar(mToolbar);
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ){mToolbar.setElevation(4.0f);}
        MainActivity.this.setTitle("");

        String title = "Scheduler";
        mToolBarTitle.setText(title);

        if(isNetworkAvailable()) {
            login();
        }
    }

    /** Data Methods **/

    private void login() {
        mAuth = FirebaseAuth.getInstance();
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
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(Arrays.asList
                                        (new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                .setTheme(R.style.LoginTheme)
                                .build(),
                        RC_SIGN_IN);
            }
        };
    }

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
        }

        else {
            System.out.println("Access Token NULL");
        }
    }

    private void setUserDataUI(JSONObject data) throws JSONException {
        String name = data.getString("name");
        String profilPic = data.getJSONObject("picture")
                .getJSONObject("data").getString("url");

        PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), 2, name, profilPic);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setCurrentItem(1, true);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageSelected(int position) {
                mPagePosition = position;
                if(position == 0) {
                    mToolBarTitle.setText(R.string.calendar_viewpager_title);
                } else if(position == 1){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grouped_menu, menu);
        mapMenuItem = menu.findItem(R.id.map_item);
        listMenuItem = menu.findItem(R.id.list_item_menu);
        renewMenuItem = menu.findItem(R.id.renew);

        if(mPagePosition == 1){
            menu.findItem(R.id.renew).setVisible(true);
            menu.findItem(R.id.map_item).setVisible(false);
            menu.findItem(R.id.list_item_menu).setVisible(false);
        } else if(mPagePosition == 0){
            if(isOnMapItemMenuClicked){
                listMenuAvailable();
            } else {
                mapMenuAvailable();
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void listMenuAvailable() {
        renewMenuItem.setVisible(false);
        mapMenuItem.setVisible(false);
        listMenuItem.setVisible(true);
    }

    private void mapMenuAvailable() {
        renewMenuItem.setVisible(false);
        mapMenuItem.setVisible(true);
        listMenuItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.renew:
                recreate();
                return true;
            case R.id.map_item:
                mapMenuItem.setVisible(false);
                listMenuItem.setVisible(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.container_frameLayout, new EventsNearByFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                listMenuAvailable();
                isOnMapItemMenuClicked = true;
                return true;
            case R.id.list_item_menu:
                mapMenuItem.setVisible(false);
                listMenuItem.setVisible(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.container_frameLayout, new PersonalCalendarFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                mapMenuAvailable();
                isOnMapItemMenuClicked = false;
                return true;
            case R.id.log_out:
                AuthUI.getInstance().signOut(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
