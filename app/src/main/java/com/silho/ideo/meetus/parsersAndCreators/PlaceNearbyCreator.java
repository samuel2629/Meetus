package com.silho.ideo.meetus.parsersAndCreators;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;

import com.silho.ideo.meetus.adapter.ItemNearbyAdapter;
import com.silho.ideo.meetus.model.PlaceNearby;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Samuel on 25/07/2017.
 */

public class PlaceNearbyCreator {

    private ItemNearbyAdapter mItemNearbyAdapter;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private ArrayList<Object> mPlacesNearby;

    public PlaceNearbyCreator(Context context, RecyclerView recyclerView){
        mContext = context;
        mRecyclerView = recyclerView;
    }

    public void initializeRecyclerviewAndAdapter(ItemNearbyAdapter.OnItemClicked c) {
        SnapHelper helper = new LinearSnapHelper();
        mItemNearbyAdapter = new ItemNearbyAdapter(mContext, new ArrayList<>(), c);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setOnFlingListener(null);
        helper.attachToRecyclerView(mRecyclerView);
    }

    public void getWebServicesPlaceApi(Location location, String type) {
        StringBuilder sbValue = new StringBuilder(stringBuilderPlaceNearby(location, type));
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(sbValue.toString());
    }

    public StringBuilder stringBuilderPlaceNearby(Location location, String type) {

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + location.getLatitude() + "," + location.getLongitude());
        sb.append("&radius=5000");
        sb.append("&types=" + type);
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyDuioRSRLgLzgBwGGaY3qPln411JJhRUIA");

        Log.d("Map", "api: " + sb.toString());

        return sb;
    }

    public StringBuilder stringBuilderPhoto(String photoRef){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        sb.append("maxwidth=400");
        sb.append("&photoreference=" + photoRef);
        sb.append("&key=AIzaSyDuioRSRLgLzgBwGGaY3qPln411JJhRUIA");
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

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJson = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            mPlacesNearby = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                PlaceNearby placeNearby = new PlaceNearby();

                HashMap<String, String> hmPlace = list.get(i);

                double lat = Double.parseDouble(hmPlace.get("lat"));
                double lng = Double.parseDouble(hmPlace.get("lng"));
                String name = hmPlace.get("place_name");
                String vicinity = hmPlace.get("vicinity");
                String photoRef = hmPlace.get("photoReference");

                placeNearby.setPhotoRefPlaceNearby(String.valueOf(stringBuilderPhoto(photoRef)));
                placeNearby.setNamePlaceNearby(name);
                placeNearby.setVincinityPlaceNearby(vicinity);
                placeNearby.setLatitude(lat);
                placeNearby.setLongitude(lng);
                mItemNearbyAdapter.add(placeNearby);
            }

            mRecyclerView.setAdapter(mItemNearbyAdapter);
        }
    }
}
