package com.silho.ideo.meetus.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Samuel on 27/07/2017.
 */

public class TrajectCreator {

    private Context mContext;
    private TextView mDurationView, mDistanceView;

    public TrajectCreator(Context context, TextView durationView, TextView distanceView){
        mContext = context;
        mDurationView = durationView;
        mDistanceView = distanceView;
    }

    public void getWebServicesPlaceApi(double myLatitude,
                                       double myLongitude,
                                       double latitudeArrival,
                                       double longitudeArrival,
                                       String type) {
        StringBuilder sbValue = new StringBuilder(stringBuilderPlaceDestination
                (myLatitude, myLongitude, latitudeArrival, longitudeArrival, type));
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(sbValue.toString());
    }

    public StringBuilder stringBuilderPlaceDestination(double myLatitude,
                                                       double myLongitude,
                                                       double latitudeArrival,
                                                       double longitudeArrival,
                                                       String type) {

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?");
        sb.append("origins=" +myLatitude + "," + myLongitude);
        sb.append("&destinations=" + latitudeArrival + "," + longitudeArrival);
        sb.append("&mode=" + type);
        sb.append("&key=AIzaSyDuioRSRLgLzgBwGGaY3qPln411JJhRUIA");

        Log.d("Map", "api: " + sb.toString());

        return sb;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception dwling url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, Void> {

        JSONObject jObject;
        private String mDistance;
        private String mDuration;

        @Override
        protected Void doInBackground(String... jsonData) {

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONArray row = jObject.getJSONArray("rows");
                JSONObject elements = row.getJSONObject(0);
                JSONArray element = elements.getJSONArray("elements");
                JSONObject distanceAndTime = element.getJSONObject(0);
                JSONObject distance = distanceAndTime.getJSONObject("distance");
                JSONObject duration = distanceAndTime.getJSONObject("duration");
                mDistance = distance.getString("text");
                mDuration = duration.getString("text");


            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String distance = "Distance : " + mDistance;
            String duration = "Duration : " + mDuration;
            mDistanceView.setText(distance);
            mDurationView.setText(duration);
        }
    }

}
