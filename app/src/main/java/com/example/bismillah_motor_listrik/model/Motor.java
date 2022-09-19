package com.example.bismillah_motor_listrik.model;

public class Motor {
    private String battery, jarak, latitude, longitude;

    public Motor(String battery, String jarak, String latitude, String longitude) {
        this.battery = battery;
        this.jarak = jarak;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getBattery() {
        return battery;
    }

    public String getJarak() {
        return jarak;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
