package com.singapore.trafficcamera.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.singapore.trafficcamera.daos.FavouriteDao
import com.singapore.trafficcamera.daos.UserDao
import com.singapore.trafficcamera.models.dao.Favourite
import com.singapore.trafficcamera.models.dao.User
import com.singapore.trafficcamera.utils.ConverterUtils

@Database(entities = [User::class, Favourite::class], version = 3, exportSchema = false)
@TypeConverters(ConverterUtils::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun favouriteDao(): FavouriteDao
}