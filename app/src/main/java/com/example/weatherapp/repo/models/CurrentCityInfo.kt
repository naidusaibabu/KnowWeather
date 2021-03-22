package com.example.weatherapp.repo.models

data class CurrentCityInfo(
    val clouds: Clouds,
    val dt: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val weather: List<Weather>,
    val wind: Wind
)