package com.sushi.testproject.common;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.sushi.testproject.interfaces.AddressUpdateListener;

import java.lang.ref.WeakReference;

public class AddressHelper extends AsyncTask<String, Void, String>{
    private Location location;
    private AddressUpdateListener addressUpdateListener;
    private WeakReference<Context> context;

public AddressHelper(Context context, Location location, AddressUpdateListener addressUpdateListener){
    this.context = new WeakReference<>(context);
    this.location = location;
    this.addressUpdateListener = addressUpdateListener;
}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(addressUpdateListener!=null){
            addressUpdateListener.updateOnSuccess(s);
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        return Utility.getCompleteAddressString(context.get(), location.getLatitude(), location.getLongitude());
    }
}
