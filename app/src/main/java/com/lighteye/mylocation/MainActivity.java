package com.lighteye.mylocation;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.lighteye.mylocation.data.Weather;

import java.text.DateFormat;
import java.util.Date;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private double currentTemperatureInFahrenheit;
    private double currentTemperatureInCelsius;
    private WeatherDataSource weatherDataSource;

    private TextView mLatitudeTextView, mLongitudeTextView, mTempInCelsiusTextView,
            mTempInFahrenheitTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);

        weatherDataSource = new WeatherDataSource();

        mLatitudeTextView = (TextView) findViewById(R.id.latitude_textview);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_textview);
        mTempInCelsiusTextView = (TextView) findViewById(R.id.temp_in_celsius_textview);
        mTempInFahrenheitTextView = (TextView) findViewById(R.id.temp_in_fahrenheit_textview);
        TextView dateTextView = (TextView) findViewById(R.id.date_textview);

        dateTextView.setText(DateFormat.getDateInstance(DateFormat.LONG).format(new Date()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            } else {
                //everything went fine lets get latitude and longitude
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                displayLocation();

                //load weather details
                loadWeatherDetails();
            }

        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        displayLocation();
    }

    private void loadWeatherDetails(){
        weatherDataSource.getWeather(currentLatitude, currentLongitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Weather>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("WeatherDetailsError", e.getMessage());
                    }

                    @Override
                    public void onNext(Weather weather) {
                        displayTemperature(weather.main.temp);
                    }
                });
    }

    private double convertFahrenheitToCelsius(double fahrenheit){
        return (fahrenheit - 32) * 5 / 9;
    }

    private void displayLocation(){
        mLatitudeTextView.setText(String.valueOf(currentLatitude));
        mLongitudeTextView.setText(String.valueOf(currentLongitude));
    }

    private void displayTemperature(double temperatureInFahrenheit){
        currentTemperatureInFahrenheit = temperatureInFahrenheit;
        currentTemperatureInCelsius = convertFahrenheitToCelsius(currentTemperatureInFahrenheit);

        String celsius = formatTemperature(currentTemperatureInCelsius) + "C";
        String fahrenheit = formatTemperature(currentTemperatureInFahrenheit) + "F";

        mTempInCelsiusTextView.setText(celsius);
        mTempInFahrenheitTextView.setText(fahrenheit);
    }

    private String formatTemperature(double temperature) {
        return getString(R.string.format_temperature, temperature);
    }
}
