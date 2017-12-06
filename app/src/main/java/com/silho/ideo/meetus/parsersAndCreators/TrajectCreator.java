package com.silho.ideo.meetus.parsersAndCreators;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Samuel on 27/07/2017.
 */

public class TrajectCreator {

    private Context mContext;
    private static TextView mDurationView;

    public interface AsyncResponseDuration {
        void taskParsedAndOutput(String output);
    }

    public interface AsyncResponse {
        void parsingTask(String output);
    }

    public TrajectCreator(Context context, TextView durationView){
        mContext = context;
        mDurationView = durationView;}

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

    public static class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;
        public AsyncResponse delegate = null;

        public PlacesTask(AsyncResponse delegate){
            this.delegate = delegate;
        }

        public PlacesTask(){}

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
            if(delegate != null){
                delegate.parsingTask(result);
            }
        }
    }

    public static class ParserTask extends AsyncTask<String, Integer, Void> {

        private String mDuration;
        JSONObject jObject;

        public AsyncResponseDuration delegate = null;
        private String mDurationInSeconds;

        public ParserTask(AsyncResponseDuration delegate){
            this.delegate = delegate;
        }

        public ParserTask(){}

        @Override
        protected Void doInBackground(String... jsonData) {

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONArray row = jObject.getJSONArray("rows");
                JSONObject elements = row.getJSONObject(0);
                JSONArray element = elements.getJSONArray("elements");
                JSONObject distanceAndTime = element.getJSONObject(0);
                JSONObject duration = distanceAndTime.getJSONObject("duration");
                mDuration = duration.getString("text");
                mDurationInSeconds = duration.getString("value");


            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String duration = "Duration : " + mDuration;
            if(mDurationView != null){
                mDurationView.setText(duration);
            }
            if(delegate != null){
                delegate.taskParsedAndOutput(mDurationInSeconds);
            }
        }
    }

}
