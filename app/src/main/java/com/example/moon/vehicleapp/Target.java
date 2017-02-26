package com.example.moon.vehicleapp;

/**
 * Created by MOON on 23-11-2016.
 */

public class Target {

    public String name;
    public String lng;
    public String lat;
    public String address;
    public String toggle;

    public Target() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Target(String name, String lng,String lat,String address,String toggle) {
        this.name = name;
        this.lng = lng;
        this.lat=lat;
        this.address=address;
        this.toggle=toggle;
    }

}




