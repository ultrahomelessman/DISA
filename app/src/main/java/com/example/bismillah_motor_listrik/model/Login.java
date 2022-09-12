package com.example.bismillah_motor_listrik.model;

public class Login {

    private  String success, message, token, id;

    public Login(String success, String message, String token, String id) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}