package com.example.weatherapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.repo.WeatherAppRepository
import com.example.weatherapp.repo.db.room.Bookmark
import com.example.weatherapp.repo.models.CurrentCityInfo
import com.example.weatherapp.repo.models.WeatherInfo
import com.example.weatherapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class WeatherAppViewModel(
    app: WeatherApplication,
    private val weatherAppRepository: WeatherAppRepository
) : AndroidViewModel(app) {

    val cities = weatherAppRepository.cities

    val currentCityInfo: MutableLiveData<Resource<CurrentCityInfo>> = MutableLiveData()

    fun getCurrentCityWeatherInfo(latLangData: Map<String, String>) = viewModelScope.launch {
        safeCurrentCityInfoCall(latLangData)
    }

    val weatherInfo: MutableLiveData<Resource<WeatherInfo>> = MutableLiveData()

    suspend fun getWeatherInfo(latLangData: Map<String, String>) = viewModelScope.launch {
        safeWeatherInfoCall(latLangData)
    }

    private fun handleCurrentCityInfo(response: Response<CurrentCityInfo>): Resource<CurrentCityInfo> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                return Resource.Success(result)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleWeatherInfo(response: Response<WeatherInfo>): Resource<WeatherInfo> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                return Resource.Success(result)
            }
        }
        return Resource.Error(response.message())
    }

    fun insertCity(bookmark: Bookmark) = viewModelScope.launch {
        weatherAppRepository.insertCity(bookmark)
    }

    fun deleteCity(bookmark: Bookmark) = viewModelScope.launch {
        weatherAppRepository.deleteCity(bookmark)
    }

    fun updateCity(bookmark: Bookmark) = viewModelScope.launch {
        weatherAppRepository.updateCity(bookmark)
    }

    private suspend fun safeCurrentCityInfoCall(latLangData: Map<String, String>) {
        currentCityInfo.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = weatherAppRepository.getCurrentCityWeatherInfo(latLangData)
                currentCityInfo.postValue(handleCurrentCityInfo(response))
            } else {
                currentCityInfo.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> currentCityInfo.postValue(Resource.Error("Network failure"))
                else -> currentCityInfo.postValue(Resource.Error("Conversion error"))
            }
        }

    }

    private suspend fun safeWeatherInfoCall(latLangData: Map<String, String>) {
        currentCityInfo.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = weatherAppRepository.getWeatherInfo(latLangData)
                weatherInfo.postValue(handleWeatherInfo(response))
            } else {
                weatherInfo.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> weatherInfo.postValue(Resource.Error("Network failure"))
                else -> weatherInfo.postValue(Resource.Error("Conversion error"))
            }
        }

    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<WeatherApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}
