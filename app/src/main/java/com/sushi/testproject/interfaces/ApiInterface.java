package com.sushi.testproject.interfaces;

import com.google.gson.JsonObject;
import com.sushi.testproject.models.DataModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {

    @GET("/summary")
    Call<JsonObject> getAllData();

    @GET("https://api.covid19api.com/summary")
    Call<JsonObject> getDummyJson();
}
