package com.silho.ideo.meetus.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.UI.activities.EventResumerActivity;
import com.silho.ideo.meetus.UI.activities.MainActivity;
import com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages.MyFirebaseMessagingService;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.model.User;
import com.silho.ideo.meetus.utils.FontHelper;
import com.truizlop.sectionedrecyclerview.SimpleSectionedAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Samuel on 15/09/2017.
 */

public class PersonalCalendarAdapterSectioned extends SimpleSectionedAdapter<PersonalCalendarAdapterSectioned.ViewHolder> {


    private Context mContext;
    private ArrayList<ScheduledEvent> mScheduledEvents;
    private long mActualTime;
    private ArrayList<ScheduledEvent> mTodayEvents;
    private ArrayList<ScheduledEvent> mTomorrowEvents;
    private DatabaseReference mDatabase;

    public PersonalCalendarAdapterSectioned(Context context, ArrayList<ScheduledEvent> scheduledEvents, long actualTime){
        mContext = context;
        mScheduledEvents = scheduledEvents;
        mActualTime = actualTime;
        mTodayEvents = new ArrayList<>();
        mTomorrowEvents = new ArrayList<>();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.header_title;
    }

    @Override
    protected int getTitleTextID() {
        return R.id.header_text;
    }

    @Override
    protected String getSectionHeaderTitle(int section) {
        switch (section) {
            case 0:
                if(mTodayEvents.isEmpty()){
                    return "Nothing Scheduled For Today.";
                }
                return "Today";
            case 1:
                if(mTomorrowEvents.isEmpty()){
                    return null;
                }
                return "Tomorrow";
            case 2:
                if(mScheduledEvents.isEmpty()){
                    return null;
                }
                return "Later";
            default:
                return "Later";
        }
    }

    @Override
    protected int getSectionCount() {
        return 3;
    }

    @Override
    protected int getItemCountForSection(int section) {
        switch (section){
            case 0:
                return mTodayEvents.size();
            case 1:
                return mTomorrowEvents.size();
            case 2:
                return mScheduledEvents.size();
            default:
                return mScheduledEvents.size();
        }
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personal_calendar, parent, false);
        return new PersonalCalendarAdapterSectioned.ViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, final int section, final int position) {
        holder.bindEvent(section, position);
        //FontHelper.setCustomTypeface(holder.itemView);
        holder.itemView.setOnLongClickListener(view -> {deleteDialogFragment(section, position).show();
            return true;
        });
    }

    private Dialog deleteDialogFragment(final int section, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure ?");

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(Profile.getCurrentProfile().getId()).child("scheduledEvent");
            if(section == 0){
                mDatabase.child(Long.toString(mTodayEvents.get(position).getTimestamp())).removeValue();
                mTodayEvents.remove(position);
                notifyDataSetChanged();
            } else if (section == 1){
                mDatabase.child(Long.toString(mTomorrowEvents.get(position).getTimestamp())).removeValue();
                mTomorrowEvents.remove(position);
                notifyDataSetChanged();
            } else {
                mDatabase.child(Long.toString(mScheduledEvents.get(position).getTimestamp())).removeValue();
                mScheduledEvents.remove(position);
                notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        return builder.create();
    }

    public void add(ScheduledEvent scheduledEvent){
        if(mActualTime<scheduledEvent.getTimestamp() && scheduledEvent.getTimestamp()<mActualTime+86400){
            mTodayEvents.add(scheduledEvent);
        } else if (mActualTime+86400<scheduledEvent.getTimestamp() && scheduledEvent.getTimestamp()<mActualTime+172800){
            mTomorrowEvents.add(scheduledEvent);
        } else if (scheduledEvent.getTimestamp()>mActualTime+172800){
            mScheduledEvents.add(scheduledEvent);
        }
        notifyDataSetChanged();
    }

    public void clear(){
        mTodayEvents.clear();
        mTomorrowEvents.clear();
        mScheduledEvents.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mDateView, mTimeView, mPlaceNameView, mFriendsTextView;
        private ImageView mImageView;
        private CardView mCardView;
        private long mTime;
        private double mLatDest, mLongDest;
        private String mPlaceName;
        private boolean mIsScheduled;
        private int mTransportType;
        private ArrayList<User> mFriends;


        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = itemView.findViewById(R.id.card_item_calendar);
            mFriendsTextView = itemView.findViewById(R.id.friendsTextView);
            mImageView = itemView.findViewById(R.id.confirmedDateImageView);
            mDateView = itemView.findViewById(R.id.dateTextView);
            mTimeView = itemView.findViewById(R.id.timeTextView);
            mPlaceNameView = itemView.findViewById(R.id.placeTextView);


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
                        (Activity) mContext,mCardView,"card_transition").toBundle();
                mContext.startActivity(intent, optionsCompat);
            } else {
                mContext.startActivity(intent);
            }
        }

        public void bindEvent(int section, int position) {
            if (section == 0){
                bindEvent(mTodayEvents.get(position));
            } else if (section == 1){
                bindEvent(mTomorrowEvents.get(position));
            } else {
                bindEvent(mScheduledEvents.get(position));
            }
        }
    }
}
