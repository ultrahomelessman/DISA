package com.example.bismillah_motor_listrik;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bismillah_motor_listrik.API.InterfaceAPI;
import com.example.bismillah_motor_listrik.model.Motor;
import com.example.bismillah_motor_listrik.model.PostMotor;
import com.example.bismillah_motor_listrik.model.Respon;
import com.example.bismillah_motor_listrik.model.Update;
import com.example.bismillah_motor_listrik.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bismillah_motor_listrik.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {

    TextView tagihan, jarak;
    String id, crd;
    Integer nilai, nl, tampil;
    Handler handler1, handler2;
    Runnable runnable1, runnable2, runnable3;
    private String KEY_NAME = "NAMA";
    Dialog myDialog;
    private Button buttonpopup;
    private View decorView;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    private ActivityMapsBinding binding;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Button btn, btn_off, btn_resume;
    private Handler handler; // handler that gets info from Bluetooth service

    Marker userLocationMarker;
    Circle userLocationAccuracyCircle;

    //TODO Bluetooth service

    private UUID mDeviceUUID;

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter btAdapter;
    private BluetoothSocket mBTSocket;
    private BluetoothDevice mDevice;


    //Declare timer
    CountDownTimer cTimer = null;
    private int level;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myDialog = new Dialog(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);

        TextView battery = (TextView) findViewById(R.id.baterry);

//        Credit
        tagihan = findViewById(R.id.tagihan);
        jarak = findViewById(R.id.jarak);

//        ambil id
        Bundle extras = getIntent().getExtras();
        id = extras.getString(KEY_NAME);

//        Function billing
        credit();
//        loopCredit();

        nl = 10;
        tampil = 1;
        if (tampil != 0) {
            jarak();
            setCredit();
        }

        java.util.Date noteTS = Calendar.getInstance().getTime();

        String time = "hh:mm%"; // 12:00
        battery.setText(DateFormat.format(time, noteTS));


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        btn = findViewById(R.id.btn_stnby);
        btn_resume = findViewById(R.id.btn_resume);
        btn_off = findViewById(R.id.btn_off);

        decorView = getWindow() .getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideSystemBars());
            }
        });

        buttonpopup = (Button) findViewById(R.id.buttonpopup);
        buttonpopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openstandbypopup();
            }
        });


        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

//        CheckBluetoothState();
        BluetoothStart();

        loopRealtime();

//        BatteryTrigger();


        //TODO HP Battery Service

        this.registerReceiver(this.mBatInfoReceive, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


//        btn_off.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                Handler handler = new Handler();
////                handler.removeCallbacks(runnable1);
////                handler.removeCallbacks(runnable2);
////                handler.removeCallbacks(runnable3);
//                Off();
//                habis();
//                return;
//            }
//        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Standby();

            }
        });

        btn_resume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Resume();
            }
        });
    }


    private void Standby() {
        btn.setVisibility(View.INVISIBLE);
        btn_resume.setVisibility(View.VISIBLE);
//        btn_off.setVisibility(View.VISIBLE);
        handler1.removeCallbacks(runnable1);
        handler2.removeCallbacks(runnable2);

        onStop();
    }


    private void Resume() {
        btn.setVisibility(View.VISIBLE);
        btn_resume.setVisibility(View.INVISIBLE);
        if (tampil != 0) {
            jarak();
            setCredit();
        }
        onStart();
    }

    private void Off() {
        onStop();
        stop();
        return;

    }

    private void stop() {
        Gson gson = new GsonBuilder().setLenient().create();

        OkHttpClient client = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(InterfaceAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        String tgh = tagihan.getText().toString();

        InterfaceAPI api = retrofit.create(InterfaceAPI.class);

        Update put = new Update(tgh);

        Call<Respon> call = api.putCredit(id, put);

        call.enqueue(new Callback<Respon>() {
            @Override
            public void onResponse(Call<Respon> call, Response<Respon> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                Toast.makeText(MapsActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<Respon> call, Throwable t) {
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void habis() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(i);
            }
        }, 2000);
    }

    private void setCredit() {
        handler1 = new Handler();
        runnable1 = new Runnable() {
            public void run() {
                nilai = (nl / 10) * 1000;
                int trans = Integer.parseInt(crd);
                tampil = trans - nilai;
                if (tampil <= 10000) {
                    Toast.makeText(MapsActivity.this, "Billing Tersisa 10.0000", Toast.LENGTH_SHORT).show();
                }

                tagihan.setText(tampil.toString());
                if (tampil == 0) {
                    Toast.makeText(MapsActivity.this, "Billing Telah Habis", Toast.LENGTH_SHORT).show();
                    habis();
                    return;
                }
                setCredit();
            }
        };
        handler1.postDelayed(runnable1, 10000);

//        stop_Standby.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                handler.removeCallbacks(runnable1);
//            }
//        });
    }

    private void jarak() {
        handler2 = new Handler();
        runnable2 = new Runnable() {
            public void run() {
                nl++;
                jarak.setText(nl.toString());
                jarak();
            }
        };
        handler2.postDelayed(runnable2, 1000);
//        stop_Standby.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                handler.removeCallbacks(runnable2);
//            }
//        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void  run() {
//                nl++;
//                jarak.setText(nl.toString());
//                jarak();
//            }
//        }, 1000);
    }

    private void credit() {
        Gson gson = new GsonBuilder().setLenient().create();

        OkHttpClient client = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(InterfaceAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        InterfaceAPI api = retrofit.create(InterfaceAPI.class);

        Call<User> call = api.user(id);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                tagihan.setText(response.body().getData().getCredit().toString());
                crd = tagihan.getText().toString();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void openstandbypopup(){
        Intent intent = new Intent(this, standbypopupp.class);
        startActivity(intent);
    }

    public void ShowPopup (View v){
        myDialog.setContentView(R.layout.activity_standbypopupp);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }

    private int hideSystemBars(){
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    //TODO BLUETOOTH
    @SuppressLint("MissingPermission")
    private void BluetoothStart() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Perangkat Tidak Mendukung Bluetooth", Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }

    }

    //TODO Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                @SuppressLint("MissingPermission") String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_ENABLE_BT) {
//            CheckBluetoothState();
//        }
//    }

//    @SuppressLint("MissingPermission")
//    private void CheckBluetoothState() {
//        // Checks for the Bluetooth support and then makes sure it is turned on
//        // If it isn't turned on, request to turn it on
//        // List paired devices
//        if(btAdapter==null) {
//
//            return;
//        } else {
//            if (btAdapter.isEnabled()) {
////                textview1.append("\nBluetooth is enabled...");
//
//                // Listing paired devices
////                textview1.append("\nPaired Devices are:");
//                @SuppressLint("MissingPermission") Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
//                for (BluetoothDevice device : devices) {
////                    textview1.append("\n  Device: " + device.getName() + ", " + device);
//                }
//            } else {
//                //Prompt user to turn on Bluetooth
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            }
//        }
//    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        mMap.setOnMapLongClickListener( this);
//        mMap.setOnMarkerDragListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager
                .PERMISSION_GRANTED) {
            enableUserLocation();
//                    zoomToUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Dialog Why Permission Important
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_LOCATION_REQUEST_CODE);
            }
        }
        // Add a marker in Sydney and move the camera
//        LatLng latLng = new LatLng(27.231,13.3123);
//        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("IDK").snippet("wonder");
//        mMap.addMarker(markerOptions);
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
//        mMap.animateCamera(cameraUpdate);

        try {
            List<Address> addresses = geocoder.getFromLocationName("abc.xyz", 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                LatLng london = new LatLng(address.getLatitude(), address.getLongitude());

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(address.getLatitude(), address.getLongitude()))
                        .title(address.getLocality());
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(london, 16));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
            if (mMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };

    public void stringLocation(Location location) {


    }

    private void loopRealtime() {
        Handler handler = new Handler();
        Runnable runnable3 = new Runnable() {
            public void run() {
                realtime();
                loopRealtime();
//                BatteryTrigger();
            }
        };
        handler.postDelayed(runnable3, 10000);

        btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable3);
                Off();
                habis();
                return;
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                realtime();
//                loopRealtime();
//            }
//        }, 10000);
    }

    public void realtime() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        double longitude = location.getLongitude();
//        double latitude = location.getLatitude();
//        String string_latlng = center.toString();

        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());

        Gson gson = new GsonBuilder().setLenient().create();

        OkHttpClient client = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(InterfaceAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        String battery = "100";
        String jarak = "1000";
//        String latitude = "7.8652";
//        String longitude = "-73.9987";
//        String latitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);
//        String longitude = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS);

        InterfaceAPI api = retrofit.create(InterfaceAPI.class);

        Motor realtime = new Motor(battery, jarak, latitude, longitude);

        Call<PostMotor> call = api.putMotor("2", realtime);

        call.enqueue(new Callback<PostMotor>() {
            @Override
            public void onResponse(Call<PostMotor> call, Response<PostMotor> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                Toast.makeText(MapsActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<PostMotor> call, Throwable t) {
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserLocationMarker(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


        if (userLocationMarker == null) {
            //Create New Marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dasd));
            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float) 0.5, (float) 0.5);
            userLocationMarker = mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        } else {
            //Use Previously created Marker
            userLocationMarker.setPosition(latLng);
            userLocationMarker.setRotation(location.getBearing());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }

        if (userLocationAccuracyCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
            circleOptions.fillColor(Color.argb(255, 255, 0, 0));
            circleOptions.radius(location.getAccuracy());
            userLocationAccuracyCircle = mMap.addCircle(circleOptions);
        } else {
            userLocationAccuracyCircle.setCenter(latLng);
            userLocationAccuracyCircle.setRadius(location.getAccuracy());
        }

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager
                .PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            //NEED REQUEST PERMISSION
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocation() {
        mMap.setMyLocationEnabled(true);
    }

    private void zoomToUserLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
//                mMap.addMarker(new MarkerOptions().position(latLng));
            }
        });
    }

//    @Override
//    public void onMapLongClick(@NonNull LatLng latLng) {
//        Log.d(TAG, "onMapLongClick: " + latLng.toString());
//        try {
//            List<Address> addresses =  geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//            if (addresses.size() > 0) {
//                Address address = addresses.get(0);
//                String streetAddress = address.getAddressLine(0);
//                mMap.addMarker(new MarkerOptions()
//                        .position(latLng)
//                        .title(streetAddress)
//                        .draggable(true)
//                );
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

//    @Override
//    public void onMarkerDrag(@NonNull Marker marker) {
//        Log.d(TAG, "onMarkerDrag: ");
//    }

//    @Override
//    public void onMarkerDragEnd(@NonNull Marker marker) {
//        Log.d(TAG, "onMarkerDragEnd: ");
//        LatLng latLng = marker.getPosition();
//        try {
//            List<Address> addresses =  geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//            if (addresses.size() > 0) {
//                Address address = addresses.get(0);
//                String streetAddress = address.getAddressLine(0);
//                marker.setTitle(streetAddress);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    public void onMarkerDragStart(@NonNull Marker marker) {
//        Log.d(TAG, "onMarkerDragStart: ");
//    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                zoomToUserLocation();
            } else {
                //Showing Dialog That permission is not granted...
            }
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {


    }

    /**
     * Called when the user touches the button
     */
//    public void standBy(View view) {
//        stopLocationUpdates();
//        startTimer();
//        btn.setText("Resume");
//
//    }

    //start timer function
    void startTimer() {
        cTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
            }
        };
        cTimer.start();
    }

    //cancel timer
    void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
    }


    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private BroadcastReceiver mBatInfoReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            TextView battery = (TextView) findViewById(R.id.baterry);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

            String str = new String(String.valueOf(level));

            int batu = Integer.parseInt(str);
            battery.setText(str);

            if (batu <= 50) {
                chargePhone();
            }

            if (batu == 100) {

                chargeDone();
            }


        }
    };

    public void chargePhone(){

        Toast.makeText(this, "BANGSAT", Toast.LENGTH_SHORT).show();
//        try {
//            //TODO Battery HP Charge
//            String sendtxt = "LN";
//            mBTSocket.getOutputStream().write(sendtxt.getBytes());
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    public void chargeDone () {

        Toast.makeText(this, "ANJING", Toast.LENGTH_SHORT).show();
//        try {
//            //TODO Battery HP Charge
//            String sendtxt = "LN";
//            mBTSocket.getOutputStream().write(sendtxt.getBytes());
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();T
//        }
    }


}