package com.example.brunovsiq.mapchat.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.brunovsiq.mapchat.R;
import com.example.brunovsiq.mapchat.models.Partner;
import com.example.brunovsiq.mapchat.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class MultiPaneActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    private ArrayList<Partner> partnerList =  new ArrayList<>();
    private ArrayList<String> usernameList =  new ArrayList<>();

    private final int PERMISSION_CHECK = 1;

    private FusedLocationProviderClient mFusedLocationClient;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multipane);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CHECK);
        }
        locationManager = getSystemService(LocationManager.class);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            User.getInstance().setLatitude(location.getLatitude());
                            User.getInstance().setLongitude(location.getLongitude());
                        }
                    }
                });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                User.getInstance().setLatitude(location.getLatitude());
                User.getInstance().setLongitude(location.getLongitude());
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
        };

        getPartnersList();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "You must enable the functionallity to fully utilize the App", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CHECK);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(locationListener);
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getPartnersList() {
        AndroidNetworking.get("https://kamorris.com/lab/get_locations.php")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONArray jsonArray = response;
                        partnerList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = jsonArray.getJSONObject(i);

                                Partner partner = new Partner(jsonObject);
                                partnerList.add(partner);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Collections.sort(partnerList);

                        for (int i=0; i < partnerList.size(); i++) {
                            usernameList.add(partnerList.get(i).getUsername());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MultiPaneActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, usernameList);

                        // Assign adapter to ListView
                        UserListFragment.usernameListView.setAdapter(adapter);

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("Response", "Responseerror");
                        //TURN ON INTERNET

                    }
                });
    }
}
