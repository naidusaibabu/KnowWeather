package com.example.weatherapp.repo.db.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookmarkDAO {

    @Query("SELECT * FROM bookmark")
    fun getCities() : LiveData<MutableList<Bookmark>>

    @Insert
    suspend fun insertCity(bookmark : Bookmark)

    @Query("UPDATE bookmark SET bookmarked = :isMarked WHERE id = :updatedId")
    suspend fun update(updatedId : Int,isMarked : Boolean)

    @Delete
    suspend fun delete(bookmark: Bookmark)
}