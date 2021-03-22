package com.example.weatherapp.repo

import com.example.weatherapp.util.Constants.Companion.OPEN_WEATHER_API_KEY
import okhttp3.Interceptor
import okhttp3.Response

class WeatherAppInterceptor : Interceptor {
    companion object{
        const val UNITS = "metric"
        const val EXCLUDE_ITEMS = "minutely,hourly"
    }
    /**
     * Interceptor class for setting of the api key for every request
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val originalHttpUrl = request.url

        /**
         * setting up appid as query parameter here
         */
        val modifiedHttpUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("units", UNITS)
            .addQueryParameter("exclude", EXCLUDE_ITEMS)
            .addQueryParameter("appid", OPEN_WEATHER_API_KEY)
            .build()
        request = request.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .url(modifiedHttpUrl)
            .build()
        return chain.proceed(request)
    }
}