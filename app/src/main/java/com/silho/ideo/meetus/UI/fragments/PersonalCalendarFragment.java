package com.silho.ideo.meetus.UI.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Profile;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.MainActivity;
import com.silho.ideo.meetus.adapter.PersonalCalendarAdapterSectioned;
import com.silho.ideo.meetus.model.ScheduledEvent;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Samuel on 24/08/2017.
 */

public class PersonalCalendarFragment extends Fragment {

    @BindView(R.id.reyclerViewCalendar)
    RecyclerView mRecyclerView;

    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private PersonalCalendarAdapterSectioned mAdapterSectioned;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_personal_calendar, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        mAdapterSectioned = new PersonalCalendarAdapterSectioned(getContext(), new ArrayList<>(),System.currentTimeMillis()/1000L);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapterSectioned);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(Profile.getCurrentProfile().getId()).child("scheduledEvent");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ScheduledEvent scheduledEvent = dataSnapshot.getValue(ScheduledEvent.class);
                mAdapterSectioned.add(scheduledEvent);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && mAdapterSectioned != null){
            mAdapterSectioned.clear();
            mDatabaseReference.orderByChild("tp").addChildEventListener(mChildEventListener);
            mAdapterSectioned.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.friend_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
