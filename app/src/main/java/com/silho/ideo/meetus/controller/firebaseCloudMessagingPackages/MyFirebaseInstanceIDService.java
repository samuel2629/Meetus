package com.silho.ideo.meetus.controller.firebaseCloudMessagingPackages;

import android.util.Log;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import androidx.annotation.NonNull;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Samuel on 08/08/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onNewToken(@NonNull String s) {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }


    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
            SyncHttpClient client = new SyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("token", token);
            client.post("https://meetusite.herokuapp.com/send", params,
                    new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            Log.d(TAG, responseString);
                        }
                    });
        }
    }
