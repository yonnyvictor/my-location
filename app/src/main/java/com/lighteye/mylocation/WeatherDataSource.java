package com.lighteye.mylocation;

import com.lighteye.mylocation.data.Weather;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by yonny on 5/17/17.
 */

public class WeatherDataSource {
    private final WeatherService mWeatherService;
    private final String units = "imperial";

    public WeatherDataSource(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mWeatherService = retrofit.create(WeatherService.class);
    }

    public Observable<Weather> getWeather(double latitude, double longitude){
        return mWeatherService.getWeather(latitude, longitude, units, BuildConfig.OPEN_WEATHER_MAP_API_KEY);
    }
}
