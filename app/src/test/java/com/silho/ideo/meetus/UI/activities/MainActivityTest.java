package com.silho.ideo.meetus.UI.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Build;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.silho.ideo.meetus.BuildConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Samuel on 21/11/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
public class MainActivityTest {

    MainActivity mActivity;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        FirebaseApp.initializeApp(RuntimeEnvironment.application);
        mActivity = Robolectric.setupActivity(MainActivity.class);
        assertNotNull(mActivity);
    }

    @Test
    public void isTitleToolbarIsOK() throws Exception{
        String givenString = "Scheduler";
        String actualString  = mActivity.mToolBarTitle.getText().toString();
        assertEquals(givenString, actualString);
    }

    @Test
    public void isNetworkAvailable() throws Exception {
        boolean givenBoolean = true;
        boolean actualBoolean = mActivity.isNetworkAvailable();
        assertEquals(givenBoolean, actualBoolean);
    }

}