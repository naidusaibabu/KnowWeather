package com.example.weatherapp.repo.api

import com.example.weatherapp.repo.models.CurrentCityInfo
import com.example.weatherapp.repo.models.WeatherInfo
import com.example.weatherapp.util.Constants.Companion.OPEN_WEATHER_API_CLIENT
import com.example.weatherapp.util.Constants.Companion.OPEN_WEATHER_ONE_CALL_API_CLIENT
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface WeatherAppApi {

    @GET(OPEN_WEATHER_ONE_CALL_API_CLIENT)
    suspend fun getWeatherInfo(@QueryMap options: Map<String, String>): Response<WeatherInfo>

    @GET(OPEN_WEATHER_API_CLIENT)
    suspend fun getCurrentCityWeatherInfo(@QueryMap options: Map<String, String>): Response<CurrentCityInfo>

}