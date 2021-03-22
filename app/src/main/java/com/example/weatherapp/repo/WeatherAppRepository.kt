package com.example.weatherapp.repo

import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.repo.db.room.Bookmark
import com.example.weatherapp.repo.db.room.BookmarkDAO
import com.example.weatherapp.repo.models.CurrentCityInfo
import com.example.weatherapp.repo.models.WeatherInfo

class WeatherAppRepository(private val bookmarkDAO: BookmarkDAO) {
    val cities = bookmarkDAO.getCities()
    /**
     * checking for bookmarked bookmarks
     */
    init {

        bookmarkDAO.getCities()
    }

    val weatherInfo: MutableLiveData<WeatherInfo> = MutableLiveData()

    suspend fun getWeatherInfo(latLangData: Map<String, String>) {
        val response = RetrofitInstance.weatherApi.getWeatherInfo(latLangData)
        if (response.isSuccessful)
            weatherInfo.postValue(response.body())
    }

    val currentCityInfo: MutableLiveData<CurrentCityInfo> = MutableLiveData()

    suspend fun getCurrentCityWeatherInfo(latLangData: Map<String, String>) {
        val response = RetrofitInstance.weatherApi.getCurrentCityWeatherInfo(latLangData)
        if (response.isSuccessful)
            currentCityInfo.postValue(response.body())
    }

    suspend fun insertCity(bookmark: Bookmark) {
        bookmarkDAO.insertCity(bookmark)
    }

    suspend fun deleteCity(bookmark: Bookmark) {
        bookmarkDAO.delete(bookmark)
    }

    suspend fun updateCity(bookmark: Bookmark) {
        bookmarkDAO.update(bookmark.id, bookmark.bookmarked)
    }

}