package com.singapore.trafficcamera.repository

import androidx.lifecycle.LiveData
import com.singapore.trafficcamera.daos.FavouriteDao
import com.singapore.trafficcamera.models.dao.Favourite
import javax.inject.Inject

class FavouriteRepository @Inject constructor(private val favouriteDao: FavouriteDao) {

    suspend fun insertUserFavouriteCamera(favourite: Favourite) {
        favouriteDao.insertUserFavouriteCamera(favourite = favourite)
    }

    suspend fun deleteUserFavouriteCamera(username: String, cameraId: String) {
        favouriteDao.deleteUserFavouriteCamera(username = username, cameraId = cameraId)
    }

    suspend fun dropFavouriteTable() {
        favouriteDao.dropFavouriteTable()
    }

    fun getFavouriteCameraBasedOnUsername(username: String): LiveData<List<String>> {
        return favouriteDao.getFavouriteCameraBasedOnUsername(username = username)
    }
}