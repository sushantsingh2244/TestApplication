package com.sushi.testproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.sushi.testproject.MyApplication;
import com.sushi.testproject.R;
import com.sushi.testproject.adapter.DataAdapter;
import com.sushi.testproject.common.AddressHelper;
import com.sushi.testproject.common.Utility;
import com.sushi.testproject.interfaces.AddressUpdateListener;
import com.sushi.testproject.interfaces.ApiInterface;
import com.sushi.testproject.models.CountryModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;
    private static final int REQUEST_GET_LOCATION = 0;
    int REQUEST_CHECK_SETTINGS = 1000;
    GoogleMap googleMap;
    private String Latitude = "", Longitude = "";

    public static final int JOB_ID = 100;
    private DataAdapter dataAdapter;
    private RecyclerView itemRecycler;
    private TextView lblTotalCase, lblRecovered, lblDeath;
    private TextView txtTotalCase, txtRecovered, txtDeath;
    private ArrayList<CountryModel> countryInfo;
    private ProgressDialog progressDoalog;
    private int TotalCase = 0;
    private int Death = 0;
    private int Recovered = 0;
    private SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemRecycler = findViewById(R.id.itemRecycler);
        lblTotalCase = findViewById(R.id.lblTotalCase);
        lblRecovered = findViewById(R.id.lblRecovered);
        lblDeath = findViewById(R.id.lblDeath);
        txtTotalCase = findViewById(R.id.txtTotalCase);
        txtRecovered = findViewById(R.id.txtRecovered);
        txtDeath = findViewById(R.id.txtDeath);

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/os_regular.ttf");
        Typeface typeface1 = Typeface.createFromAsset(this.getAssets(), "fonts/os_medium.ttf");
        Typeface typeface2 = Typeface.createFromAsset(this.getAssets(), "fonts/os_bold.ttf");
        lblTotalCase.setTypeface(typeface);
        lblRecovered.setTypeface(typeface);
        lblDeath.setTypeface(typeface);
        txtTotalCase.setTypeface(typeface1);
        txtRecovered.setTypeface(typeface1);
        txtDeath.setTypeface(typeface1);

        callLocationInfo();

        pref = this.getSharedPreferences("CountryName", 0);
        editor = pref.edit();

        countryInfo = new ArrayList<>();
        dataAdapter = new DataAdapter(countryInfo, this);
        Utility.setRecycler(this, itemRecycler, dataAdapter);

        if (Utility.isOnline(MainActivity.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                JobScheduler jobScheduler = (JobScheduler) MainActivity.this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                // jobScheduler.cancelAll();
                // scheduleJob(MainActivity.this, jobScheduler);
            }
        }

        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMessage("Loading....");
        progressDoalog.show();

        /*Create handle for the RetrofitInstance interface*/
        ApiInterface service = MyApplication.getInstance().getRetrofitInstance().create(ApiInterface.class);

        Call<JsonObject> call = service.getAllData();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressDoalog.dismiss();
                final String respons = response.body().toString();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(respons);
                    JSONArray JArray = jsonObject.getJSONArray("Countries");
                    if (JArray.length() > 0) {
                        for (int i = 0; i < JArray.length(); i++) {
                            JSONObject json = JArray.getJSONObject(i);
                            CountryModel countryModel = new CountryModel();
                            countryModel.setCountry(json.getString("Country"));
                            countryModel.setCountryCode(json.getString("CountryCode"));
                            countryModel.setSlug(json.getString("Slug"));
                            countryModel.setNewConfirmed(Integer.valueOf(json.getString("NewConfirmed")));
                            countryModel.setTotalConfirmed(Integer.valueOf(json.getString("TotalConfirmed")));
                            TotalCase = TotalCase + Integer.parseInt(json.getString("TotalConfirmed"));
                            countryModel.setNewDeaths(Integer.valueOf(json.getString("NewDeaths")));
                            countryModel.setTotalDeaths(Integer.valueOf(json.getString("TotalDeaths")));
                            Death = Death + Integer.parseInt(json.getString("TotalDeaths"));
                            countryModel.setNewRecovered(Integer.valueOf(json.getString("NewRecovered")));
                            countryModel.setTotalRecovered(Integer.valueOf(json.getString("TotalRecovered")));
                            Recovered = Recovered + Integer.parseInt(json.getString("TotalRecovered"));
                            countryModel.setDate(json.getString("Date"));
                            countryInfo.add(countryModel);
                            dataAdapter.notifyDataSetChanged();
                        }
                        txtTotalCase.setText("" + TotalCase);
                        txtRecovered.setText("" + Recovered);
                        txtDeath.setText("" + Death);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressDoalog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //======================Location============
    private void checkForLocationRequestSetting(LocationRequest locationRequest) {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .addLocationRequest(locationRequest);
            Task<LocationSettingsResponse> result =
                    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                break;
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_GET_LOCATION) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        populateAutoComplete();
                    }
                }
                callLocationInfo();
            }
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION}, REQUEST_GET_LOCATION);
        }
        return false;
    }

    private void callLocationInfo() {
        try {
            populateAutoComplete();
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{ACCESS_FINE_LOCATION,
                            ACCESS_COARSE_LOCATION}, REQUEST_GET_LOCATION);
                }
            } else {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                locationRequest = LocationRequest.create();
                locationRequest.setInterval(1800000);
                locationRequest.setFastestInterval(900000);
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        for (Location location : locationResult.getLocations()) {
                            lastLocation = location;
                            setLocationInView(location);
                        }
                    }

                };
                checkForLocationRequestSetting(locationRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        if (mFusedLocationClient != null)
            stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mFusedLocationClient == null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION,
                                ACCESS_COARSE_LOCATION, READ_EXTERNAL_STORAGE, CAMERA}, REQUEST_GET_LOCATION);
                    }
                } else {
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    //getLastLocation();
                    locationRequest = LocationRequest.create();
                    locationRequest.setInterval(5000);
                    locationRequest.setFastestInterval(1000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    mLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            for (Location location : locationResult.getLocations()) {
                                Latitude = String.valueOf(location.getLatitude() + "");
                                Longitude = String.valueOf(location.getLongitude() + "");
                            }
                        }

                    };
                    checkForLocationRequestSetting(locationRequest);
                }
            } catch (SecurityException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            startLocationUpdates();
        }
        super.onResume();
    }

    private void setLocationInView(Location lastLocation) {
        Latitude = String.valueOf(lastLocation.getLatitude());
        Longitude = String.valueOf(lastLocation.getLongitude());
        try {
            AddressUpdateListener addressUpdateListener = new AddressUpdateListener() {
                @Override
                public void updateOnSuccess(String address) {
                    // txtAddress.setText(address);
                    editor.putString("Country", address);
                    editor.apply();
                    editor.commit();
                }
            };
            AddressHelper addressHelper = new AddressHelper(this, lastLocation, addressUpdateListener);
            addressHelper.execute();
        } catch (Exception se) {
            Log.d("TAG", "" + se.getMessage());
        }
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void startLocationUpdates() {
        if (mFusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
