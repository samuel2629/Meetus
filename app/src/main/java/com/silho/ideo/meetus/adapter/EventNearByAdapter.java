package com.silho.ideo.meetus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.model.EventfulResponse;
import com.silho.ideo.meetus.utils.FontHelper;

/**
 * Created by Samuel on 30/11/2017.
 */

public class EventNearByAdapter extends RecyclerView.Adapter<EventNearByAdapter.ItemEventNearbyViewHolder> {

    private EventfulResponse mEventfulResponse;

    public EventNearByAdapter(EventfulResponse eventfulResponse){
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
        return mEventfulResponse.event.mEvents.size();
    }

    public class ItemEventNearbyViewHolder extends RecyclerView.ViewHolder {

        private TextView title, description, date, placeName;

        public ItemEventNearbyViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.eventNearByTitle);
            description = itemView.findViewById(R.id.eventNearByDescription);
            date = itemView.findViewById(R.id.eventNearByDate);
            placeName = itemView.findViewById(R.id.eventNearByPlace);
        }

        public void bindEvent(EventfulResponse.Event event) {
            description.setVisibility(View.GONE);
            title.setText(event.getTitle());
            //description.setText(event.getDescription());
            date.setText(event.getStartTime());
            placeName.setText(event.getPlaceName()+ ", " + event.getAddress());
        }
    }
}
