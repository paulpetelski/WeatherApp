package com.example.weatherapp;

import static com.google.android.gms.location.LocationRequest.create;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Fused Client to get location
    FusedLocationProviderClient fusedLocationClient;
    double latitude;
    double longitude;
    String city, state;
    String api_url;
    double tempF;

    // openweathermap api key
    public static final String TAG = "Main";

    TextView location, temp;
    ImageView sunny, cloudy;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location = findViewById(R.id.location);
        temp = findViewById(R.id.temp);

        sunny = findViewById(R.id.sunny);
        cloudy = findViewById(R.id.cloudy);

        button = findViewById(R.id.button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // on button press change text to show lat, long, and city
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                getLastLocation();

                System.out.println(latitude);
                System.out.println(longitude);
                try {
                    getCity();
                    getState();
                } catch (IOException e) {
                       e.printStackTrace();
                }

                // uses secrets gradle plugin to store api_key in local.properties which is not uploaded to github to key api_key private
                api_url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + BuildConfig.Api_key;
                Log.d(TAG,api_url);

                getWeatherFromApi();
            }
        });
    }

    /**
     * Get weather data in JSON format from openweathermap API. Retrieve temperature (kelvin) and convert to fahrenheit.
     *
     * tempK = temperature in Kelvin
     * tempF = temperature in Fahrenheit
     *
     */
    public void getWeatherFromApi(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // log response from api
                Log.d("api", response);

                // parse JSON data
                try {
                    JSONObject obj = new JSONObject(response);

                    double tempK = obj.getJSONObject("main").getDouble("temp");
                    // sunny, cloudy, etc
                    // weather is an array has [{}] so need to created JSONArray
                    // then get the weather description using JSONObject to search that array
                    JSONArray weatherArray = obj.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    // sunny, Clouds, etc
                    String weatherDesc = weatherObject.getString("main");


                    // convert temperature from Kelvin to Fahrenheit
                    tempF = ((9.0/5.0)*(tempK-273.15)+32);

                    // Print the location and weather
                    location.setText(city + ", " + state);
                    temp.setText((int) tempF + "°F");

                    // TODO: add more icons for different weather
                    // icons depending on weather
                    // only have sunny and cloudy icons so far
                    if (weatherDesc.equals("Clouds"))
                        cloudy.setVisibility(View.VISIBLE);
                    else if(weatherDesc.equals("Clear"))
                        sunny.setVisibility(View.VISIBLE);
                    else if(weatherDesc.equals("Rain") || weatherDesc.equals("Drizzle")){
                        cloudy.setVisibility(View.VISIBLE);
                    } else if(weatherDesc.equals("Snow")){
                        cloudy.setVisibility(View.VISIBLE);
                    } else if(weatherDesc.equals("Thunderstorm")){
                        cloudy.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley","could not get data from API");
            }
        });
        queue.add(stringRequest);
    }

    /**
     * Converts latitude and longitude to name of city
     * @return city user is located in
     * @throws IOException
     */
    private String getCity() throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List <Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
        try{
            addresses = geocoder.getFromLocation(latitude,longitude,1);
        } catch (IOException e){
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0){
            city = addresses.get(0).getLocality();
        }

        return city;
    }

    private String getState() throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List <Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
        try{
            addresses = geocoder.getFromLocation(latitude,longitude,1);
        } catch (IOException e){
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0){
            // getAdminArea returns State the city is in
            state = addresses.get(0).getAdminArea();
        }
        return state;
    }

    /**
     * Get last location of device. If permissions not granted, request permissions.
     */
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("permissions","not granted");
            // make pop up to get permission to see location
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        } else {
            // if we have permissions
            // get the last location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // have last known location
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d("Location", "found");
                            }
                        }
                    });
        }
    }
}
