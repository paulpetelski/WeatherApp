package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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
    String city, state, weatherDesc;
    String api_url, api_url_daily_forecast;
    double tempF;

    public static final String TAG = "Main";

    TextView location, temp, dayOneMax, dayTwoMax, dayThreeMax, nextThreeDaysTitle;
    ImageView sunny, cloudy, rainy, thunderstorm, snow;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // text fields
        location = findViewById(R.id.location);
        temp = findViewById(R.id.temp);
        // daily weather forecast
        nextThreeDaysTitle = findViewById(R.id.nextThreeDaysTitle);
        dayOneMax = findViewById(R.id.dayOneMax);
        dayTwoMax = findViewById(R.id.dayTwoMax);
        dayThreeMax = findViewById(R.id.dayThreeMax);
        // icons
        sunny = findViewById(R.id.sunny);
        cloudy = findViewById(R.id.cloudy);
        rainy = findViewById(R.id.rainy);
        thunderstorm = findViewById(R.id.thunderstorm);
        snow = findViewById(R.id.snow);
        // buttons
        button = findViewById(R.id.button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // on button press change text
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                getLastLocation();

                try {
                    getCity();
                    getState();
                } catch (IOException e) {
                       e.printStackTrace();
                }

                // uses secrets gradle plugin to store api_key in local.properties which is not uploaded to github to keep api_key private
                api_url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=imperial&appid=" + BuildConfig.Api_key;
                api_url_daily_forecast = "https://api.openweathermap.org/data/2.5/onecall?lat=" + latitude + "&lon=" + longitude + "&units=imperial&exclude=hourly,minutely&appid=" + BuildConfig.Api_key;
                Log.d(TAG,api_url);

                getWeatherFromApi();
                System.out.println(api_url_daily_forecast);
                getDailyWeather();





            }
        });
    }

    /**
     * Get daily forecasts
     *
     */
    public void getDailyWeather(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api_url_daily_forecast, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // log response from api
                Log.d("api", response);

                // parse JSON data
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray dailyForecast = obj.getJSONArray("daily");
                    JSONObject dailyObject = dailyForecast.getJSONObject(0);
                    String dayUTC = dailyObject.getString("dt");
                    // returns day and time in unix UTC
                    // TODO: make method to convert to regular day and local time

                    System.out.println(dayUTC);

                    // days for forecast
                    JSONObject dayOne = dailyForecast.getJSONObject(1);
                    JSONObject dayTwo = dailyForecast.getJSONObject(2);
                    JSONObject dayThree = dailyForecast.getJSONObject(3);

                    int dayOneTempMax = (int) dayOne.getJSONObject("temp").getDouble("max");
                    int dayTwoTempMax = (int) dayTwo.getJSONObject("temp").getDouble("max");
                    int dayThreeTempMax = (int) dayThree.getJSONObject("temp").getDouble("max");
                    System.out.println("Max Temps for next 3 days: " + dayOneTempMax + " " + dayTwoTempMax + " " + dayThreeTempMax);
                    // display temperatures for next three days
                    dayOneMax.setText(dayOneTempMax + " °F");
                    dayTwoMax.setText(dayTwoTempMax + " °F" );
                    dayThreeMax.setText(dayThreeTempMax + " °F");


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

                    double tempF = obj.getJSONObject("main").getDouble("temp");
                    // sunny, cloudy, etc
                    // weather is an array has [{}] so need to created JSONArray
                    // then get the weather description using JSONObject to search that array
                    JSONArray weatherArray = obj.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    // sunny, Clouds, etc
                    weatherDesc = weatherObject.getString("main");

                    // Print the location and weather
                    location.setText(city + ", " + state);
                    temp.setText((int) tempF + "°F");

                    // icons depending on weather
                    showIcons();

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
     * Convert from kelvin to fahrenheit
     * @param kelvin temperature
     * @return temperature in fahrenheit
     */
    public int convertToF(double kelvin){
        return (int) (tempF = (int) ((9.0/5.0)*(kelvin-273.15)+32));
    }

    /**
     * Display weather icons
     * ex. sun, clouds, rain, snow
     */
    private void showIcons(){
        if (weatherDesc.equals("Clouds"))
            cloudy.setVisibility(View.VISIBLE);
        else if(weatherDesc.equals("Clear"))
            sunny.setVisibility(View.VISIBLE);
        else if(weatherDesc.equals("Rain") || weatherDesc.equals("Drizzle")){
            rainy.setVisibility(View.VISIBLE);
        } else if(weatherDesc.equals("Snow")){
            snow.setVisibility(View.VISIBLE);
        } else if(weatherDesc.equals("Thunderstorm")){
            thunderstorm.setVisibility(View.VISIBLE);
        }
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

    /**
     * Converts lat and long to name of State
     * @return state user is located in
     * @throws IOException
     */
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
