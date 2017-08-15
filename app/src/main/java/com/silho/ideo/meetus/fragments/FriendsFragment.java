package com.silho.ideo.meetus.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.silho.ideo.meetus.R;
import com.silho.ideo.meetus.adapter.FriendsAdapter;
import com.silho.ideo.meetus.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;


public class FriendsFragment extends Fragment implements SearchView.OnQueryTextListener, FriendsAdapter.OnItemFrienClicked{

    CallbackManager callbackManager;
    RecyclerView recyclerView;
    TextView emptyText;
    private FriendsAdapter mFriendsAdapter;
    private String mToken;
    private String mUrlProfilPic;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);

        emptyText = (TextView) view.findViewById(R.id.emptyText);
        emptyText.setVisibility(View.GONE);

        if (AccessToken.getCurrentAccessToken() == null) {
            getActivity().finish();
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.friendRecyclerView);
        callbackManager = CallbackManager.Factory.create();

        SearchView searchView = (SearchView) view.findViewById(R.id.searchFriend);
        searchView.setOnQueryTextListener(this);

        Set permissions = AccessToken.getCurrentAccessToken().getPermissions();
        if (permissions.contains("user_friends")) {
            fetchFriends();
        }
        else {
            LoginManager loginManager = LoginManager.getInstance();
            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    fetchFriends();
                }

                @Override
                public void onCancel() {
                    String permissionMsg = getResources().getString(R.string.permission_message);
                    Toast.makeText(getActivity(), permissionMsg, Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }

                @Override
                public void onError(FacebookException error) {}
            });
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_friends"));
        }
        return view;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void fetchFriends() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,picture");
        parameters.putInt("limit", 100);
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/friends",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() != null) {
                            Toast.makeText(getActivity(), response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<FriendsAdapter.FriendItem> friendList = new ArrayList<>();

                        JSONObject jsonResponse = response.getJSONObject();
                        try {
                            JSONArray jsonData = jsonResponse.getJSONArray("data");
                            for (int i=0; i<jsonData.length(); i++) {
                                JSONObject jsonUser = jsonData.getJSONObject(i);
                                String id = jsonUser.getString("id");
                                String name = jsonUser.getString("name");
                                String image = jsonUser.getJSONObject("picture").getJSONObject("data").getString("url");

                                FriendsAdapter.FriendItem friend = new FriendsAdapter.FriendItem(id, name, image);
                                friendList.add(friend);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mFriendsAdapter = new FriendsAdapter(friendList, FriendsFragment.this);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(mFriendsAdapter);

                        if (friendList.size() == 0) {
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    }
                }
        ).executeAsync();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        mFriendsAdapter.filter(text);
        return false;
    }

    @Override
    public void onItemFriendClicked(User user) {
        mToken = user.token;
        mUrlProfilPic = user.profilPic;
    }

    public String getToken() {
        return mToken;
    }

    public String getUrlProfilPic(){
        return mUrlProfilPic;
    }
}
