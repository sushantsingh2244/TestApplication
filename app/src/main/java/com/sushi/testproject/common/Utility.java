package com.sushi.testproject.common;

import android.content.Context;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class Utility {


    public static void setRecycler(Context context, RecyclerView recycler, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recycler.setLayoutManager(linearLayoutManager);
        recycler.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL) {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            }
        });
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setHasFixedSize(true);
        recycler.setAdapter(adapter);
    }


    public static boolean isOnline(Context context) {
        boolean connection = false;
        ConnectivityManager cm = null;

        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        connection = netInfo != null && netInfo.isConnected();

        return connection;
    }


    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
//                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
//                   // strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
//
//                }
                strAdd = returnedAddress.getCountryName();
               // strAdd = strReturnedAddress.toString().trim();
                Log.e("Location1 : ", strReturnedAddress.toString());
            } else {
                Log.e("Location2 : ", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Location3 : ", "Canont get Address!");
        }
        return strAdd;
    }

}

