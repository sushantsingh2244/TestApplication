package com.sushi.testproject;

import android.app.Application;
import android.os.StrictMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sushi.testproject.common.ResponseInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyApplication extends Application {
    public static final String TAG = MyApplication.class.getSimpleName();
    private Retrofit mRetrofit;

    private static MyApplication mInstance;
    public static final String BASE_URL = "https://api.covid19api.com/";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(200, TimeUnit.SECONDS)
            .writeTimeout(200, TimeUnit.SECONDS)
            .readTimeout(200, TimeUnit.SECONDS)
            .addInterceptor(new ResponseInterceptor())
            .build();

    public  Retrofit getRetrofitInstance() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (mRetrofit == null) {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
        }

        return mRetrofit;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

}
