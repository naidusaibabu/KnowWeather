package com.example.weatherapp.repo.models

data class Daily(
    val clouds: Int,
    val dt: Int,
    val feels_like: FeelsLike,
    val humidity: Int,
    val sunrise: Int,
    val sunset: Int,
    val temp: Temp,
    val weather: List<Weather>,
    val wind_speed: Double
)