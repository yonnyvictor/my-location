package com.lighteye.mylocation;

import com.lighteye.mylocation.data.Weather;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yonny on 5/17/17.
 */

public interface WeatherService {
    @GET("/data/2.5/weather")
    Observable<Weather> getWeather(@Query("lat") double lat, @Query("lon") double lon,
                                   @Query("units") String units, @Query("appid") String appid);
}
