package com.silho.ideo.meetus.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.EventResumerActivity;
import com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.model.User;
import com.silho.ideo.meetus.utils.FontHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Samuel on 02/12/2017.
 */

public class PersonalCalendarAdapter extends RecyclerView.Adapter<PersonalCalendarAdapter.ViewHolder> {

    private Context mContext;

    public ArrayList<ScheduledEvent> getScheduledEvents() {
        return mScheduledEvents;
    }

    private ArrayList<ScheduledEvent> mScheduledEvents;

    public PersonalCalendarAdapter(Context context, ArrayList<ScheduledEvent> scheduledEvents){
        mContext = context;
        mScheduledEvents = scheduledEvents;}


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personal_calendar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindEvent(position);
        FontHelper.setCustomTypeface(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return mScheduledEvents.size();
    }

    public void add(List<ScheduledEvent> scheduledEvent){
        mScheduledEvents.addAll(scheduledEvent);
        notifyDataSetChanged();
    }

    public void clear(){
        mScheduledEvents.clear();
    }

    public void add(ScheduledEvent scheduledEvent) {
        mScheduledEvents.add(scheduledEvent);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mDateView, mTimeView, mPlaceNameView, mFriendsTextView;
        private ImageView mImageView;
        private LinearLayout mLinearLayout;
        private long mTime;
        private double mLatDest, mLongDest;
        private String mPlaceName;
        private boolean mIsScheduled;
        private int mTransportType;
        private ArrayList<User> mFriends;


        public ViewHolder(View itemView) {
            super(itemView);

            mFriendsTextView = itemView.findViewById(R.id.friendsTextView);
            mImageView = itemView.findViewById(R.id.confirmedDateImageView);
            mDateView = itemView.findViewById(R.id.dateTextView);
            mTimeView = itemView.findViewById(R.id.timeTextView);
            mPlaceNameView = itemView.findViewById(R.id.placeTextView);
            mLinearLayout = itemView.findViewById(R.id.linearLayoutItemPersonalCalendar);


            itemView.setOnClickListener(this);
        }

        public void bindEvent(ScheduledEvent scheduledEvent) {
            mTime = scheduledEvent.getTimestamp();
            mLatDest = scheduledEvent.getLatitude();
            mLongDest = scheduledEvent.getLongitude();
            mPlaceName = scheduledEvent.getPlaceName();
            mIsScheduled = scheduledEvent.getIsScheduled();
            mFriends = scheduledEvent.getUsers();
            mTransportType = scheduledEvent.getTransportType();

            if(mIsScheduled){
                mImageView.setImageResource(R.drawable.ic_fiber_manual_record_green_24dp);
            } else {
                mImageView.setImageResource(R.drawable.ic_fiber_manual_record_red_24dp);
            }

            if(mFriends == null){
                mFriendsTextView.setVisibility(View.GONE);
            } else {
                mFriendsTextView.setVisibility(View.VISIBLE);
                ArrayList<String> names = new ArrayList<>();
                for(User user:mFriends){
                    String name = user.getName();
                    names.add(name);
                }
                Object [] strings = names.toArray();
                mFriendsTextView.setText(Arrays.toString(strings).replaceAll("\\[|\\]", ""));
            }

            String date = getDate(mTime*1000L);
            String dayAndMonth = getDayAndMonth(mTime*1000L);
            mDateView.setText(dayAndMonth);
            mTimeView.setText(date);
            mPlaceNameView.setText(scheduledEvent.getPlaceName());
        }

        private String getDate(long time) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeInMillis(time);
            return DateFormat.format("EEE d MMM yyyy 'at' HH:mm", cal).toString();
        }

        private String getDayAndMonth(long time) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeInMillis(time);
            return DateFormat.format("dd/MM", cal).toString();
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, EventResumerActivity.class);
            intent.putExtra(MyFirebaseMessagingService.TIME, mTime);
            intent.putExtra(MyFirebaseMessagingService.LATITUDE_DEST, mLatDest);
            intent.putExtra(MyFirebaseMessagingService.LONGITUDE_DEST, mLongDest);
            intent.putExtra(MyFirebaseMessagingService.PLACE_NAME, mPlaceName);
            intent.putExtra(MyFirebaseMessagingService.TRANSPORT_TYPE, mTransportType);
            if(mFriends != null){
                intent.putExtra(MyFirebaseMessagingService.FRIENDS_LIST_INVITED, mFriends);
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Bundle optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) mContext,mLinearLayout,"card_transition").toBundle();
                mContext.startActivity(intent, optionsCompat);
            } else {
                mContext.startActivity(intent);
            }
        }

        public void bindEvent(int position) {
            bindEvent(mScheduledEvents.get(position));
        }
    }
}
