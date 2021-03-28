package com.example.weatherapp.repo

import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.repo.db.room.Bookmark
import com.example.weatherapp.repo.db.room.BookmarkDAO
import com.example.weatherapp.repo.models.WeatherInfo

class WeatherAppRepository(private val bookmarkDAO: BookmarkDAO) {
    val cities = bookmarkDAO.getCities()

    /**
     * checking for bookmarked bookmarks
     */
    init {

        bookmarkDAO.getCities()
    }

    suspend fun getWeatherInfo(latLangData: Map<String, String>) =
        RetrofitInstance.weatherApi.getWeatherInfo(latLangData)


    suspend fun getCurrentCityWeatherInfo(latLangData: Map<String, String>) =
        RetrofitInstance.weatherApi.getCurrentCityWeatherInfo(latLangData)

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