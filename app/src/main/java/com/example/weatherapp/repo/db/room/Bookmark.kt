package com.example.weatherapp.repo.db.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(

    @PrimaryKey(autoGenerate = true)
    val id : Int,
    @ColumnInfo()
    var latitude : String = "",
    @ColumnInfo()
    var longitude : String="",
    @ColumnInfo()
    var cityName : String="",
    @ColumnInfo()
    val temp : String ="",
    @ColumnInfo()
    var bookmarked : Boolean = false
)
