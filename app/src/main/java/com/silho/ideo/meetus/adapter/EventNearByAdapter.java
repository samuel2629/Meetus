package com.silho.ideo.meetus.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.fragments.FriendsFragment;
import com.silho.ideo.meetus.model.EventfulResponse;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.model.User;
import com.silho.ideo.meetus.utils.FontHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Samuel on 30/11/2017.
 */

public class EventNearByAdapter extends RecyclerView.Adapter<EventNearByAdapter.ItemEventNearbyViewHolder> {

    private final Context context;
    private EventfulResponse mEventfulResponse;
    private EventNearByAdapter.OnMapSet mListener;

    public interface OnMapSet{
        void onMapSet(ArrayList<Float> latitude, ArrayList<Float> longitude, ArrayList<String> title);
    }

    public EventNearByAdapter(Context context, EventfulResponse eventfulResponse, EventNearByAdapter.OnMapSet listener){
        this.context = context;
        mListener = listener;
        mEventfulResponse = eventfulResponse;
    }

    @Override
    public ItemEventNearbyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_nearby, parent, false);
        return new ItemEventNearbyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemEventNearbyViewHolder holder, int position) {
        FontHelper.setCustomTypeface(holder.itemView);
        holder.bindEvent(mEventfulResponse.event.mEvents.get(position));
    }

    @Override
    public int getItemCount() {
        if(mEventfulResponse.event.mEvents != null) {
            return mEventfulResponse.event.mEvents.size();
        } else {
            return 0;
        }
    }

    public class ItemEventNearbyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView title, description, date, placeName;
        private long mTime;
        private String mNamePlace;
        private float mLatitudeDestination;
        private float mLongitudeDestination;
        private int mTransportType;

        public ItemEventNearbyViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.eventNearByTitle);
            description = itemView.findViewById(R.id.eventNearByDescription);
            date = itemView.findViewById(R.id.eventNearByDate);
            placeName = itemView.findViewById(R.id.eventNearByPlace);

            itemView.setOnClickListener(this);
        }

        public void bindEvent(EventfulResponse.Event event) {
            String time = event.getStartTime();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = null;
            try {
                d = formatter.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(d != null) mTime = d.getTime() / 1000;
            mNamePlace = event.getPlaceName();
            mLatitudeDestination = event.getLatitude();
            mLongitudeDestination = event.getLongitude();
            mTransportType = 0;

            description.setVisibility(View.GONE);
            title.setText(event.getTitle());
            date.setText(event.getStartTime());
            placeName.setText(event.getPlaceName());
            passDataToFragment();
        }

        private void passDataToFragment() {
            ArrayList<Float> latitudes = new ArrayList<>();
            ArrayList<Float> longitudes = new ArrayList<>();
            ArrayList<String> titles = new ArrayList<>();
            for (EventfulResponse.Event e: mEventfulResponse.event.mEvents) {
                latitudes.add(e.getLatitude());
                longitudes.add(e.getLongitude());
                titles.add(e.getTitle());
            }
            mListener.onMapSet(latitudes, longitudes, titles);
        }

        @Override
        public void onClick(View view) {
            Dialog dialog = onCreateDialog();
            dialog.show();
        }

        public Dialog onCreateDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle(R.string.pick_an_action)
                    .setItems(R.array.colors_array, (dialog, which) -> {
                        if (which == 0) {
                            setEvent(null);
                        } else {
                        }
                    });
            return builder.create();
        }

        private void setEvent(ArrayList<User> friends) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(Profile.getCurrentProfile().getId()).child("scheduledEvent");
            ScheduledEvent scheduledEvent = new ScheduledEvent(mTime, mNamePlace, mLatitudeDestination, mLongitudeDestination, true, friends, mTransportType);
            databaseReference.child(Long.toString(mTime)).setValue(scheduledEvent);
            Toast.makeText(itemView.getContext(), "Event Created Successfully", Toast.LENGTH_SHORT).show();
        }
    }
}
