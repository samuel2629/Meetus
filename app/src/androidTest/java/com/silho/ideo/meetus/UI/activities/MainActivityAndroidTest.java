package com.silho.ideo.meetus.UI.activities;

import android.os.Build;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.silho.ideo.meetus.R;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;


import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;


/**
 * Created by Samuel on 21/11/2017.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityAndroidTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Before
    public void grantPhoneLocalisation() {
        // In M+, trying to call a number will trigger a runtime dialog. Make sure
        // the permission is granted before running this test.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.ACCESS_FINE_LOCATION");
        }
    }

    /*@Test
    public void facebookLoginButtonIsWorking() throws Exception{
        onView(withText("Se connecter avec Facebook")).check(matches(isDisplayed()));
        onView(withText("Se connecter avec Facebook")).perform(click());
    }*/

    @Test
    public void placeTypeIsWorking(){
        onView(withId(R.id.visitTypeFAB)).perform(click());
        onView(withId(R.id.recyclerViewItemNearby)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.nameItem)).check(matches(withText("Le Prado")));
    }

    @Test
    public void roadItinareryIsWorkingDependingOnPlaceType() throws Exception{
        placeTypeIsWorking();
        onView(withId(R.id.walkingFAB)).perform(click());
        onView(withId(R.id.durationTextView)).check(matches(withText(startsWith("Duration"))));
    }

    @Test
    public void chooseFriendIsWorking() throws Exception{
        onView(withId(R.id.scheduleButton)).perform(scrollTo());
        onView(withId(R.id.friendRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    @Test
    public void pickADateIsWorking() throws Exception{
        onView(withId(R.id.scheduleButton)).perform(scrollTo(), click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2017, 12,17 ));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void pickATimeIsWorkingDependingOnPickDateIsWorking() throws Exception{
        pickADateIsWorking();
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(20, 30));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void navigateBetweenFragmentsIsWorking() throws Exception{
        onView(withId(R.id.pager)).perform(swipeRight());
        onView(withId(R.id.toolbar_title)).check(matches(withText("Calendar")));
    }

    @Test
    public void scheduleAnEventIsWorking() throws Exception{
        roadItinareryIsWorkingDependingOnPlaceType();
        chooseFriendIsWorking();
        pickATimeIsWorkingDependingOnPickDateIsWorking();
        navigateBetweenFragmentsIsWorking();
        
    }
}