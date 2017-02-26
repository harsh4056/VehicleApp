package com.example.moon.vehicleapp;

/**
 * Created by MOON on 22-11-2016.
 */

public class User {

    public String status;
    public String lng;
    public String lat;
    public String name;
    public String phone;
    public String unit;
    public String icon;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String status, String lng,String lat,String name,String unit,String phone,String icon) {
        this.status = status;
        this.lng = lng;
        this.lat=lat;
        this.name=name;
        this.unit=unit;
        this.phone=phone;
        this.icon=icon;
    }

}