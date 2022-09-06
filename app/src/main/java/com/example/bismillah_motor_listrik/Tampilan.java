package com.example.bismillah_motor_listrik;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class Tampilan extends AppCompatActivity {

    TextView txt;
    private String nama;
    private String KEY_NAME = "NAMA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tampilan);

        txt = (TextView) findViewById(R.id.code);

        Bundle extras = getIntent().getExtras();
        nama = extras.getString(KEY_NAME);
        txt.setText("Hello, " + nama + " !");
    }
}