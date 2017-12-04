package com.silho.ideo.meetus.api;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by Samuel on 29/11/2017.
 */

public class Service {

    public static EventfulApi getEvents(){

        return new Retrofit.Builder()
                .baseUrl(EventfulApi.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
                .create(EventfulApi.class);
    }
}
