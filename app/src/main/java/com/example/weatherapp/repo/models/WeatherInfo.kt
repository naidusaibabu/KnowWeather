package com.example.weatherapp.repo.models

data class WeatherInfo(
    val current: Current,
    val daily: List<Daily>,
    val lat: Double,
    val lon: Double
)