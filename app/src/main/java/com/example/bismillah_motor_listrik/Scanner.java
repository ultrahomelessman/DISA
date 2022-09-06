package com.example.bismillah_motor_listrik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

public class Scanner extends AppCompatActivity {

    CodeScanner codeScanner;
    CodeScannerView codeScannerView;
    private String KEY_NAME = "NAMA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        codeScannerView = (CodeScannerView) findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, codeScannerView);

//        codeScanner.setCamera(1);

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String nama = result.getText().toString();

//                        langsung ke start page selanjutnya
//                        startActivity(new Intent(getApplicationContext(), Tampilan.class));

                        Intent i = new Intent(Scanner.this, Tampilan.class);
                        i.putExtra(KEY_NAME, nama);
                        startActivity(i);
                    }
                });
            }
        });
    }


    private void codeapply() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCamera();
    }

    private void requestCamera() {
        codeScanner.startPreview();
    }
}