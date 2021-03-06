package com.silho.ideo.meetus.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.silho.ideo.meetus.model.User;
import com.silho.ideo.meetus.utils.CircleTransform;
import com.silho.ideo.meetus.utils.FontHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
    private boolean mIsOnClickActivated;

    public interface OnItemFrienClicked{
        void onItemFriendClicked(User user, String id);
    }

    public FriendsAdapter(List<FriendItem> items, FriendsAdapter.OnItemFrienClicked listener) {
        mValues = items;
        arraylist = new ArrayList<>();
        arraylist.addAll(items);
        mListener = listener;
        mIsOnClickActivated = true;
    }

    public FriendsAdapter(List<FriendItem> items){
        mValues = items;
        arraylist = new ArrayList<>();
        arraylist.addAll(items);
        mIsOnClickActivated = false;
    }

    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_invited, parent, false);
        return new FriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FriendsAdapter.ViewHolder holder, int position) {
        //FontHelper.setCustomTypeface(holder.mView);
        final FriendItem friendItem = mValues.get(position);
        holder.mName.setText(friendItem.name);
        displayProfilePic(holder.mProfilePic, friendItem.image);

        Context c = holder.mView.getContext();
        SharedPreferences prefs = c.getSharedPreferences(Profile.getCurrentProfile().getId(),0);
        boolean isFollowing = prefs.getBoolean(friendItem.id, false);
        updateFollowButton((LinearLayout)holder.mView, holder.mProfilePic, holder.mName, isFollowing, c);

        if(mIsOnClickActivated){
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context c = view.getContext();
                String userID = Profile.getCurrentProfile().getId();
                SharedPreferences prefs = c.getSharedPreferences(userID, 0);
                SharedPreferences.Editor editor = prefs.edit();

                // switch following state for the given friend ID
                boolean isFollowing = prefs.getBoolean(friendItem.id, false);
                editor.putBoolean(friendItem.id, !isFollowing);
                editor.apply();

                updateFollowButton((LinearLayout)holder.mView, holder.mProfilePic, holder.mName, !isFollowing, c);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(friendItem.id);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        mListener.onItemFriendClicked(user, friendItem.id);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });}

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final View mView;
        final TextView mName;
        final ImageView mProfilePic;

        public ViewHolder(View view) {
            super(view);

            mView = view;
            mName = view.findViewById(R.id.name);
            mProfilePic = view.findViewById(R.id.image);
        }
    }

    private void updateFollowButton(LinearLayout buttonView, ImageView imageView, TextView textView, boolean isFollowing, Context c) {
        if (isFollowing) {
            buttonView.setBackgroundResource(R.color.colorFade);
            textView.setTextColor(ContextCompat.getColor(c, R.color.colorPrimary));
        }
        else {
            buttonView.setBackgroundResource(R.color.colorBackground);
            imageView.setVisibility(View.VISIBLE);
            textView.setTextColor(ContextCompat.getColor(c, R.color.colorSecondary));
        }
    }

    private void displayProfilePic(ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url)
                .thumbnail(1f).apply(RequestOptions
                .bitmapTransform(new CircleTransform()))
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