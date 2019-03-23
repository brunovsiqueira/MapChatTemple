package com.example.brunovsiq.mapchat.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.brunovsiq.mapchat.R;
import com.example.brunovsiq.mapchat.encryption.KeyService;
import com.example.brunovsiq.mapchat.models.Partner;
import com.example.brunovsiq.mapchat.models.User;
import com.example.brunovsiq.mapchat.utils.OnSwipeTouchListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import static android.nfc.NdefRecord.createMime;

public class MultiPaneActivity extends AppCompatActivity implements OnMapReadyCallback, NfcAdapter.CreateNdefMessageCallback {

//    LocationManager locationManager;
//    LocationListener locationListener;
    private GoogleMap mMap;
    private ArrayList<Partner> partnerList =  new ArrayList<>();
    private ArrayList<String> usernameList =  new ArrayList<>();
    private LocationRequest mLocationRequest;

    private final int PERMISSION_CHECK = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private boolean isMarkersAdded = false;
    private EditText editUsername;
    private Button saveButton;
    private boolean isRegistered = false;

    private KeyService keyService;
    private String username;
    private boolean isBound = false;
    private KeyPair userKeyPair;
    private NfcAdapter nfcAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multipane);

        configureByScreenSize(); //single pane for small screens
        doBindService();


        /* START NFC Initialization */

        // Check for available NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            //finish();
            //return;
        } else {
            // Register callback
            nfcAdapter.setNdefPushMessageCallback(this, this);
        }
        /*END NFC Initialization*/

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getString("username", "") != "") {
            User.getInstance().setUsername(sharedPref.getString("username", ""));
            username = User.getInstance().getUsername();
        }

        editUsername = findViewById(R.id.username_edit);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(saveClickListener);

        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CHECK);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            User.getInstance().setLatitude(location.getLatitude());
                            User.getInstance().setLongitude(location.getLongitude());
                            postUser();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(User.getInstance().getLatitude(), User.getInstance().getLongitude()), 10.0f));
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    User.getInstance().setLatitude(location.getLatitude());
                    User.getInstance().setLongitude(location.getLongitude());
                    postUser();
                }
            };
        };

        getPartnersList();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 30000); //call getPartnersList every 30 seconds

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @TargetApi(Build.VERSION_CODES.M)
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onServiceConnected(ComponentName className, IBinder service) {

            // This is called when the connection with the service has
            // been established, giving us the service object we can use
            // to interact with the service.  Because we have bound to a
            // explicit service that we know is running in our own
            // process, we can cast its IBinder to a concrete class and
            // directly access it.
            keyService = ((KeyService.KeyBinder)service).getService();
            userKeyPair = keyService.getMyKeyPair();

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has
            // been unexpectedly disconnected -- that is, its process
            // crashed. Because it is running in our same process, we
            // should never see this happen.
            keyService = null;

        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation
        // that we know will be running in our own process (and thus
        // won't be supporting component replacement by other
        // applications).
        bindService(new Intent(this, KeyService.class),
                mConnection,
                Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MultiPaneActivity.this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", editUsername.getText().toString());
            User.getInstance().setUsername(editUsername.getText().toString());
            username = editUsername.getText().toString();
            editor.commit();

            postUser();
        }
    };

    private void postUser() {
        if (User.getInstance().getUsername() != null) {
            AndroidNetworking.post("https://kamorris.com/lab/register_location.php")
                    .addBodyParameter("user", User.getInstance().getUsername())
                    .addBodyParameter("latitude", String.valueOf(User.getInstance().getLatitude()))
                    .addBodyParameter("longitude", String.valueOf(User.getInstance().getLongitude()))
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // do anything with response
                            Log.d("RESPONSE", "");
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            Log.d("ERROR", "");
                            if (error.getMessage().contains("OK")) {
                                if (!isRegistered) {
                                    isRegistered = true;
                                    Toast.makeText(MultiPaneActivity.this, "Username successfully registered/updated!", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                if (!isRegistered) {
                                    Toast.makeText(MultiPaneActivity.this, "Error registering username", Toast.LENGTH_LONG).show();
                                }
                            }

                            //OK is saved
                        }
                    });
        }
    }

    private TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            getPartnersList();
        }
    };

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

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CHECK);
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    void doUnbindService() {
        if (isBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            isBound = false;
        }
    }

    void processIntent(Intent intent) {
        String userdata = new String(((NdefMessage)intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0])
                .getRecords()[0]
                .getPayload());
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(userdata);
            String partnerKey = jsonObject.getString("key");
            partnerKey = partnerKey.replace("-----BEGIN PUBLIC KEY-----", "");
            partnerKey = partnerKey.replace("\n-----END PUBLIC KEY-----", "");
            keyService.storePublicKey(jsonObject.getString("user"), partnerKey, MultiPaneActivity.this);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(10);
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
                                if(!partner.getUsername().equalsIgnoreCase(User.getInstance().getUsername())) {
                                    //see if the partner is the user
                                    partnerList.add(partner);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!isMarkersAdded) {
                            for (Partner partner : partnerList) {
                                mMap.addMarker(new MarkerOptions().position(new LatLng(partner.getLatitude(), partner.getLongitude()))
                                        .title(partner.getUsername()));
                            }
                        }

                        Collections.sort(partnerList);
                        usernameList.clear();
                        for (int i=0; i < partnerList.size(); i++) {
                            usernameList.add(partnerList.get(i).getUsername());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MultiPaneActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, usernameList);

                        // Assign adapter to ListView
                        UserListFragment.usernameListView.setAdapter(adapter);
                        UserListFragment.usernameListView.setOnItemClickListener(onItemClickListener);

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("Response", "Responseerror");
                        //TURN ON INTERNET

                    }
                });
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.O)
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String partnerName = usernameList.get(position);
            String publicKey = null;

            publicKey = keyService.getPublicKey(partnerName, MultiPaneActivity.this);


            if (publicKey != null) {
                if (User.getInstance().getUsername() != null) {
                    //go to chat
                } else {
                    Toast.makeText(MultiPaneActivity.this, "First you need to register an username!", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(MultiPaneActivity.this, "First you need to exchange keys via NFC!", Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


    }

    private void configureByScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;

        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        if ( (widthDp < 300) || (heightDp < 480) ) {
            hideMapPane();
            View userFragment = findViewById(R.id.user_list_fragment);
            userFragment.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeLeft(){
                    showMapPane();
                    hideUserPane();
                    super.onSwipeLeft();
                }

                @Override
                public void onSwipeRight(){
                    //showAlphaPane();
                    showUserPane();
                    hideMapPane();
                    super.onSwipeRight();
                }

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //hideAlphaPane();
                    return super.onTouch(v, event);
                }
            });
        }

    }

    /**
     * Method to hide the User pane
     */
    private void hideUserPane() {
        View alphaPane = findViewById(R.id.user_list_fragment);
        if (alphaPane.getVisibility() == View.VISIBLE) {
            alphaPane.setVisibility(View.GONE);
        }
    }

    /**
     * Method to show the User pane
     */
    private void showUserPane() {
        View alphaPane = findViewById(R.id.user_list_fragment);
        if (alphaPane.getVisibility() == View.GONE) {
            alphaPane.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to hide the Map pane
     */
    private void hideMapPane() {
        View alphaPane = findViewById(R.id.map_fragment);
        if (alphaPane.getVisibility() == View.VISIBLE) {
            alphaPane.setVisibility(View.GONE);
        }
    }

    /**
     * Method to show the Map pane
     */
    private void showMapPane() {
        View alphaPane = findViewById(R.id.map_fragment);
        if (alphaPane.getVisibility() == View.GONE) {
            alphaPane.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        X509EncodedKeySpec spec = null;
        String keyString = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            spec = keyFactory.getKeySpec(userKeyPair.getPublic(), X509EncodedKeySpec.class);
            keyString = Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        keyString = String.format("-----BEGIN PUBLIC KEY-----%s-----END PUBLIC KEY-----", keyString);


        JSONObject keyJsonObject = new JSONObject();
        try {
            keyJsonObject.put("user", username);
            keyJsonObject.put("key", keyString);
        } catch (JSONException e) {

            e.printStackTrace();
        }

        String PEMRecord = keyJsonObject.toString();
//        NdefMessage n = new NdefMessage(new NdefRecord[] { NdefRecord.createTextRecord(null,PEMRecord), NdefRecord.createApplicationRecord(getPackageName())});
        NdefMessage n = new NdefMessage(
                new NdefRecord[] { createMime("application/vnd.com.example.android.beam", PEMRecord.getBytes())

                });

        return n;
    }
}
