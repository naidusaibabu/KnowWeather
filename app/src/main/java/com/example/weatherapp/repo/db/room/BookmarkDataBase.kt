package com.example.weatherapp.repo.db.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Bookmark::class], version = 6)
abstract class BookmarkDataBase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDAO

    companion object {
        @Volatile
        private var INSTANCE: BookmarkDataBase? = null
        fun getInstance(context: Context): BookmarkDataBase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BookmarkDataBase::class.java,
                        "city_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }

    }
}