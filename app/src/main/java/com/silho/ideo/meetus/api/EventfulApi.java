package com.silho.ideo.meetus.api;

import android.support.annotation.Nullable;

import com.silho.ideo.meetus.model.EventfulResponse;

import butterknife.Optional;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Samuel on 29/11/2017.
 */

public interface EventfulApi {

    String BASE_URL = "http://api.eventful.com/rest/events/";

    @GET("search?app_key=tTGMSVjn8tV3mPF7&date=Future&page_size=50&within=10")
    Call <EventfulResponse> event(@Query("where") String location);

    @GET("search?app_key=tTGMSVjn8tV3mPF7&date=Future&page_size=50&within=10")
    Call<EventfulResponse> eventSearched(@Query("where") String location,
                                         @Query("keywords") String kewords);
}
