package com.example.bismillah_motor_listrik;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    public static final int RESPONSE_MESSAGE = 10;
    Handler uih;

    public ConnectedThread(BluetoothSocket mmSocket, InputStream mmInStream, OutputStream mmOutStream) {
        this.mmSocket = mmSocket;
        this.mmInStream = mmInStream;
        this.mmOutStream = mmOutStream;
    }
}
