package com.silho.ideo.meetus.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.model.PlaceNearby;
import com.silho.ideo.meetus.utils.CircleTransform;
import com.silho.ideo.meetus.utils.FontHelper;

import java.util.ArrayList;

/**
 * Created by Samuel on 25/07/2017.
 */

public class ItemNearbyAdapter extends RecyclerView.Adapter<ItemNearbyAdapter.ItemNearbyViewHolder> {

    private final Context mContext;
    private ArrayList<PlaceNearby> mPlaceNearby;
    private ItemNearbyAdapter.OnItemClicked mListener;

    public interface OnItemClicked{
        void onItemClicked(double latitude, double longitude, String name);
    }

    public ItemNearbyAdapter(Context context, ArrayList<PlaceNearby> places, OnItemClicked listener){
        mContext = context;
        mPlaceNearby = places;
        mListener = listener;
    }

    @Override
    public ItemNearbyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nearby, parent, false);
        return new ItemNearbyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemNearbyViewHolder holder, int position) {
        //FontHelper.setCustomTypeface(holder.itemView);
        holder.bindPlace(mPlaceNearby.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlaceNearby.size();
    }

    public void add(PlaceNearby place){
        mPlaceNearby.add(place);
    }

    public void clear(){
        mPlaceNearby.clear();
    }

    public class ItemNearbyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mNameItem, mVincinityItem;
        private ImageView mPhotoImageView;
        private PlaceNearby mNearByPlace;

        public ItemNearbyViewHolder(View itemView) {
            super(itemView);

            mNameItem = (TextView) itemView.findViewById(R.id.nameItem);
            mVincinityItem = (TextView) itemView.findViewById(R.id.vicinityItem);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.photoPlaceNearby);

            itemView.setOnClickListener(this);
        }

        public void bindPlace(PlaceNearby placeNearby) {
            mNearByPlace = placeNearby;
            mNameItem.setText(placeNearby.getNamePlaceNearby());
            mVincinityItem.setText(placeNearby.getVincinityPlaceNearby());
            Glide.with(mContext).load(placeNearby.getPhotoRefPlaceNearby())
                    .thumbnail(1f)
                    .apply(RequestOptions.bitmapTransform(new CircleTransform(mContext)))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(mPhotoImageView);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClicked(mNearByPlace.getLatitude(), mNearByPlace.getLongitude(),
                    mNearByPlace.getNamePlaceNearby());
        }
    }
}
