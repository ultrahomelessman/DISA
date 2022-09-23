package com.example.bismillah_motor_listrik;

import static android.content.ContentValues.TAG;

import static java.lang.System.out;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.bismillah_motor_listrik.API.InterfaceAPI;
import com.example.bismillah_motor_listrik.model.Login;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.Result;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Scanner extends AppCompatActivity {

    public static final String DEVICE_EXTRA = "com.example.bismillah_motor_listrik.SOCKET";
    public static final String DEVICE_UUID = "com.example.bismillah_motor_listrik.uuid";
    private static final String DEVICE_LIST = "com.example.bismillah_motor_listrik.devicelist";
    private static final String DEVICE_LIST_SELECTED = "com.example.bismillah_motor_listrik.devicelistselected";
    public static final String BUFFER_SIZE = "com.example.bismillah_motor_listrik.buffersize";
    private static final String TAG = "BlueTest5-MainActivity";
    private BluetoothAdapter mBTAdapter;
    private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
    private static final int SETTINGS = 20;
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int mBufferSize = 50000; //Default
//    private ReadInput mReadThread = null;

    private int mMaxChars = 50000;//Default//change this to string..........
    private BluetoothSocket mBTSocket;

    private View decorView;

    private ProgressDialog progressDialog;

    private ReadInput mReadThread = null;


    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;
    CodeScanner codeScanner;
    CodeScannerView codeScannerView;
    private String KEY_NAME = "NAMA";
    ProgressDialog loading;
    String username;
    String id;
    String key_device, key_mBuffer, key_mDevice;
    private Handler handler; // handler that gets info from Bluetooth service


    String[] permissions = {
            Manifest.permission.CAMERA
    };
    int PERM_CODE = 11;

//    private ReadInput mReadThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);

        if (mBluetoothAdapter == null) {
            out.append("device not supported");
        }

        Log.d(TAG, "Ready");


        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        getSupportActionBar().hide();

        setContentView(R.layout.activity_scanner);
        checkpermissions();
        codeScannerView = (CodeScannerView) findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, codeScannerView);

        Bundle extras = getIntent().getExtras();
        key_device = extras.getString(key_device);
        key_mDevice = extras.getString(key_mDevice);
        key_mBuffer = extras.getString(key_mBuffer);

//        codeScanner.setCamera(1);

        decorView = getWindow() .getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0)
                    decorView.setSystemUiVisibility(hideSystemBars());
            }
        });

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        username = result.getText().toString();

                        login();
                    }
                });
            }
        });

//        Intent intent = getIntent();
//        Bundle b = intent.getExtras();
//        mDevice = b.getParcelable(BluetoothFragment.DEVICE_EXTRA);
////        mDeviceUUID = UUID.fromString(b.getString(BluetoothFragment.DEVICE_UUID));
//        mMaxChars = b.getInt(BluetoothFragment.BUFFER_SIZE);

        Log.d(TAG, "Ready");

    }

    private void checkpermissions(){
        List<String> listofpermisssions = new ArrayList<>();
        for (String perm: permissions){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), perm) != PackageManager.PERMISSION_GRANTED){
                listofpermisssions.add(perm);

            }
        }
        if (!listofpermisssions.isEmpty()){
            ActivityCompat.requestPermissions(this, listofpermisssions.toArray(new String[listofpermisssions.size()]), PERM_CODE);
        }
    }


    private void login() {
        loading = ProgressDialog.show(Scanner.this, "Memuat Data", "Harap Tunggu ..");
        OkHttpClient client = new OkHttpClient();


        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(InterfaceAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        InterfaceAPI api = retrofit.create(InterfaceAPI.class);

        Call<Login> call = api.loginScanner(username);

        call.enqueue(new Callback<Login>() {
            @Override
            public void onResponse(Call<Login> call, Response<Login> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(Scanner.this, response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                motorOn();
                id = response.body().getId();
                if (response.body().getSuccess() != null){
                    loading.cancel();
                    BluetoothDevice device = (mDevice);
//                    Intent intent = new Intent(getApplicationContext(), Scanner.class);
                    Intent i = new Intent(Scanner.this, MapsActivity.class);
                    i.putExtra(DEVICE_EXTRA, device);
                    i.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                    i.putExtra(BUFFER_SIZE, mBufferSize);

                    i.putExtra(KEY_NAME, id);
                    i.putExtra(key_device, key_device);
                    i.putExtra(key_mDevice, key_mDevice);
                    i.putExtra(key_mBuffer, key_mBuffer);

//                    startActivity(intent);
                    startActivity(i);
                }
            }

            @Override
            public void onFailure(Call<Login> call, Throwable t) {
                Toast.makeText(Scanner.this, t.getMessage() + " | " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        requestCamera();
//    }

    private void requestCamera() {
        codeScanner.startPreview();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(Scanner.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554

        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
// Unable to connect to device`
                // e.printStackTrace();
                mConnectSuccessful = false;



            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();

                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;

                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);

                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
                         */



                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {//cant inderstand these dotss

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }


    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
        requestCamera();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }




    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
    private void motorOn() {

        ByteArrayOutputStream stream
                = new ByteArrayOutputStream();

        // Initializing string
//        String st = "0";

        // writing the specified byte to the output stream
        try {
            String sendtxt = "0";
            mBTSocket.getOutputStream().write(sendtxt.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // converting stream to byte array
        // and typecasting into string
//        String finalString
//                = new String(stream.toByteArray());
//
//        // printing the final string
//        System.out.println(finalString);

    }





}
