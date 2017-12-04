package com.silho.ideo.meetus.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.model.EventfulResponse;
import com.silho.ideo.meetus.utils.FontHelper;

import java.util.ArrayList;

/**
 * Created by Samuel on 30/11/2017.
 */

public class EventNearByAdapter extends RecyclerView.Adapter<EventNearByAdapter.ItemEventNearbyViewHolder> {

    private EventfulResponse mEventfulResponse;
    private EventNearByAdapter.OnMapSet mListener;

    public interface OnMapSet{
        void onMapSet(ArrayList<Float> latitude, ArrayList<Float> longitude, ArrayList<String> title);
    }

    public EventNearByAdapter(EventfulResponse eventfulResponse, EventNearByAdapter.OnMapSet listener){
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

    public class ItemEventNearbyViewHolder extends RecyclerView.ViewHolder {

        private TextView title, description, date, placeName;

        public ItemEventNearbyViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.eventNearByTitle);
            description = itemView.findViewById(R.id.eventNearByDescription);
            date = itemView.findViewById(R.id.eventNearByDate);
            placeName = itemView.findViewById(R.id.eventNearByPlace);
            itemView.getId();
        }

        public void bindEvent(EventfulResponse.Event event) {
            description.setVisibility(View.GONE);
            title.setText(event.getTitle());
            date.setText(event.getStartTime());
            placeName.setText(event.getPlaceName()+ ", " + event.getAddress());
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
    }
}
