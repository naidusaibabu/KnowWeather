package com.example.weatherapp.repo.models

data class Current(
    val clouds: Int,
    val dt: Int,
    val feels_like: Double,
    val humidity: Int,
    val sunrise: Int,
    val sunset: Int,
    val temp: Double,
    val weather: List<Weather>,
    val wind_speed: Double
)