package com.example.bismillah_motor_listrik;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bismillah_motor_listrik.API.InterfaceAPI;
import com.example.bismillah_motor_listrik.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Tampilan extends AppCompatActivity {

    TextView txt, message, name;
    private String id;
    private String KEY_NAME = "NAMA";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tampilan);

        txt = findViewById(R.id.code);
        message = findViewById(R.id.message);
        name = findViewById(R.id.name);

        Bundle extras = getIntent().getExtras();
        id = extras.getString(KEY_NAME);
        txt.setText(id);
    }

    private void user() {
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
                message.setText(response.body().getMessage());
                name.setText(response.body().getData().getName());
                Toast.makeText(Tampilan.this,
                        "Berhasil : " + response.code(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(Tampilan.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}