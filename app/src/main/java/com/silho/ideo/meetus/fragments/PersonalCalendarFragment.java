package com.silho.ideo.meetus.fragments;

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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.activities.MainActivity;
import com.silho.ideo.meetus.adapter.PersonalCalendarAdapter;
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

    private PersonalCalendarAdapter adapter;
    private DatabaseReference mDatabaseReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_calendar, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new PersonalCalendarAdapter(getContext(), new ArrayList<ScheduledEvent>());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(adapter);
        mDatabaseReference =  FirebaseDatabase.getInstance().getReference().child("users")
                .child(MainActivity.mIdFacebook).child("scheduledEvent");

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ScheduledEvent scheduledEvent = dataSnapshot.getValue(ScheduledEvent.class);
                adapter.add(scheduledEvent);
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
        adapter.notifyDataSetChanged();
        mDatabaseReference.orderByChild("tp").addChildEventListener(childEventListener);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.friend_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
