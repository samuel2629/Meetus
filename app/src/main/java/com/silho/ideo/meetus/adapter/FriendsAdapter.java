package com.silho.ideo.meetus.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.activities.MainActivity;
import com.silho.ideo.meetus.model.User;
import com.silho.ideo.meetus.utils.CircleTransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Samuel on 03/08/2017.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    public static class FriendItem {
        final String id;
        final String name;
        final String image;

        public String getId() {
            return id;
        }

        public String getImage() {
            return image;
        }

        public String getName() {
            return name;
        }


        public FriendItem(String id, String name, String image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }
    }

    private final List<FriendItem> mValues;
    private ArrayList<FriendItem> arraylist;
    private FriendsAdapter.OnItemFrienClicked mListener;

    public interface OnItemFrienClicked{
        public void onItemFriendClicked(User user);
    }

    public FriendsAdapter(List<FriendItem> items, FriendsAdapter.OnItemFrienClicked listener) {
        mValues = items;
        arraylist = new ArrayList<>();
        arraylist.addAll(items);
        mListener = listener;
    }

    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_invited, parent, false);
        return new FriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FriendsAdapter.ViewHolder holder, int position) {
        holder.bindFriend(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final View mView;
        final TextView mName;
        final ImageView mProfilePic;
        final TextView mDurationFriend;
        private String mId;
        private String mUrlProfilPic;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.name);
            mProfilePic = (ImageView) view.findViewById(R.id.image);
            mDurationFriend = (TextView) view.findViewById(R.id.durationFriend);
            mDurationFriend.setVisibility(View.GONE);

            view.setOnClickListener(this);
        }

        public void bindFriend(FriendItem friendItem) {
            mName.setText(friendItem.name);
            displayProfilePic(mProfilePic, friendItem.image);
            mId = friendItem.id;
        }

        @Override
        public void onClick(View view) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    mListener.onItemFriendClicked(user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Toast.makeText(view.getContext(), mId, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayProfilePic(ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url)
                .thumbnail(1f).apply(RequestOptions
                .bitmapTransform(new CircleTransform(imageView.getContext())))
                .apply(RequestOptions
                        .diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(imageView);
    }


    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mValues.clear();
        if (charText.length() == 0) {
            mValues.addAll(arraylist);
        } else {
            for (FriendItem  wp : arraylist) {
                if (wp.name.toLowerCase(Locale.getDefault()).contains(charText)) {
                    mValues.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}