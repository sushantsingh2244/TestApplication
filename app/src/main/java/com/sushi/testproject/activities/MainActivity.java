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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
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
import java.util.Collections;
import java.util.Comparator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {
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
    private TextView lblNote,lblTotalCase, lblRecovered, lblDeath;
    private TextView txtTotalCase, txtRecovered, txtDeath;
    private TextView lblAsc, lblDesc, lblDeathAsc, lblDeathDesc, lblRecoveredAsc, lblRecoveredDesc;
    private ShimmerFrameLayout shimmer_view;
    private RelativeLayout contentLayout;

    private ArrayList<CountryModel> countryInfo;
    private int TotalCase = 0;
    private int Death = 0;
    private int Recovered = 0;
    private SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentLayout = findViewById(R.id.contentLayout);
        shimmer_view = findViewById(R.id.shimmer_view);
        itemRecycler = findViewById(R.id.itemRecycler);
        lblNote = findViewById(R.id.lblNote);
        lblTotalCase = findViewById(R.id.lblTotalCase);
        lblRecovered = findViewById(R.id.lblRecovered);
        lblDeath = findViewById(R.id.lblDeath);
        txtTotalCase = findViewById(R.id.txtTotalCase);
        txtRecovered = findViewById(R.id.txtRecovered);
        txtDeath = findViewById(R.id.txtDeath);
        lblAsc = findViewById(R.id.lblAsc);
        lblDesc = findViewById(R.id.lblDesc);
        lblDeathAsc = findViewById(R.id.lblDeathAsc);
        lblDeathDesc = findViewById(R.id.lblDeathDesc);
        lblRecoveredAsc = findViewById(R.id.lblRecoveredAsc);
        lblRecoveredDesc = findViewById(R.id.lblRecoveredDesc);

        Typeface typeface1 = Typeface.createFromAsset(this.getAssets(), "fonts/os_medium.ttf");
        Typeface typeface2 = Typeface.createFromAsset(this.getAssets(), "fonts/os_bold.ttf");
        lblNote.setTypeface(typeface1);
        lblTotalCase.setTypeface(typeface1);
        lblRecovered.setTypeface(typeface1);
        lblDeath.setTypeface(typeface1);
        txtTotalCase.setTypeface(typeface2);
        txtRecovered.setTypeface(typeface2);
        txtDeath.setTypeface(typeface2);

        lblAsc.setTypeface(typeface1);
        lblDesc.setTypeface(typeface1);
        lblDeathAsc.setTypeface(typeface1);
        lblDeathDesc.setTypeface(typeface1);
        lblRecoveredAsc.setTypeface(typeface1);
        lblRecoveredDesc.setTypeface(typeface1);

        lblAsc.setOnClickListener(this);
        lblDesc.setOnClickListener(this);
        lblDeathAsc.setOnClickListener(this);
        lblDeathDesc.setOnClickListener(this);
        lblRecoveredAsc.setOnClickListener(this);
        lblRecoveredDesc.setOnClickListener(this);

        callLocationInfo();

        pref = this.getSharedPreferences("CountryName", 0);
        editor = pref.edit();

        countryInfo = new ArrayList<>();
        dataAdapter = new DataAdapter(countryInfo, this);
        Utility.setRecycler(this, itemRecycler, dataAdapter);

        if (Utility.isOnline(MainActivity.this)) {
            contentLayout.setVisibility(View.GONE);
            shimmer_view.setVisibility(View.VISIBLE);
            shimmer_view.startShimmerAnimation();
            /*Create handle for the RetrofitInstance interface*/
            ApiInterface service = MyApplication.getInstance().getRetrofitInstance().create(ApiInterface.class);

            Call<JsonObject> call = service.getAllData();
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    shimmer_view.stopShimmerAnimation();
                    final String respons = response.body().toString();
                    JSONObject jsonObject = null;
                    contentLayout.setVisibility(View.VISIBLE);
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
                            lblNote.setText("Note: Total cases are sorted in desc order");
                            Collections.sort(countryInfo, new Comparator<CountryModel>() {
                                public int compare(CountryModel obj1, CountryModel obj2) {
                                    return obj2.getTotalConfirmed().compareTo(obj1.getTotalConfirmed());
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    shimmer_view.stopShimmerAnimation();
                    shimmer_view.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    shimmer_view.stopShimmerAnimation();
                    shimmer_view.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lblAsc:
                Collections.sort(countryInfo, new Comparator<CountryModel>() {
                    public int compare(CountryModel obj1, CountryModel obj2) {
                        contentLayout.setVisibility(View.GONE);
                        // ===================Ascending order===========
                        shimmer_view.setVisibility(View.VISIBLE);
                        shimmer_view.startShimmerAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shimmer_view.stopShimmerAnimation();
                                shimmer_view.setVisibility(View.GONE);
                                contentLayout.setVisibility(View.VISIBLE);
                                lblAsc.setVisibility(View.GONE);
                                lblDesc.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                        return obj1.getTotalConfirmed().compareTo(obj2.getTotalConfirmed());
                    }
                });
                lblNote.setText("Note: Total cases are sorted in Asce order");
                dataAdapter.notifyDataSetChanged();
                break;
            case R.id.lblDesc:
                Collections.sort(countryInfo, new Comparator<CountryModel>() {
                    public int compare(CountryModel obj1, CountryModel obj2) {
                        contentLayout.setVisibility(View.GONE);
                        // ===================Descending order===========
                        shimmer_view.setVisibility(View.VISIBLE);
                        shimmer_view.startShimmerAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shimmer_view.stopShimmerAnimation();
                                shimmer_view.setVisibility(View.GONE);
                                contentLayout.setVisibility(View.VISIBLE);
                                lblDesc.setVisibility(View.GONE);
                                lblAsc.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                        return obj2.getTotalConfirmed().compareTo(obj1.getTotalConfirmed());
                    }
                });
                lblNote.setText("Note: Total cases are sorted in desc order");
                dataAdapter.notifyDataSetChanged();
                break;
            case R.id.lblDeathAsc:
                Collections.sort(countryInfo, new Comparator<CountryModel>() {
                    public int compare(CountryModel obj1, CountryModel obj2) {
                        contentLayout.setVisibility(View.GONE);
                        // ===================Ascending order===========
                        shimmer_view.setVisibility(View.VISIBLE);
                        shimmer_view.startShimmerAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shimmer_view.stopShimmerAnimation();
                                shimmer_view.setVisibility(View.GONE);
                                contentLayout.setVisibility(View.VISIBLE);
                                lblDeathAsc.setVisibility(View.GONE);
                                lblDeathDesc.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                        return obj1.getTotalDeaths().compareTo(obj2.getTotalDeaths());
                    }
                });
                lblNote.setText("Note: Total death are sorted in Ascending order");
                dataAdapter.notifyDataSetChanged();
                break;
            case R.id.lblDeathDesc:
                Collections.sort(countryInfo, new Comparator<CountryModel>() {
                    public int compare(CountryModel obj1, CountryModel obj2) {
                        contentLayout.setVisibility(View.GONE);
                        // ===================Descending order===========
                        shimmer_view.setVisibility(View.VISIBLE);
                        shimmer_view.startShimmerAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shimmer_view.stopShimmerAnimation();
                                shimmer_view.setVisibility(View.GONE);
                                contentLayout.setVisibility(View.VISIBLE);
                                lblDeathDesc.setVisibility(View.GONE);
                                lblDeathAsc.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                        return obj2.getTotalDeaths().compareTo(obj1.getTotalDeaths());
                    }
                });
                lblNote.setText("Note: Total death are sorted in desc order");
                dataAdapter.notifyDataSetChanged();
                break;
            case R.id.lblRecoveredAsc:
                Collections.sort(countryInfo, new Comparator<CountryModel>() {
                    public int compare(CountryModel obj1, CountryModel obj2) {
                        contentLayout.setVisibility(View.GONE);
                        // ===================Ascending order===========
                        shimmer_view.setVisibility(View.VISIBLE);
                        shimmer_view.startShimmerAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shimmer_view.stopShimmerAnimation();
                                shimmer_view.setVisibility(View.GONE);
                                contentLayout.setVisibility(View.VISIBLE);
                                lblRecoveredAsc.setVisibility(View.GONE);
                                lblRecoveredDesc.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                        return obj1.getTotalRecovered().compareTo(obj2.getTotalRecovered());
                    }
                });
                lblNote.setText("Note: Total Recovered are sorted in Ascending order");
                dataAdapter.notifyDataSetChanged();
                break;
            case R.id.lblRecoveredDesc:
                Collections.sort(countryInfo, new Comparator<CountryModel>() {
                    public int compare(CountryModel obj1, CountryModel obj2) {
                        contentLayout.setVisibility(View.GONE);
                        // ===================Descending order===========
                        shimmer_view.setVisibility(View.VISIBLE);
                        shimmer_view.startShimmerAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                shimmer_view.stopShimmerAnimation();
                                shimmer_view.setVisibility(View.GONE);
                                contentLayout.setVisibility(View.VISIBLE);
                                lblRecoveredDesc.setVisibility(View.GONE);
                                lblRecoveredAsc.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                        return obj2.getTotalRecovered().compareTo(obj1.getTotalRecovered());
                    }
                });
                lblNote.setText("Note: Total Recovered are sorted in desc order");
                dataAdapter.notifyDataSetChanged();
                break;
        }
    }
    //======================Location=======================

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
