package com.example.weatherapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.repo.WeatherAppRepository

class WeatherAppViewModelFactory(private val repository: WeatherAppRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(WeatherAppViewModel::class.java)){
            return WeatherAppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }

}