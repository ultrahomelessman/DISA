package com.example.bismillah_motor_listrik.API;

import com.example.bismillah_motor_listrik.model.Login;
import com.example.bismillah_motor_listrik.model.Motor;
import com.example.bismillah_motor_listrik.model.PostMotor;
import com.example.bismillah_motor_listrik.model.Respon;
import com.example.bismillah_motor_listrik.model.Update;
import com.example.bismillah_motor_listrik.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface InterfaceAPI {

    String BASE_URL = "https://461c-202-67-40-205.ngrok.io/";

    @POST("api/login")
    @FormUrlEncoded
    Call<Login> loginScanner(@Field("username") String username);

    @GET("api/user/{id}")
    Call<User> user(@Path("id") String id);

    @PUT("api/user/{id}")
    Call<Respon> putCredit(@Path("id") String id, @Body Update put);

    @PUT("api/motor/{id}")
    Call<PostMotor> putMotor(@Path("id") String id, @Body Motor realtime);
}

