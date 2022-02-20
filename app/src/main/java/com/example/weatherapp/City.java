package com.example.weatherapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class City extends MainActivity {
    private double latitude;
    private double longitude;
    private String city;
    private String State;
    private Object Context;

    public City(Double latitude, Double longitude){
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public City(Double latitude, Double longitude, String city, String state){
        setLatitude(latitude);
        setLongitude(longitude);
        setCity(city);
        setState(state);
    }

    public void setLatitude(Double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude){
        this.longitude = longitude;
    }

    public void setCity(String city){
        this.city = city;
    }

    public void setState(String state){
        this.state = state;
    }



}
