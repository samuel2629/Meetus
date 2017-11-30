package com.silho.ideo.meetus.api;

import com.silho.ideo.meetus.model.EventfulResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Samuel on 29/11/2017.
 */

public interface EventfulApi {

    String BASE_URL = "http://api.eventful.com/rest/events/";
    @GET("search?app_key=tTGMSVjn8tV3mPF7&date=Future&page_size=250&within=50")
    Call <EventfulResponse> recipes(@Query("where") String location);
}
