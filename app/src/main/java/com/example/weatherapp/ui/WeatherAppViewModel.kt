package com.example.weatherapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.repo.WeatherAppRepository
import com.example.weatherapp.repo.db.room.Bookmark
import kotlinx.coroutines.launch

class WeatherAppViewModel(private val weatherAppRepository: WeatherAppRepository) : ViewModel() {

    val cities = weatherAppRepository.cities

    val weatherInfo = weatherAppRepository.weatherInfo

    val currentCityInfo = weatherAppRepository.currentCityInfo

    fun insertCity(bookmark: Bookmark) = viewModelScope.launch {
        weatherAppRepository.insertCity(bookmark)
    }

    fun deleteCity(bookmark: Bookmark) = viewModelScope.launch {
        weatherAppRepository.deleteCity(bookmark)
    }

    fun updateCity(bookmark: Bookmark) = viewModelScope.launch {
        weatherAppRepository.updateCity(bookmark)
    }
}