package com.example.bismillah_motor_listrik.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostMotor {
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private dataMotor data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public dataMotor getData() {
        return data;
    }

    public void setData(dataMotor data) {
        this.data = data;
    }
}
