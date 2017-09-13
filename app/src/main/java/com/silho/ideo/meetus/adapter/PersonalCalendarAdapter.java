package com.silho.ideo.meetus.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.activities.InvitationResumerActivity;
import com.silho.ideo.meetus.firebaseCloudMessaging.MyFirebaseMessagingService;
import com.silho.ideo.meetus.model.ScheduledEvent;
import com.silho.ideo.meetus.utils.FontHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Samuel on 24/08/2017.
 */

public class PersonalCalendarAdapter extends RecyclerView.Adapter<PersonalCalendarAdapter.PersonalCalendarViewHolder> {

    private Context mContext;
    private ArrayList<ScheduledEvent> mScheduledEvents;

    public PersonalCalendarAdapter(Context context, ArrayList<ScheduledEvent> scheduledEvents){
        mContext = context;
        mScheduledEvents = scheduledEvents;
    }

    @Override
    public PersonalCalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personal_calendar, parent, false);
        return new PersonalCalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PersonalCalendarViewHolder holder, int position) {
        FontHelper.setCustomTypeface(holder.itemView);
        holder.bindEvent(mScheduledEvents.get(position));
    }

    @Override
    public int getItemCount() {
        return mScheduledEvents.size();
    }

    public void add(ScheduledEvent scheduledEvent){
        mScheduledEvents.add(scheduledEvent);
        notifyDataSetChanged();
    }

    public void clear(){
        mScheduledEvents.clear();
    }

    public class PersonalCalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mDateView, mTimeView, mPlaceNameView;
        private long mTime;
        private double mLatDest, mLongDest;
        private String mPlaceName;

        public PersonalCalendarViewHolder(View itemView) {
            super(itemView);

            mDateView = (TextView) itemView.findViewById(R.id.dateTextView);
            mTimeView = (TextView) itemView.findViewById(R.id.timeTextView);
            mPlaceNameView = (TextView) itemView.findViewById(R.id.placeTextView);

            itemView.setOnClickListener(this);
        }

        public void bindEvent(ScheduledEvent scheduledEvent) {
            mTime = scheduledEvent.getTp();
            mLatDest = scheduledEvent.getLat();
            mLongDest = scheduledEvent.getLong();
            mPlaceName = scheduledEvent.getPlaceName();

            String date = getDate(scheduledEvent.getTp()*1000L);
            String dayAndMonth = getDayAndMonth(scheduledEvent.getTp()*1000L);
            mDateView.setText(dayAndMonth);
            mTimeView.setText(date);
            mPlaceNameView.setText(scheduledEvent.getPlaceName());
        }

        private String getDate(long time) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeInMillis(time);
            return DateFormat.format("EEE d MMM yyyy HH:mm", cal).toString();
        }

        private String getDayAndMonth(long time) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeInMillis(time);
            return DateFormat.format("dd/MM", cal).toString();
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, InvitationResumerActivity.class);
            intent.putExtra(MyFirebaseMessagingService.TIME, mTime);
            intent.putExtra(MyFirebaseMessagingService.LATITUDE_DEST, mLatDest);
            intent.putExtra(MyFirebaseMessagingService.LONGITUDE_DEST, mLongDest);
            intent.putExtra(MyFirebaseMessagingService.PLACE_NAME, mPlaceName);
            mContext.startActivity(intent);
        }
    }
}
