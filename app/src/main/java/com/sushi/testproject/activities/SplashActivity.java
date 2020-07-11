package com.sushi.testproject.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sushi.testproject.R;
import com.sushi.testproject.common.AddressHelper;
import com.sushi.testproject.interfaces.AddressUpdateListener;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SplashActivity extends AppCompatActivity implements LocationListener {
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;
    private static final int REQUEST_GET_LOCATION = 0;
    int REQUEST_CHECK_SETTINGS = 1000;
    GoogleMap googleMap;
    private String Latitude = "", Longitude = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        requestPermission();
        callLocationInfo();
    }

    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
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
                                    resolvable.startResolutionForResult(SplashActivity.this, REQUEST_CHECK_SETTINGS);
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
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        for (Location location : locationResult.getLocations()) {
                            lastLocation = location;
                            setLocationInView(location);
                            if (googleMap != null) {
                                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                                CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                                googleMap.clear();

                                MarkerOptions mp = new MarkerOptions();
                                mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
                                mp.visible(true);
                                googleMap.addMarker(mp);
                                googleMap.moveCamera(center);
                                googleMap.animateCamera(zoom);

                            }
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
